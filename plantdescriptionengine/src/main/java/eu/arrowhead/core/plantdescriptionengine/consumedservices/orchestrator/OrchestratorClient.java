package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreEntry;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreEntryList;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreEntryListDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreRuleDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.RuleStore;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.RuleStoreException;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionUpdateListener;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.utils.RetryFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OrchestratorClient implements PlantDescriptionUpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorClient.class);

    private final HttpClient httpClient;
    private final InetSocketAddress orchestratorAddress;
    private final RuleStore ruleStore;
    private final PlantDescriptionTracker pdTracker;
    private final RuleCreator ruleCreator;

    /**
     * Class constructor.
     *
     * @param httpClient Object for sending HTTP messages to the Orchestrator.
     * @param ruleStore  Object providing permanent storage for Orchestration
     *                   rule data.
     * @param pdTracker  Object used for keeping track of Plant Descriptions.
     */
    public OrchestratorClient(
        final HttpClient httpClient,
        final RuleStore ruleStore,
        final PlantDescriptionTracker pdTracker,
        final InetSocketAddress orchestratorAddress
    ) {

        Objects.requireNonNull(httpClient, "Expected HttpClient");
        Objects.requireNonNull(ruleStore, "Expected backing store");
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
        Objects.requireNonNull(orchestratorAddress, "Expected Orchestrator address");

        this.httpClient = httpClient;
        this.ruleStore = ruleStore;
        this.pdTracker = pdTracker;
        this.orchestratorAddress = orchestratorAddress;

        ruleCreator = new RuleCreator(pdTracker);
    }

    /**
     * Initializes the Orchestrator client.
     *
     * @return A {@code Future} which will complete once Orchestrator rules have
     * been created for the connections in the active Plant Description entry
     * (if any).
     */
    public Future<Void> initialize() {
        pdTracker.addListener(this);
        PlantDescriptionEntry activeEntry = pdTracker.activeEntry();

        final String retryMessage = "Failed to connect to the Orchestrator, retrying in "
            + ApiConstants.CORE_SYSTEM_RETRY_DELAY / 1000 +
            " seconds.";

        final RetryFuture retrier = new RetryFuture(
            ApiConstants.CORE_SYSTEM_RETRY_DELAY,
            ApiConstants.CORE_SYSTEM_MAX_RETRIES,
            retryMessage);

        // If there is no active Plant Description entry, simply ping the
        // orchestrator.
        if (activeEntry == null) {
            return retrier.run(this::pingOrchestrator);
        }

        // If there is an active Plant Description entry, ping the orchestrator
        // and then update orchestration rules.
        return retrier.run(this::pingOrchestrator)
            .flatMap(response -> deleteRules(activeEntry.id()))
            .flatMap(deletionResult -> postRules())
            .flatMap(createdRules -> {
                ruleStore.writeRules(activeEntry.id(), createdRules.getIds());
                logger.info("Created rules for Plant Description Entry '" + activeEntry.plantDescription() + "'.");
                return Future.done();
            });
    }

    private Future<Void> pingOrchestrator() {
        return httpClient
            .send(orchestratorAddress,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(ApiConstants.ORCHESTRATOR_ECHO_PATH)
                    .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON))
            .flatMap(response -> Future.done());
    }

    private Future<StoreEntryListDto> postRules() {

        final List<StoreRuleDto> rules = ruleCreator.createRules();

        if (rules.isEmpty()) {
            return Future.success(emptyRuleList());
        }

        return httpClient
            .send(orchestratorAddress,
                new HttpClientRequest()
                    .method(HttpMethod.POST)
                    .uri(ApiConstants.ORCHESTRATOR_STORE_PATH)
                    .body(rules, CodecType.JSON)
                    .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON))
            .flatMap(response -> response.bodyToIfSuccess(StoreEntryListDto::decodeJson));
    }

    /**
     * @return An empty {@code StoreEntryListDto}.
     */
    private StoreEntryListDto emptyRuleList() {
        return new StoreEntryListDto.Builder().count(0).build();
    }

    /**
     * Deletes a single Orchestrator Store Entry.
     *
     * @param id The ID of an Orchestrator Store Entry to delete.
     * @return A Future that performs the deletion.
     */
    private Future<Void> deleteRule(final int id) {
        return httpClient
            .send(orchestratorAddress,
                new HttpClientRequest()
                    .method(HttpMethod.DELETE)
                    .uri(ApiConstants.ORCHESTRATOR_STORE_PATH + "/" + id)
                    .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON))
            .flatMap(response -> {
                if (response.status() != HttpStatus.OK) {
                    return Future.failure(OrchestratorRequestException.ruleDeletionFailure(id));
                }
                return Future.done();
            });
    }

    /**
     * Deletes all orchestrator rules created by the Orchestrator client for the
     * given Plant Description.
     *
     * @param plantDescriptionId ID of a Plant Description.
     * @return A Future that performs the deletions.
     */
    private Future<Void> deleteRules(int plantDescriptionId) {

        final Set<Integer> rules;
        try {
            rules = ruleStore.readRules(plantDescriptionId);
        } catch (final RuleStoreException e) {
            return Future.failure(e);
        }

        if (rules.isEmpty()) {
            return Future.done();
        }

        return Futures.serialize(rules.stream()
            .map(this::deleteRule))
            .flatMap(result -> {
                ruleStore.removeRules(plantDescriptionId);
                logger.info(
                    "Deleted all orchestrator rules created for Plant Description with ID "
                        + plantDescriptionId + "."
                );
                return Future.done();
            });
    }

    /**
     * Logs the fact that the specified entry has been activated.
     *
     * @param entry    A Plant Description Entry.
     * @param ruleList The list of Orchestrator rules connected to the entry.
     */
    private void logEntryActivated(final PlantDescriptionEntry entry, final StoreEntryList ruleList) {

        final String entryName = entry.plantDescription();

        if (ruleList.count() > 0) {
            String msg = "Orchestrator rules created for Plant Description '" + entryName + "': [";
            final List<String> ids = new ArrayList<>();
            for (final StoreEntry rule : ruleList.data()) {
                ids.add(rule.id().toString());
            }
            msg += String.join(", ", ids) + "]";
            logger.info(msg);
        } else {
            logger.warn("No new rules were created for Plant Description '" + entryName + "'.");
        }
    }

    /**
     * Handles an update of a Plant Description Entry.
     * <p>
     * Deletes and/or creates rules in the Orchestrator as appropriate.
     *
     * @param updated The updated Plant Description entry.
     * @param old     The Plant Description entry as it was before the update.
     */
    @Override
    public Future<?> onPlantDescriptionUpdated(final PlantDescriptionEntry updated, final PlantDescriptionEntry old) {
        Objects.requireNonNull(updated, "Expected updated entry.");

        final boolean wasActive = old != null && old.active();
        final Future<Void> deleteRulesTask = wasActive ? deleteRules(updated.id()) : Future.done();

        return deleteRulesTask
            .flatMap(deletionResult -> {
                final boolean activeConnectionsExist = !pdTracker.getActiveConnections().isEmpty();
                final boolean shouldPostRules = updated.active() && activeConnectionsExist;
                return shouldPostRules ? postRules() : Future.success(emptyRuleList());
            })
            .flatMap(createdRules -> {
                if (updated.active()) {
                    try {
                        ruleStore.writeRules(updated.id(), createdRules.getIds());
                        logEntryActivated(updated, createdRules);
                    } catch (RuleStoreException e) {
                        return Future.failure(e);
                    }
                } else if (wasActive) {
                    logger.info("Deactivated Plant Description '" + updated.plantDescription() + "'");
                }
                return Future.done();
            });
    }

    /**
     * Handles the addition of a new Plant Description Entry.
     *
     * @param entry The added entry.
     */
    @Override
    public Future<?> onPlantDescriptionAdded(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected entry.");
        return onPlantDescriptionUpdated(entry, null);
    }

    /**
     * Handles the removal of a Plant Description Entry.
     *
     * @param entry The entry that has been removed.
     */
    @Override
    public Future<Void> onPlantDescriptionRemoved(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected entry.");

        if (!entry.active()) {
            return Future.done();
        }

        return deleteRules(entry.id());
    }

}
package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.*;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.RuleStore;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.RuleStoreException;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionUpdateListener;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoWritable;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;

import java.net.InetSocketAddress;
import java.util.*;

public class OrchestratorClient implements PlantDescriptionUpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorClient.class);

    private final HttpClient client;
    private final InetSocketAddress orchestratorAddress;
    private final RuleStore ruleStore;
    private final PlantDescriptionTracker pdTracker;
    private final RuleCreator ruleCreator;
    private PlantDescriptionEntry activeEntry = null;

    /**
     * Class constructor.
     *
     * @param httpClient Object for sending HTTP messages to the Orchestrator.
     * @param ruleStore  Object providing permanent storage for Orchestration
     *                   rule data.
     * @param pdTracker  Object used for keeping track of Plant Descriptions.
     */
    public OrchestratorClient(
        HttpClient httpClient,
        RuleStore ruleStore,
        PlantDescriptionTracker pdTracker,
        InetSocketAddress orchestratorAddress
    ) {

        Objects.requireNonNull(httpClient, "Expected HttpClient");
        Objects.requireNonNull(ruleStore, "Expected backing store");
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
        Objects.requireNonNull(orchestratorAddress, "Expected Orchestrator address");

        this.client = httpClient;
        this.ruleStore = ruleStore;
        this.pdTracker = pdTracker;

        this.orchestratorAddress = orchestratorAddress;
        this.ruleCreator = new RuleCreator(pdTracker);
    }

    /**
     * Initializes the Orchestrator client.
     *
     * @return A {@code Future} which will complete once Orchestrator rules have
     * been created for the connections in the active Plant Description
     * entry (if any).
     */
    public Future<Void> initialize() {
        pdTracker.addListener(this);
        activeEntry = pdTracker.activeEntry();

        return deleteActiveRules()
            .flatMap(deletionResult -> (activeEntry == null) ? Future.done() : postRules().flatMap(createdRules -> {
                ruleStore.setRules(createdRules.getIds());
                logger.info("Created rules for Plant Description Entry '" + activeEntry.plantDescription() + "'.");
                return Future.done();
            }));
    }

    /**
     * Posts Orchestrator rules for the given Plant Description Entry.
     * <p>
     * For each connection in the given entry, a corresponding rule is posted to the
     * Orchestrator.
     *
     * @return A Future which will contain a list of the created rules.
     */
    private Future<StoreEntryListDto> postRules() {

        List<DtoWritable> rules = ruleCreator.createRules();

        if (rules.size() == 0) {
            return Future.success(emptyRuleList());
        }

        return client
            .send(orchestratorAddress,
                new HttpClientRequest().method(HttpMethod.POST)
                    .uri("/orchestrator/store/flexible")
                    .body(DtoEncoding.JSON, rules)
                    .header("accept", "application/json"))
            .flatMap(response -> response.bodyAsClassIfSuccess(DtoEncoding.JSON, StoreEntryListDto.class));
    }

    /**
     * @return An empty {@code StoreEntryListDto}.
     */
    private StoreEntryListDto emptyRuleList() {
        return new StoreEntryListBuilder().count(0).build();
    }

    /**
     * Deletes a single Orchestrator Store Entry.
     *
     * @param id The ID of an Orchestrator Store Entry to delete.
     * @return A Future that performs the deletion.
     */
    private Future<Void> deleteRule(int id) {
        return client
            .send(orchestratorAddress,
                new HttpClientRequest().method(HttpMethod.DELETE)
                    .uri("/orchestrator/store/flexible/" + id))
            .flatMap(response -> {
                if (response.status() != HttpStatus.OK) {
                    // TODO: Throw some other type of Exception.
                    return Future.failure(new RuntimeException("Failed to delete store rule with ID " + id));
                }
                return Future.done();
            });
    }

    /**
     * Deletes all orchestrator rules created by the Orchestrator client.
     *
     * @return A Future that performs the deletions.
     */
    private Future<Void> deleteActiveRules() {

        Set<Integer> rules;
        try {
            rules = ruleStore.readRules();
        } catch (RuleStoreException e) {
            return Future.failure(e);
        }

        if (rules.isEmpty()) {
            return Future.done();
        }

        // Delete any rules previously created by the Orchestrator client:
        var deletions = new ArrayList<Future<Void>>();

        for (int rule : rules) {
            deletions.add(deleteRule(rule));
        }

        return Futures.serialize(deletions)
            .flatMap(result -> {
                ruleStore.removeAll();
                logger.info("Deleted all orchestrator rules created by the Orchestrator client.");
                return Future.done();
            });
    }

    /**
     * Logs the fact that the specified entry has been activated.
     *
     * @param entry    A Plant Description Entry.
     * @param ruleList The list of Orchestrator rules connected to the entry.
     */
    private void logEntryActivated(PlantDescriptionEntry entry, StoreEntryList ruleList) {
        Objects.requireNonNull(entry, "Expected Plant Description Entry");

        String entryName = entry.plantDescription();

        if (ruleList.count() > 0) {
            String msg = "Orchestrator rules created for Plant Description '" + entryName + "': [";
            List<String> ids = new ArrayList<>();
            for (var rule : ruleList.data()) {
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
     * @param entry The updated entry.
     */
    @Override
    public void onPlantDescriptionUpdated(PlantDescriptionEntry entry) {

        int numConnections = pdTracker.getActiveConnections().size();
        boolean wasDeactivated = !entry.active() && activeEntry != null && activeEntry.id() == entry.id();
        boolean shouldPostRules = entry.active() && numConnections > 0;
        boolean shouldDeleteCurrentRules = entry.active() || wasDeactivated;

        final Future<Void> deleteRulesTask = shouldDeleteCurrentRules ? deleteActiveRules() : Future.done();

        final Future<StoreEntryListDto> postRulesTask = shouldPostRules ? postRules() : Future.success(emptyRuleList());

        deleteRulesTask.flatMap(result -> postRulesTask)
            .ifSuccess(createdRules -> {
                if (entry.active()) {
                    activeEntry = entry;
                    ruleStore.setRules(createdRules.getIds());
                    logEntryActivated(entry, createdRules);

                } else if (wasDeactivated) {
                    activeEntry = null;
                    logger.info("Deactivated Plant Description '" + entry.plantDescription() + "'");
                }
            })
            .onFailure(throwable -> logger.error(
                "Encountered an error while handling the new Plant Description '" + entry.plantDescription() + "'",
                throwable)
            );
    }

    /**
     * Handles the addition of a new Plant Description Entry.
     *
     * @param entry The added entry.
     */
    @Override
    public void onPlantDescriptionAdded(PlantDescriptionEntry entry) {
        onPlantDescriptionUpdated(entry);
    }

    /**
     * Handles the removal of a Plant Description Entry.
     *
     * @param entry The entry that has been removed.
     */
    @Override
    public void onPlantDescriptionRemoved(PlantDescriptionEntry entry) {

        // If the removed Plant Description was not active, there is no more
        // work to be done.
        if (activeEntry == null || activeEntry.id() != entry.id()) {
            return;
        }

        // Otherwise, all of its Orchestration rules should be deleted:
        deleteActiveRules()
            .ifSuccess(result -> logger.info("Deleted all Orchestrator rules belonging to Plant Description Entry '"
                + entry.plantDescription() + "'"))
            .onFailure(throwable -> logger.error(
                "Encountered an error while attempting to delete Plant Description '" + entry.plantDescription() + "'",
                throwable));
    }

}
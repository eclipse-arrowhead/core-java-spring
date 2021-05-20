package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemListDto;
import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import eu.arrowhead.core.plantdescriptionengine.utils.RetryFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Object used to keep track of registered Arrowhead systems.
 */
public class SystemTracker {
    private static final Logger logger = LoggerFactory.getLogger(SystemTracker.class);
    // List of instances that need to be informed when systems are added or
    // removed from the service registry.
    private final List<SystemUpdateListener> listeners = new ArrayList<>();
    private final String SYSTEMS_URI = "/serviceregistry/pull-systems";
    private final HttpClient httpClient;
    private final InetSocketAddress serviceRegistryAddress;
    private final int pollInterval;
    private List<SrSystem> systems = Collections.emptyList();
    private boolean initialized;

    /**
     * Class constructor
     *
     * @param httpClient             Object for communicating with the Service
     *                               Registry.
     * @param serviceRegistryAddress Address of the Service Registry.
     * @param pollInterval           Time between each system poll request sent
     *                               to the Service registry, in milliseconds.
     */
    public SystemTracker(
        final HttpClient httpClient,
        final InetSocketAddress serviceRegistryAddress,
        final int pollInterval
    ) {

        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(serviceRegistryAddress, "Expected service registry address");

        this.httpClient = httpClient;
        this.serviceRegistryAddress = serviceRegistryAddress;
        this.pollInterval = pollInterval;
    }

    /**
     * @return A Future which will complete with a list of registered systems.
     * <p>
     * The retrieved systems are stored locally, and can be accessed using
     * {@code #getSystems()}, {@code #getSystem(String} or
     * {@code #getSystem(String, Map<String>, <String>)
     * }.
     */
    private Future<Void> fetchSystems() {
        return httpClient
            .send(serviceRegistryAddress,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(SYSTEMS_URI)
                    .header("accept", "application/json"))
            .flatMap(response -> response.bodyToIfSuccess(SrSystemListDto::decodeJson))
            .flatMap(systemList -> {
                List<SrSystem> oldSystems = Collections.unmodifiableList(systems);
                systems = Collections.unmodifiableList(systemList.data());

                initialized = true;
                notifyListeners(oldSystems, systems);
                return Future.done();
            });
    }

    /**
     * Informs all registered listeners of which systems have been added or
     * removed from the service registry since the last refresh.
     *
     * @param oldSystems Systems that were previously present in the Service
     *                   Registry.
     * @param newSystems Systems that are present in the Service Registry.
     */
    private void notifyListeners(final List<? extends SrSystem> oldSystems, final List<? extends SrSystem> newSystems) {
        // Report removed systems
        for (final SrSystem oldSystem : oldSystems) {
            final boolean stillPresent = newSystems.stream()
                .anyMatch(newSystem -> newSystem.systemName().equals(oldSystem.systemName()));
            if (!stillPresent) {
                for (final SystemUpdateListener listener : listeners) {
                    logger.info("System '" + oldSystem.systemName() + "' has been removed from the Service Registry.");
                    listener.onSystemRemoved(oldSystem);
                }
            }
        }

        // Report added systems
        for (final SrSystem newSystem : newSystems) {
            final boolean wasPresent = oldSystems.stream()
                .anyMatch(oldSystem -> newSystem.systemName().equals(oldSystem.systemName()));
            if (!wasPresent) {
                logger.info("System '" + newSystem.systemName() + "' detected in Service Registry.");
                for (final SystemUpdateListener listener : listeners) {
                    listener.onSystemAdded(newSystem);
                }
            }
        }
    }

    /**
     * Registers another object to be notified whenever a system is added or
     * removed.
     *
     * @param listener Object to notify.
     */
    public void addListener(final SystemUpdateListener listener) {
        Objects.requireNonNull(listener, "Expected listener.");
        listeners.add(listener);
    }

    /**
     * Retrieves the specified system. Note that the returned data will be stale
     * if the system in question has changed state since the last call to {@link
     * #fetchSystems()}. If no match is found, null is returned.
     *
     * @param systemName Name of a system.
     * @param metadata   Metadata describing a system.
     * @return The desired system, if it is present in the local cache.
     */

    public SrSystem getSystem(final String systemName, final Map<String, String> metadata) {
        Objects.requireNonNull(systemName, "Expected system name.");
        Objects.requireNonNull(metadata, "Expected metadata.");

        if (!initialized) {
            throw new IllegalStateException("SystemTracker has not been initialized.");
        }

        // TODO: Throw an Exception if more than one match is found?
        for (final SrSystem system : systems) {
            if (systemName.equals(system.systemName()) && Metadata.isSubset(metadata, system.metadata())) {
                return system;
            }
        }

        return null;
    }

    /**
     * Retrieves the specified system. Note that the returned data will be stale
     * if the system in question has changed state since the last call to {@link
     * #fetchSystems()}. If no match is found, null is returned.
     *
     * @param systemName Name of a system.
     * @return The desired system, if it is present in the local cache.
     */

    public SrSystem getSystem(final String systemName) {
        return getSystem(systemName, Collections.emptyMap());
    }

    /**
     * @return All systems stored by this instance.
     */
    public List<SrSystem> getSystems() {
        return systems;
    }

    /**
     * Starts polling the Service Registry for registered systems.
     *
     * @return A Future that completes after the first reply from the Service
     * Registry has been stored.
     */
    public Future<Void> start() {
        final int retryDelayMillis = 15000;
        final int maxRetries = 3;
        final String retryMessage = "Failed to connect to Service registry, retrying in "
            + retryDelayMillis / 1000 +
            " seconds.";

        final RetryFuture retrier = new RetryFuture(retryDelayMillis, maxRetries, retryMessage);

        return retrier.run(this::fetchSystems)
            .flatMap(result -> {
                startSystemFetchLoop();
                return Future.done();
            });
    }

    private void startSystemFetchLoop() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                fetchSystems()
                    .onFailure(error -> logger.error(
                        "Failed to retrieve registered systems",
                        error));
            }
        }, pollInterval, pollInterval);
    }

}
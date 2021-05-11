package eu.arrowhead.core.plantdescriptionengine.utils;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemUpdateListener;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// TODO: Find a better way to mock the system tracker, this class
// is too convoluted.

/**
 * Subclass of SystemTracker used for testing purposes.
 */
public class MockSystemTracker extends SystemTracker {

    private final List<SystemUpdateListener> listeners = new ArrayList<>();
    private final List<SrSystem> systems = new ArrayList<>();

    public MockSystemTracker(final HttpClient httpClient, final InetSocketAddress serviceRegistryAddress) {
        super(
            Objects.requireNonNull(httpClient, "Expected HTTP client"),
            Objects.requireNonNull(serviceRegistryAddress, "Expected Service Registry address"),
            5000
        );
    }

    /**
     * Adds a new system to the system tracker.
     *
     * @param system The system to add.
     */
    public void addSystem(final SrSystem system) {
        Objects.requireNonNull(system, "Expected system");

        systems.add(system);

        for (final SystemUpdateListener listener : listeners) {
            listener.onSystemAdded(system);
        }
    }

    @Override
    public void addListener(final SystemUpdateListener listener) {
        Objects.requireNonNull(listener, "Expected listener.");
        listeners.add(listener);
    }

    /**
     * Return true if the specified system can be described by the given system
     * name and metadata combination.
     *
     * @param system     An Arrowhead system.
     * @param systemName Name of a system.
     * @param metadata   Metadata describing a system.
     */
    private boolean systemMatches(final SrSystem system, String systemName, Map<String, String> metadata) {
        if (systemName != null && !systemName.equals(system.systemName())) {
            return false;
        }

        return (metadata == null || Metadata.isSubset(metadata, system.metadata()));
    }

    /**
     * If a system with the specified system name and metadata is present in the
     * system tracker, it is removed. Any registered
     * {@code SystemUpdateListeners} are notified.
     *
     * @param systemName name of the system to remove. May be null if {@code
     *                   metadata} is present.
     * @param metadata   Metadata of the system to remove. May be null if {@code
     *                   systemName} is present.
     * @throws IllegalArgumentException If the specified system cannot be found.
     */
    public void remove(final String systemName, final Map<String, String> metadata) {

        if (systemName == null && metadata == null) {
            throw new IllegalArgumentException("Either systemName or metadata must be present.");
        }

        List<SrSystem> matchingSystems = systems.stream()
            .filter(system -> systemMatches(system, systemName, metadata))
            .collect(Collectors.toList());

        if (matchingSystems.isEmpty()) {
            throw new IllegalArgumentException("System not present in the System Tracker.");
        }

        for (SrSystem system : matchingSystems) {
            systems.remove(system);
            for (final SystemUpdateListener listener : listeners) {
                listener.onSystemRemoved(system);
            }
        }
    }

    /**
     * Removes the system with the given system name from the system tracker.
     *
     * @param systemName name of the system to remove.
     */
    public void remove(final String systemName) {
        Objects.requireNonNull(systemName, "Expected system name.");
        remove(systemName, null);
    }

    @Override
    public List<SrSystem> getSystems() {
        return systems;
    }

    @Override
    public SrSystem getSystem(final String systemName, final Map<String, String> metadata) {
        Objects.requireNonNull(systemName, "Expected system name.");
        Objects.requireNonNull(metadata, "Expected metadata.");

        for (final SrSystem system : systems) {
            if (systemName.equals(system.systemName()) && Metadata.isSubset(metadata, system.metadata())) {
                return system;
            }
        }

        return null;
    }

    @Override
    public SrSystem getSystem(final String systemName) {
        return getSystem(systemName, Collections.emptyMap());
    }

}
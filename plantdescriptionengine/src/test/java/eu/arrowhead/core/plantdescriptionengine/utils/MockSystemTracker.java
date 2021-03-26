package eu.arrowhead.core.plantdescriptionengine.utils;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemUpdateListener;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

/**
 * Subclass of SystemTracker used for testing purposes.
 */
public class MockSystemTracker extends SystemTracker {

    public MockSystemTracker(final HttpClient httpClient, final InetSocketAddress serviceRegistryAddress) {
        super(
            Objects.requireNonNull(httpClient, "Expected HTTP client"),
            Objects.requireNonNull(serviceRegistryAddress, "Expected Service Registry address")
        );
        initialized = true;
    }

    /**
     * Adds a new system to the system tracker.
     *
     * @param system The system to add.
     */
    public void addSystem(final SrSystem system) {
        Objects.requireNonNull(system, "Expected system");
        systems.put(toKey(system), system);
        for (final SystemUpdateListener listener : listeners) {
            listener.onSystemAdded(system);
        }
    }

    /**
     * If a system with the specified system name and metadata is present in the
     * system tracker, it is removed.
     *
     * @param systemName name of the system to remove. May be null if {@code
     *                   metadata} is present.
     * @param metadata   Metadata of the system to remove. May be null if {@code
     *                   systemName} is present.
     */
    public void remove(final String systemName, final Map<String, String> metadata) {
        if (systemName == null && metadata == null) {
            throw new IllegalArgumentException("Either systemName or metadata must be present.");
        }
        final SrSystem system = systems.remove(toKey(systemName, metadata));
        if (system == null) {
            throw new IllegalArgumentException("System not present in the System Tracker.");
        }
        for (final SystemUpdateListener listener : listeners) {
            listener.onSystemRemoved(system);
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
}
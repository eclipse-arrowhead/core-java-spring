package eu.arrowhead.core.plantdescriptionengine.utils;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Subclass of SystemTracker used for testing purposes.
 */
public class MockSystemTracker extends SystemTracker {

    public MockSystemTracker(HttpClient httpClient, InetSocketAddress serviceRegistryAddress) {
        super(httpClient, serviceRegistryAddress);
        initialized = true;
    }

    /**
     * Adds a new system to the system tracker.
     *
     * @param system The system to add.
     */
    public void addSystem(SrSystem system) {
        systems.put(toKey(system), system);
        for (var listener : listeners) {
            listener.onSystemAdded(system);
        }
    }

    /**
     * If a system with the specified system name and metadata is present in the
     * system tracker, it is removed.
     *
     * @param systemName name of the system to remove.
     * @param metadata   Metadata of the system to remove.
     */
    public void remove(String systemName, Map<String, String> metadata) {
        SrSystem system = systems.remove(toKey(systemName, metadata));
        if (system == null) {
            throw new IllegalArgumentException("System '" + systemName + "' is not present in the System Tracker.");
        }
        for (var listener : listeners) {
            listener.onSystemRemoved(system);
        }
    }

    /**
     * Removes the system with the given system name from the system tracker.
     *
     * @param systemName name of the system to remove.
     */
    public void remove(String systemName) {
        remove(systemName, null);
    }
}
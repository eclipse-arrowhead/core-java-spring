package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import se.arkalix.description.ServiceDescription;
import se.arkalix.dto.json.value.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Object used for keeping track of inventory data of monitorable systems.
 */
public class MonitorInfo {

    private final Map<String, Bundle> infoBundles = new ConcurrentHashMap<>();

    /**
     * @param service An Arrowhead Framework service, as provided by a local or
     *                remote system.
     * @return A unique identifier for the given service.
     */
    private String getKey(ServiceDescription service) {
        return service.provider().name() + service.uri();
    }

    /**
     * Store the inventory ID of a given service. Any inventory ID previously stored
     * for this system will be overwritten.
     *
     * @param service     An Arrowhead Framework service.
     * @param inventoryId An inventory ID.
     */
    public void putInventoryId(ServiceDescription service, String inventoryId) {
        String systemName = service.provider().name();
        Map<String, String> metadata = service.metadata();
        String key = getKey(service);
        Bundle oldBundle = infoBundles.get(key);
        Bundle newBundle;
        if (oldBundle == null) {
            newBundle = new Bundle(systemName, service.name(), metadata, null, inventoryId);
        } else {
            newBundle = new Bundle(systemName, service.name(), metadata, oldBundle.systemData, inventoryId);
        }
        infoBundles.put(key, newBundle);
    }

    /**
     * Store system data for the given service. Any system data previously stored
     * for this system will be overwritten.
     *
     * @param service An Arrowhead Framework service.
     * @param data    System data to be stored.
     */
    public void putSystemData(ServiceDescription service, JsonObject data) {
        String key = getKey(service);
        String systemName = service.provider().name();
        Map<String, String> metadata = service.metadata();
        Bundle oldBundle = infoBundles.get(key);
        Bundle newBundle;

        if (oldBundle == null) {
            newBundle = new Bundle(systemName, service.name(), metadata, data, null);
        } else {
            newBundle = new Bundle(systemName, service.name(), metadata, data, oldBundle.inventoryId);
        }
        infoBundles.put(key, newBundle);
    }

    /**
     * @param systemName Name of an Arrowhead Framework system, or null.
     * @param metadata   Metadata describing an Arrowhead Framework system, or null.
     * @return A list containing all stored monitor data for the system that
     * corresponds to the given arguments.
     */
    public List<Bundle> getSystemInfo(String systemName, Map<String, String> metadata) {
        List<Bundle> result = new ArrayList<>();
        for (var bundle : infoBundles.values()) {
            if (systemName != null && systemName.equals(bundle.systemName)) {
                result.add(bundle);
            } else if (metadata != null && Metadata.isSubset(metadata, bundle.metadata)) {
                result.add(bundle);
            }
        }
        return result;
    }

    public static class Bundle {
        public final JsonObject systemData;
        public final String serviceDefinition;
        public final Map<String, String> metadata;
        public final String inventoryId;
        public final String systemName;

        Bundle(String systemName, String serviceDefinition, Map<String, String> metadata, JsonObject systemData,
               String inventoryId) {
            this.systemData = systemData;
            this.serviceDefinition = serviceDefinition;
            this.inventoryId = inventoryId;
            this.systemName = systemName;
            this.metadata = metadata;
        }

        /**
         * Returns true if the given arguments match this instance's metadata.
         * <p>
         * More specifically, returns true if {@code portMetadata} is present, and the
         * union of {@code systemMetadata} and {@code portMetadata} is a subset of this
         * instance's metadata.
         *
         * @param systemMetadata Metadata relating to a particular system (read from a
         *                       system in a Plant Description Entry).
         * @param portMetadata   Metadata relating to a particular service (read from
         *                       one of the ports of a system in a Plant Description
         *                       Entry).
         */
        public boolean matchesPortMetadata(Map<String, String> systemMetadata, Map<String, String> portMetadata) {
            if (portMetadata == null || portMetadata.size() == 0) {
                return false;
            }

            var mergedMetadata = Metadata.merge(systemMetadata, portMetadata);
            return Metadata.isSubset(mergedMetadata, metadata);
        }

        /**
         * Returns true if the given arguments match this instances metadata.
         * <p>
         * More specifically, returns true if {@code systemMetadata} is not present, or
         * is a superset of this instance's metadata.
         *
         * @param systemMetadata Metadata relating to a particular system (read from a
         *                       system in a Plant Description Entry).
         */
        public boolean matchesSystemMetadata(Map<String, String> systemMetadata) {
            if (systemMetadata == null) {
                return true;
            }
            return Metadata.isSubset(metadata, systemMetadata);
        }

    }

}
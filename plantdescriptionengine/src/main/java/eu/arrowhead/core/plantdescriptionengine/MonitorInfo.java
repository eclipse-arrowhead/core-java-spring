package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import se.arkalix.description.ServiceDescription;
import se.arkalix.dto.json.value.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private String getKey(final ServiceDescription service) {
        return service.provider().name() + service.uri();
    }

    /**
     * Store the inventory ID of a given service. Any inventory ID previously
     * stored for this system will be overwritten.
     *
     * @param service     An Arrowhead Framework service.
     * @param inventoryId An inventory ID.
     */
    public void putInventoryId(final ServiceDescription service, final String inventoryId) {
        Objects.requireNonNull(service, "Expected service");
        Objects.requireNonNull(service, "Expected inventory ID");
        final String systemName = service.provider().name();
        final Map<String, String> metadata = service.metadata();
        final String key = getKey(service);
        final Bundle oldBundle = infoBundles.get(key);
        final Bundle newBundle;
        if (oldBundle == null) {
            newBundle = new Bundle(systemName, service.name(), metadata, null, inventoryId);
        } else {
            newBundle = new Bundle(systemName, service.name(), metadata, oldBundle.systemData, inventoryId);
        }
        infoBundles.put(key, newBundle);
    }

    /**
     * Store system data for the given service. Any system data previously
     * stored for this system will be overwritten.
     *
     * @param service An Arrowhead Framework service.
     * @param data    System data to be stored.
     */
    public void putSystemData(final ServiceDescription service, final JsonObject data) {
        Objects.requireNonNull(service, "Expected service");
        Objects.requireNonNull(data, "Expected system data");

        final String key = getKey(service);
        final String systemName = service.provider().name();
        final Map<String, String> metadata = service.metadata();
        final Bundle oldBundle = infoBundles.get(key);
        final Bundle newBundle;

        if (oldBundle == null) {
            newBundle = new Bundle(systemName, service.name(), metadata, data, null);
        } else {
            newBundle = new Bundle(systemName, service.name(), metadata, data, oldBundle.inventoryId);
        }
        infoBundles.put(key, newBundle);
    }

    /**
     * @param systemName Name of an Arrowhead Framework system. May be null if
     *                   {@code metadata} is present.
     * @param metadata   Metadata describing an Arrowhead Framework system. May
     *                   be null if {@code systemName} is present.
     * @return A list containing all stored monitor data for the system that
     * corresponds to the given arguments.
     */
    public List<Bundle> getSystemInfo(final String systemName, final Map<String, String> metadata) {

        if (systemName == null && metadata == null) {
            throw new IllegalArgumentException("Either system name or metadata must be present.");
        }

        final List<Bundle> result = new ArrayList<>();
        for (final Bundle bundle : infoBundles.values()) {
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

        Bundle(final String systemName, final String serviceDefinition, final Map<String, String> metadata, final JsonObject systemData,
               final String inventoryId) {
            this.systemData = systemData;
            this.serviceDefinition = serviceDefinition;
            this.inventoryId = inventoryId;
            this.systemName = systemName;
            this.metadata = metadata;
        }

        /**
         * Returns true if the given arguments match this instance's metadata.
         * <p>
         * More specifically, returns true if {@code portMetadata} is present,
         * and the union of {@code systemMetadata} and {@code portMetadata} is a
         * subset of this instance's metadata.
         *
         * @param systemMetadata Metadata relating to a particular system (read
         *                       from a system in a Plant Description Entry).
         *                       May be null.
         * @param portMetadata   Metadata relating to a particular service (read
         *                       from one of the ports of a system in a Plant
         *                       Description Entry). May be null.
         */
        public boolean matchesPortMetadata(final Map<String, String> systemMetadata, final Map<String, String> portMetadata) {
            if (portMetadata == null || portMetadata.isEmpty()) {
                return false;
            }

            final Map<String, String> mergedMetadata = Metadata.merge(systemMetadata, portMetadata);
            return Metadata.isSubset(mergedMetadata, metadata);
        }

        /**
         * Returns true if the given arguments match this instances metadata.
         * <p>
         * More specifically, returns true if {@code systemMetadata} is not
         * present, or is a superset of this instance's metadata.
         *
         * @param systemMetadata Metadata relating to a particular system (read
         *                       from a system in a Plant Description Entry).
         */
        public boolean matchesSystemMetadata(final Map<String, String> systemMetadata) {
            if (systemMetadata == null) {
                return true;
            }
            return Metadata.isSubset(metadata, systemMetadata);
        }

    }

}
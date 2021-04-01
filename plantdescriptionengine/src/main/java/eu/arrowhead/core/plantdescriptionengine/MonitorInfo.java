package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.codec.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
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
     * @param service An Arrowhead Framework service.
     * @return A unique identifier for the given service.
     */
    private String toKey(final ServiceRecord service) {

        Map<String, String> metadata = service.provider().metadata();
        String result = "name=" + service.provider().name() +
            ",serviceUri=" + service.uri();

        if (metadata != null) {
            result += ",metadata=" + Metadata.toString(metadata);
        }

        return result + "}";
    }

    /**
     * Store the inventory ID of a given service. Any inventory ID previously
     * stored for this system will be overwritten.
     *
     * @param service     An Arrowhead Framework service.
     * @param inventoryId An inventory ID.
     */
    public void putInventoryId(final ServiceRecord service, final String inventoryId) {

        Objects.requireNonNull(service, "Expected service");
        Objects.requireNonNull(service, "Expected inventory ID");

        final String systemName = service.provider().name();
        final Map<String, String> serviceNetadata = service.metadata();
        final Map<String, String> systemMetadata = service.provider().metadata();

        final String key = toKey(service);
        final Bundle oldBundle = infoBundles.get(key);
        final JsonObject systemData = (oldBundle == null) ? null : oldBundle.systemData;

        final Bundle newBundle = new Bundle(
            systemName,
            service.name(),
            systemMetadata,
            serviceNetadata,
            systemData,
            inventoryId
        );

        infoBundles.put(key, newBundle);
    }

    /**
     * Store system data for the given service. Any system data previously
     * stored for this system will be overwritten.
     *
     * @param service An Arrowhead Framework service.
     * @param data    System data to be stored.
     */
    public void putSystemData(final ServiceRecord service, final JsonObject data) {

        Objects.requireNonNull(service, "Expected service");
        Objects.requireNonNull(data, "Expected system data");

        final String key = toKey(service);
        final SystemRecord provider = service.provider();
        final String systemName = provider.name();

        final Map<String, String> systemMetadata = provider.metadata();
        final Map<String, String> serviceMetadata = service.metadata();
        final String serviceDefinition = service.name();
        final Bundle oldBundle = infoBundles.get(key);
        final String inventoryId = (oldBundle == null) ? null : oldBundle.inventoryId;

        final Bundle newBundle = new Bundle(
            systemName,
            serviceDefinition,
            systemMetadata,
            serviceMetadata,
            data,
            inventoryId
        );

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
            final boolean namesMatch = systemName == null || systemName.equals(bundle.systemName);
            final boolean metadataMatch = bundle.matchesSystemMetadata(metadata);
            if (namesMatch && metadataMatch) {
                result.add(bundle);
            }
        }
        return result;
    }

    public static class Bundle {
        public final JsonObject systemData;
        public final String serviceDefinition;
        public final Map<String, String> systemMetadata;
        public final Map<String, String> serviceMetadata;
        public final String inventoryId;
        public final String systemName;

        Bundle(
            final String systemName,
            final String serviceDefinition,
            final Map<String, String> systemMetadata,
            final Map<String, String> serviceMetadata,
            final JsonObject systemData,
            final String inventoryId
        ) {
            this.systemData = systemData;
            this.serviceDefinition = serviceDefinition;
            this.inventoryId = inventoryId;
            this.systemName = systemName;
            this.serviceMetadata = serviceMetadata;
            this.systemMetadata = systemMetadata;
        }

        /**
         * Returns true if the given arguments match this instances metadata.
         * <p>
         * More specifically, returns true if {@code metadata} is not present,
         * or is a superset of this instance's system metadata.
         *
         * @param metadata Metadata relating to a particular system (read from a
         *                 system in a Plant Description Entry).
         */
        public boolean matchesSystemMetadata(final Map<String, String> metadata) {
            if (metadata == null || metadata.isEmpty()) {
                return true;
            }
            if (systemMetadata == null || systemMetadata.isEmpty()) {
                return false;
            }
            return Metadata.isSubset(metadata, systemMetadata);
        }

    }

}
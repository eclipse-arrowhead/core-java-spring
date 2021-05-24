package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import se.arkalix.codec.json.JsonObject;

import java.util.Map;

public class MonitorInfo {
    public final JsonObject systemData;
    public final String serviceDefinition;
    public final Map<String, String> systemMetadata;
    public final Map<String, String> serviceMetadata;
    public final String inventoryId;
    public final String systemName;

    MonitorInfo(
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
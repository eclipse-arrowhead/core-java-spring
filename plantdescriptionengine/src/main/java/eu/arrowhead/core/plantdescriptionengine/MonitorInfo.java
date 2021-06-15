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

    private boolean matchesSystemMetadata(final Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return true;
        }
        if (systemMetadata == null || systemMetadata.isEmpty()) {
            return false;
        }
        return Metadata.isSubset(metadata, systemMetadata);
    }

    public boolean matches(final String systemName, final Map<String, String> metadata) {
        if (systemName != null && !systemName.equals(this.systemName)) {
            return false;
        }
        return matchesSystemMetadata(metadata);

    }
}
package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.value.JsonObject;

import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Data Transfer Object (DTO) interface for plant description port entries.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface PortEntry {

    String portName();

    String serviceDefinition();

    Optional<String> serviceInterface();

    Optional<Map<String, String>> metadata();

    Optional<JsonObject> systemData();

    Optional<String> inventoryId();

    Optional<Map<String, String>> inventoryData();

    /**
     * Indicates whether this port is used to consume or produce services.
     */
    Optional<Boolean> consumer();
}

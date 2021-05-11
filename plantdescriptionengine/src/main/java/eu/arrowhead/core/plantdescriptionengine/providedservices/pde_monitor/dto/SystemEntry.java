package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.codec.json.JsonObject;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface SystemEntry {

    String systemId();

    Optional<String> systemName();

    Map<String, String> metadata();

    List<PortEntry> ports();

    Optional<JsonObject> systemData();

    Optional<String> inventoryId();

    Optional<JsonObject> inventoryData();

}
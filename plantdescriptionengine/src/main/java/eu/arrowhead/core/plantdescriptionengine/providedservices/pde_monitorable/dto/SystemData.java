package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto;

import se.arkalix.codec.json.JsonObject;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for System data.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface SystemData {
    Optional<JsonObject> data();
}

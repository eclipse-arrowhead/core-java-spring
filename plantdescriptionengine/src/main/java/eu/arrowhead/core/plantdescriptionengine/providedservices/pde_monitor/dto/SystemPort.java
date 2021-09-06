package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for representing one side (the consumer
 * or producer) of a plant description connection.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface SystemPort {

    String systemId();

    String portName();

}

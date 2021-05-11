package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for plant description system ports.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface Port {

    String portName();

    String serviceDefinition();

    Optional<String> serviceInterface();

    Map<String, String> metadata();

    /**
     * Indicates whether this port is used to consume or produce services.
     * When false or not present, the port is assumed to be a producer.
     */
    Optional<Boolean> consumer();
}

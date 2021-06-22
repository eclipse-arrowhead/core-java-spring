package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for store entry.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface ServiceRegistryEntry {
    Integer id();

    ServiceDefinition serviceDefinition();

    SrSystem provider();

    String serviceUri();

    Optional<String> endOfValidity();

    String secure();

    Map<String, String> metadata();

    Integer version();

    List<ServiceInterface> interfaces();

    String createdAt();

    String updatedAt();

}

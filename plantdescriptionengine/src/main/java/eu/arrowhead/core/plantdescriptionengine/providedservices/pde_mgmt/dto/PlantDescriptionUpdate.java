package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Data Transfer Object (DTO) interface for plant descriptions.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
public interface PlantDescriptionUpdate {

    Optional<String> plantDescription();

    Optional<Boolean> active();

    Optional<List<PdeSystem>> systems();

    Optional<List<Connection>> connections();

    Optional<List<Integer>> include();

}
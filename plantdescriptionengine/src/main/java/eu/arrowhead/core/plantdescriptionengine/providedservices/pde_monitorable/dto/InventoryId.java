package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoWritableAs;

import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for Inventory IDs.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
public interface InventoryId {
    Optional<String> id();
}

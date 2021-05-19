package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for lists of plant descriptions.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface PlantDescriptionEntryList {

    List<MonitorPlantDescriptionEntry> data();

    int count();
}

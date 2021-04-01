package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.List;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for plant descriptions.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface MonitorPlantDescriptionEntry {

    int id();

    String plantDescription();

    boolean active();

    List<Integer> include();

    List<SystemEntry> systems();

    List<Connection> connections();

    Instant createdAt();

    Instant updatedAt();

}
package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for lists of store entries.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface StoreEntryList {

    List<StoreEntry> data();

    int count();

    default Set<Integer> getIds() {
        return data().stream()
            .map(StoreEntry::id)
            .collect(Collectors.toSet());
    }
}

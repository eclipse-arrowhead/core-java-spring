package eu.arrowhead.core.plantdescriptionengine.providedservices.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.Objects;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for error messages.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface ErrorMessage {
    static ErrorMessageDto of(final String message) {
        Objects.requireNonNull(message, "Expected message.");
        return new ErrorMessageDto.Builder().error(message).build();
    }

    String error();

}

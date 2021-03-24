package eu.arrowhead.core.plantdescriptionengine.providedservices.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Data Transfer Object (DTO) interface for error messages.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface ErrorMessage {
    static ErrorMessageDto of(String message) {
        return new ErrorMessageBuilder().error(message).build();
    }

    String error();

}

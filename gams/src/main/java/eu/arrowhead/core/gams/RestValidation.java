package eu.arrowhead.core.gams;

import java.util.Objects;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import org.apache.http.HttpStatus;

public class RestValidation {
    public static final String GAMS_IS_NULL_ERROR = "Gams instance must not be null";

    public static final String PAYLOAD_IS_NULL_ERROR = "Payload must not be null";
    public static final String PAYLOAD_IS_EMPTY_ERROR = "Payload must not be empty";
    public static final String TIMESTAMP_IS_EMPTY_ERROR = "Timestamp must not be empty";

    public void verify(final CreateInstanceRequest dto, final String origin) {
        if (Objects.isNull(dto)) {
            throw new BadPayloadException(PAYLOAD_IS_NULL_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        } else if (Utilities.isEmpty(dto.getName())) {
            throw new BadPayloadException(PAYLOAD_IS_EMPTY_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final CreateSensorRequest dto, final String origin) {
        if (Objects.isNull(dto)) {
            throw new BadPayloadException(PAYLOAD_IS_NULL_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        } else if (Utilities.isEmpty(dto.getName())) {
            throw new BadPayloadException(PAYLOAD_IS_EMPTY_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final PublishSensorDataRequest dto, final String origin) {
        if (Objects.isNull(dto)) {
            throw new BadPayloadException(PAYLOAD_IS_NULL_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        } else if (Objects.isNull(dto.getData())) {
            throw new BadPayloadException(PAYLOAD_IS_EMPTY_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyEquals(final GamsInstance aInstance, final GamsInstance bInstance, final String origin) {
        if (Objects.isNull(aInstance)) {
            throw new BadPayloadException("Sensor is missing Gams instance", HttpStatus.SC_BAD_REQUEST, origin);
        } else if (Objects.isNull(bInstance)) {
            throw new BadPayloadException(GAMS_IS_NULL_ERROR, HttpStatus.SC_BAD_REQUEST, origin);
        } else if (!Objects.equals(aInstance, bInstance)) {
            throw new BadPayloadException("Sensor belongs to a different Gams instance", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}

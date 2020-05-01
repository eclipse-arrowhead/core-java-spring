package eu.arrowhead.core.mscv;

import java.util.Objects;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.dto.shared.mscv.TargetDto;
import eu.arrowhead.common.dto.shared.mscv.TargetLoginRequest;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.mscv.http.ClientExecutionRequest;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.ExampleMatcher;

public class Validation {

    public static final String ID_NULL_ERROR_MESSAGE = "Id must not be null";
    public static final String PAYLOAD_NULL_ERROR_MESSAGE = "Payload must not be null";
    public static final String PAGE_NULL_ERROR_MESSAGE = "Page must not be null";
    public static final String EXAMPLE_NULL_ERROR_MESSAGE = "Example must not be null";
    public static final String LAYER_NULL_ERROR_MESSAGE = "Layer must not be null";

    public static final String CREDENTIALS_NULL_ERROR_MESSAGE = "Credentials must not be null/empty";
    public static final String CREDENTIALS_INVALID_ERROR_MESSAGE = "Credentials must be Base64 encoded in the form <user>:<password>";

    public static final String TARGET_NULL_ERROR_MESSAGE = "Target must not be null";
    public static final String TARGET_EMPTY_ERROR_MESSAGE = "Target must have value";
    public static final String LOGIN_TARGET_NOT_FOUND = "Target not found";

    public static final String NAME_NULL_ERROR_MESSAGE = "Name must not be null";
    public static final String NAME_EMPTY_ERROR_MESSAGE = "Name must have value";

    public static final String USERNAME_NULL_ERROR_MESSAGE = "Username must not be null";
    public static final String USERNAME_EMPTY_ERROR_MESSAGE = "Username must have value";

    public static final String OS_NULL_ERROR_MESSAGE = "OS must not be null";
    public static final String OS_EMPTY_ERROR_MESSAGE = "OS must have value";

    public static final String ADDRESS_NULL_ERROR_MESSAGE = "Address must not be null";
    public static final String ADDRESS_EMPTY_ERROR_MESSAGE = " Address must have value";

    public static final String PORT_NULL_ERROR_MESSAGE = "Port must not be null";
    public static final String PORT_EMPTY_ERROR_MESSAGE = "Port must have value";
    public static final String PORT_INVALID_ERROR_MESSAGE = "Port must be positive number between 1 - 65535";

    public static final String LIST_NULL_ERROR_MESSAGE = "List must not be null";
    public static final String LIST_EMPTY_ERROR_MESSAGE = "List must not be empty";


    private final Logger logger = LogManager.getLogger();

    public Validation() { super(); }


    public void verify(final ClientExecutionRequest dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        if (Objects.isNull(dto)) {
            throw new BadPayloadException(PAYLOAD_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Objects.isNull(dto.getLayer())) {
            throw new BadPayloadException(LAYER_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        verify(dto.getTarget(), origin);
    }

    public void verify(final SshTargetDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        verify((TargetDto) dto, origin);
        verifyAddress(dto.getAddress(), origin);
        verifyPort(dto.getPort(), origin);
    }

    public void verify(final TargetDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        if (Objects.isNull(dto)) {
            throw new BadPayloadException(TARGET_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Utilities.isEmpty(dto.getName())) {
            throw new BadPayloadException(NAME_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Objects.isNull(dto.getOs())) {
            throw new BadPayloadException(OS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final TargetLoginRequest request, final String origin) {
        logger.debug("verify({},{}) started...", request, origin);
        verify(request.getTarget(), origin);
        if(Utilities.isEmpty(request.getCredentials())) {
            throw new BadPayloadException(CREDENTIALS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyAddress(final String address, final String origin) {
        if (Utilities.isEmpty(address)) {
            throw new BadPayloadException(ADDRESS_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyPort(final Integer port, final String origin) {
        if (Objects.isNull(port)) {
            throw new BadPayloadException(PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (port <= CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException(PORT_INVALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyCredentials(final String[] credentials, final String origin) {
        if(Objects.isNull(credentials)) {
            throw new BadPayloadException(CREDENTIALS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if(credentials.length != 2) {
            throw new BadPayloadException(CREDENTIALS_INVALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public ExampleMatcher exampleMatcher(final ExampleMatcher.MatchMode mode) {

        final ExampleMatcher retValue;
        switch (mode) {
            case ALL:
                retValue = ExampleMatcher.matchingAll();
                break;
            case ANY:
                retValue = ExampleMatcher.matchingAny();
                break;
            default:
                retValue = ExampleMatcher.matching();
        }

        return retValue.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                       .withIgnoreCase()
                       .withIgnoreNullValues();
    }

}

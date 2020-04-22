package eu.arrowhead.core.mscv;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.exception.BadPayloadException;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.ExampleMatcher;

import java.util.Objects;

public class Validation {

    private static final String NAME_NULL_ERROR_MESSAGE = "Name must have value";
    private static final String OS_NULL_ERROR_MESSAGE = "OS must have value";
    private static final String ADDRESS_NULL_ERROR_MESSAGE = " Address must have value";
    private static final String PORT_NULL_ERROR_MESSAGE = "Port must have value";
    private static final String PORT_INVALID_ERROR_MESSAGE = "Port must be positive number between 1 - 65535";

    private final Logger logger = LogManager.getLogger();

    public Validation() { super(); }

    public void verify(final SshTargetDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        if (Utilities.isEmpty(dto.getName())) {
            throw new BadPayloadException(NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Objects.isNull(dto.getOs())) {
            throw new BadPayloadException(OS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        verifyAddress(dto.getAddress(), origin);
        verifyPort(dto.getPort(), origin);
    }

    public void verifyAddress(final String address, final String origin) {
        if (Utilities.isEmpty(address)) {
            throw new BadPayloadException(ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
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

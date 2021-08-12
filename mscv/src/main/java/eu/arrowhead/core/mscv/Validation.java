package eu.arrowhead.core.mscv;

import java.util.Objects;
import java.util.regex.Pattern;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.mscv.CategoryDto;
import eu.arrowhead.common.dto.shared.mscv.ClientExecutionRequest;
import eu.arrowhead.common.dto.shared.mscv.DomainDto;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.MipDto;
import eu.arrowhead.common.dto.shared.mscv.MipIdentifierDto;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.ScriptRequestDto;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.dto.shared.mscv.StandardDto;
import eu.arrowhead.common.dto.shared.mscv.TargetDto;
import eu.arrowhead.common.dto.shared.mscv.TargetLoginRequest;
import eu.arrowhead.common.exception.BadPayloadException;
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

    public static final String MIP_NULL_ERROR_MESSAGE = "MIP must not be null";
    public static final String MIP_IDENTIFIER_NULL_ERROR_MESSAGE = "MIP identifier not be null";
    public static final String MIP_IDENTIFIER_FORMAT_ERROR_MESSAGE = "MIP identifier must be in the format: [CATEGORY ABBREVIATION]-[EXTERNAL ID]. e.g. IAC-1";
    public static final Pattern MIP_IDENTIFIER_PATTERN = Pattern.compile("(\\D+)-(\\d+)");

    public static final String CATEGORY_NULL_ERROR_MESSAGE = "Category must not be null";
    public static final String STANDARD_NULL_ERROR_MESSAGE = "Standard must not be null";
    public static final String DOMAIN_NULL_ERROR_MESSAGE = "Domain must not be null";

    public static final String SCRIPT_NULL_ERROR_MESSAGE = "Script must not be null";
    public static final String SCRIPT_CONTENT_NULL_ERROR_MESSAGE = "Script content must not be null";
    public static final String PATH_NULL_ERROR_MESSAGE = "Path must not be empty";

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

    public static final String ABBR_EMPTY_ERROR_MESSAGE = "Abbreviation must have value";

    private final Logger logger = LogManager.getLogger();

    public Validation() { super(); }


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


    public void verify(final MipDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        verifyPayload(dto, origin);
        verify(dto.getCategory(), origin);
        verify(dto.getDomain(), origin);
        verify(dto.getStandard(), origin);
        verifyName(dto.getName(), origin);
        if (Objects.isNull(dto.getExtId())) {
            throw new BadPayloadException(ID_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final MipIdentifierDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        verifyPayload(dto, origin);
        if (Objects.isNull(dto.getExtId())) {
            throw new BadPayloadException(ID_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Objects.isNull(dto.getCategoryAbbreviation())) {
            throw new BadPayloadException(ABBR_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final ScriptRequestDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        if (Objects.isNull(dto)) {
            throw new BadPayloadException(SCRIPT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        verify(dto.getMip(), origin);
        verifyOs(dto.getOs(), origin);
        verifyLayer(dto.getLayer(), origin);
    }

    public void verify(final DomainDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        verifyPayload(dto, origin);
        if (Objects.isNull(dto.getName())) {
            throw new BadPayloadException(DOMAIN_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final ClientExecutionRequest dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        verifyPayload(dto, origin);
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
        verifyOs(dto.getOs(), origin);
    }

    public void verify(final TargetLoginRequest request, final String origin) {
        logger.debug("verify({},{}) started...", request, origin);
        verifyPayload(request, origin);
        verify(request.getTarget(), origin);
        if (Utilities.isEmpty(request.getCredentials())) {
            throw new BadPayloadException(CREDENTIALS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final StandardDto standard, final String origin) {
        logger.debug("verify({},{}) started...", standard, origin);
        if (Objects.isNull(standard)) {
            throw new BadPayloadException(STANDARD_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Objects.isNull(standard.getName())) {
            throw new BadPayloadException(NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Objects.isNull(standard.getIdentification())) {
            throw new BadPayloadException(ID_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verify(final CategoryDto dto, final String origin) {
        logger.debug("verify({},{}) started...", dto, origin);
        verifyPayload(dto, origin);
        if (Utilities.isEmpty(dto.getName())) {
            throw new BadPayloadException(NAME_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Utilities.isEmpty(dto.getAbbreviation())) {
            throw new BadPayloadException(ABBR_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyId(final Long mipId, final String origin) {
        logger.debug("verifyId({},{}) started...", mipId, origin);
        if (Objects.isNull(mipId)) {
            throw new BadPayloadException(ID_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyExtId(final Integer mipId, final String origin) {
        logger.debug("verifyId({},{}) started...", mipId, origin);
        if (Objects.isNull(mipId)) {
            throw new BadPayloadException(ID_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyLayer(final Layer layer, final String origin) {
        logger.debug("verifyLayer({},{}) started...", layer, origin);
        if (Objects.isNull(layer)) {
            throw new BadPayloadException(OS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyOs(final OS os, final String origin) {
        logger.debug("verifyOs({},{}) started...", os, origin);
        if (Objects.isNull(os)) {
            throw new BadPayloadException(OS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyAddress(final String address, final String origin) {
        logger.debug("verifyAddress({},{}) started...", address, origin);
        if (Utilities.isEmpty(address)) {
            throw new BadPayloadException(ADDRESS_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyName(final String name, final String origin) {
        logger.debug("verifyName({},{}) started...", name, origin);
        if (Utilities.isEmpty(name)) {
            throw new BadPayloadException(NAME_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyAbbreviation(final String name, final String origin) {
        logger.debug("verifyAbbreviation({},{}) started...", name, origin);
        if (Utilities.isEmpty(name)) {
            throw new BadPayloadException(ABBR_EMPTY_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyIdentifier(final String identifier, final String origin) {
        logger.debug("verifyIdentifier({},{}) started...", identifier, origin);
        if (Utilities.isEmpty(identifier)) {
            throw new BadPayloadException(MIP_IDENTIFIER_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (!MIP_IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new BadPayloadException(MIP_IDENTIFIER_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyPort(final Integer port, final String origin) {
        logger.debug("verifyPort({},{}) started...", port, origin);
        if (Objects.isNull(port)) {
            throw new BadPayloadException(PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (port <= CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException(PORT_INVALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyCredentials(final String[] credentials, final String origin) {
        logger.debug("verifyCredentials({},{}) started...", credentials, origin);
        if (Objects.isNull(credentials)) {
            throw new BadPayloadException(CREDENTIALS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (credentials.length != 2) {
            throw new BadPayloadException(CREDENTIALS_INVALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public <T> void verifyPayload(final T payload, final String origin) {
        logger.debug("verifyPayload({},{}) started...", payload, origin);
        if (Objects.isNull(payload)) {
            throw new BadPayloadException(PAYLOAD_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    public void verifyIdentification(final String identification, final String origin) {
        logger.debug("verifyIdentification({},{}) started...", identification, origin);
        if (Utilities.isEmpty(identification)) {
            throw new BadPayloadException(STANDARD_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}

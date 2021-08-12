package eu.arrowhead.core.mscv.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.Part;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.MipDomain;
import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.Standard;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.ScriptListResponseDto;
import eu.arrowhead.common.dto.shared.mscv.ScriptRequestDto;
import eu.arrowhead.common.dto.shared.mscv.ScriptResponseDto;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.mscv.Constants;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.ScriptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static eu.arrowhead.core.mscv.Constants.PARAMETER_IDENTIFICATION;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_DOMAIN;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_IDENTIFIER;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_IDENTIFIER_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_LAYER;
import static eu.arrowhead.core.mscv.Constants.PATH_MIP_IDENTIFIER;
import static eu.arrowhead.core.mscv.Constants.PATH_OS;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, Constants.SWAGGER_TAG_SCRIPT_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ScriptMgmtController {

    private static final String MIP_IDENTIFIER_DESCRIPTION = "Identifier in the form: &lt;category abbreviation&gt;-&lt;external id&gt;";

    private static final String SCRIPT_URI = "/script";
    public static final String QUALIFY_SCRIPT_URI = SCRIPT_URI +
            PATH_MIP_IDENTIFIER + PARAMETER_MIP_IDENTIFIER_PATH +
            PATH_OS + PARAMETER_OS_PATH +
            PATH_LAYER + PARAMETER_LAYER_PATH;
    private static final String UPLOAD_URI = "/upload";
    private static final String UPLOAD_SCRIPT_URI = CoreCommonConstants.MGMT_URI + SCRIPT_URI + UPLOAD_URI;
    private static final String UPLOAD_SCRIPT_DESCRIPTION = "Upload new MSCV script";
    private static final String UPLOAD_SCRIPT_SUCCESS = "New MSCV script created";
    private static final String UPLOAD_SCRIPT_EXISTS = "MSCV exists already";
    private static final String UPLOAD_SCRIPT_BAD_REQUEST = "Unable to create new MSCV script";

    private static final String READ_SCRIPT_URI = CoreCommonConstants.MGMT_URI + QUALIFY_SCRIPT_URI;
    private static final String READ_SCRIPT_DESCRIPTION = "Get MSCV script by mip id, layer and os";
    private static final String READ_SCRIPT_SUCCESS = "MSCV script returned";
    private static final String READ_SCRIPT_NOT_FOUND = "MSCV script not found";
    private static final String READ_SCRIPT_BAD_REQUEST = "Unable to return MSCV script";

    private static final String READ_SCRIPT_CONTENT_URI = QUALIFY_SCRIPT_URI;
    private static final String READ_SCRIPT_CONTENT_MGMT_URI = CoreCommonConstants.MGMT_URI + QUALIFY_SCRIPT_URI;
    private static final String READ_SCRIPT_CONTENT_DESCRIPTION = "Get MSCV script content by mip identifier, layer and os";
    private static final String READ_SCRIPT_CONTENT_SUCCESS = "MSCV script returned";
    private static final String READ_SCRIPT_CONTENT_NOT_FOUND = "MSCV script not found";
    private static final String READ_SCRIPT_CONTENT_BAD_REQUEST = "Unable to return MSCV script";

    private static final String READ_ALL_SCRIPT_URI = CoreCommonConstants.MGMT_URI + SCRIPT_URI;
    private static final String READ_ALL_SCRIPT_DESCRIPTION = "Get all MSCV scripts";
    private static final String READ_ALL_SCRIPT_SUCCESS = "All MSCV scripts returned";
    private static final String READ_ALL_SCRIPT_BAD_REQUEST = "Unable to return MSCV scripts";

    private static final String UPDATE_SCRIPT_URI = CoreCommonConstants.MGMT_URI + QUALIFY_SCRIPT_URI;
    private static final String UPDATE_SCRIPT_DESCRIPTION = "Update MSCV script";
    private static final String UPDATE_SCRIPT_SUCCESS = "MSCV script updated";
    private static final String UPDATE_SCRIPT_NOT_FOUND = "MSCV script not found";
    private static final String UPDATE_SCRIPT_BAD_REQUEST = "Unable to update MSCV script";

    private static final String UPDATE_SCRIPT_CONTENT_URI = CoreCommonConstants.MGMT_URI + QUALIFY_SCRIPT_URI + UPLOAD_URI;
    private static final String UPDATE_SCRIPT_CONTENT_DESCRIPTION = "Update MSCV script";
    private static final String UPDATE_SCRIPT_CONTENT_SUCCESS = "MSCV script updated";
    private static final String UPDATE_SCRIPT_CONTENT_NOT_FOUND = "MSCV script not found";
    private static final String UPDATE_SCRIPT_CONTENT_BAD_REQUEST = "Unable to update MSCV script";

    private static final String DELETE_SCRIPT_URI = CoreCommonConstants.MGMT_URI + QUALIFY_SCRIPT_URI;
    private static final String DELETE_SCRIPT_DESCRIPTION = "Delete MSCV script";
    private static final String DELETE_SCRIPT_SUCCESS = "MSCV script deleted";
    private static final String DELETE_SCRIPT_BAD_REQUEST = "Unable to delete MSCV script";

    private final Logger logger = LogManager.getLogger();
    private final String parentPath;
    private final ScriptService service;
    private final Validation validation;

    @Autowired
    public ScriptMgmtController(final MscvDefaults mscvDefaults, final ScriptService service) {
        super();
        this.parentPath = mscvDefaults.getDefaultPath();
        this.service = service;
        this.validation = new Validation();
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPLOAD_SCRIPT_DESCRIPTION, response = ScriptResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPLOAD_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPLOAD_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_CONFLICT, message = UPLOAD_SCRIPT_EXISTS, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(value = UPLOAD_SCRIPT_URI)
    @ResponseBody
    public ScriptResponseDto create(@RequestPart("file") final MultipartFile file,
                                    @ApiParam(value = "Example: {\"layer\": \"DEVICE\", \"mip\": {\"categoryAbbreviation\": \"IAC\", \"extId\": 1 },   " +
                                            "\"os\": \"LINUX\" }", type = "eu.arrowhead.common.dto.shared.mscv.ScriptRequestDto")
                                    @RequestPart(name = "dto", required = true) final Part part)  {
        logger.debug("create started ...");
        final String origin = createMgmtOrigin(UPLOAD_SCRIPT_URI);
        final ByteArrayOutputStream byteArray;
        final ObjectMapper mapper = new ObjectMapper();
        final ScriptRequestDto dto;

        try(final InputStream inputStream = part.getInputStream()) {
            dto = mapper.readValue(inputStream, ScriptRequestDto.class);
        } catch(final Exception e) {
            throw new ArrowheadException("Unable to convert Json DTO to ScriptRequestDto.class", HttpStatus.SC_BAD_REQUEST, e);
        }

        validation.verify(dto, origin);
        byteArray = new ByteArrayOutputStream();

        try (final InputStream inputStream = file.getInputStream()) {
            StreamUtils.copy(inputStream, byteArray);
        } catch (IOException e) {
            throw new ArrowheadException("Error during file upload", HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }

        final Script uploadedScript = service.create(byteArray, dto.getMip().getCategoryAbbreviation(),
                                                     dto.getMip().getExtId(), dto.getLayer(), dto.getOs());
        return MscvDtoConverter.convert(uploadedScript);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_SCRIPT_DESCRIPTION, response = ScriptResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_SCRIPT_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_SCRIPT_URI)
    @ResponseBody
    public ScriptResponseDto read(@ApiParam(value = MIP_IDENTIFIER_DESCRIPTION)
                                  @PathVariable(PARAMETER_MIP_IDENTIFIER) final String identifier,
                                  @PathVariable(PARAMETER_OS) final Layer layer,
                                  @PathVariable(PARAMETER_LAYER) final OS os) {
        logger.debug("read started ...");
        final Script script = findScript(identifier, layer, os, createMgmtOrigin(READ_SCRIPT_URI));
        return convert(script);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_SCRIPT_CONTENT_DESCRIPTION, response = ByteArrayResource.class, tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_SCRIPT_CONTENT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_SCRIPT_CONTENT_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_SCRIPT_CONTENT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(value = READ_SCRIPT_CONTENT_URI, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> readContent(@ApiParam(value = MIP_IDENTIFIER_DESCRIPTION)
                                                @PathVariable(PARAMETER_MIP_IDENTIFIER) final String identifier,
                                                @PathVariable(PARAMETER_OS) final Layer layer,
                                                @PathVariable(PARAMETER_LAYER) final OS os) {
        logger.debug("readContent started ...");
        return readContentMgmt(identifier, layer, os);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_SCRIPT_CONTENT_DESCRIPTION, response = ByteArrayResource.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_SCRIPT_CONTENT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_SCRIPT_CONTENT_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_SCRIPT_CONTENT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(value = READ_SCRIPT_CONTENT_MGMT_URI, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> readContentMgmt(@ApiParam(value = MIP_IDENTIFIER_DESCRIPTION)
                                                    @PathVariable(PARAMETER_MIP_IDENTIFIER) final String identifier,
                                                    @PathVariable(PARAMETER_OS) final Layer layer,
                                                    @PathVariable(PARAMETER_LAYER) final OS os) {
        logger.debug("readContentMgmt started ...");
        final String origin = createMgmtOrigin(READ_SCRIPT_CONTENT_URI);
        final Script script = findScript(identifier, layer, os, origin);

        try {

            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=img.jpg");
            header.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            header.add(HttpHeaders.PRAGMA, "no-cache");
            header.add(HttpHeaders.EXPIRES, "0");

            final File file;

            if (Files.exists(Path.of(script.getPhysicalPath()))) {
                file = Path.of(script.getPhysicalPath()).toFile();
            } else if (Files.exists(Path.of(parentPath, script.getPhysicalPath()))) {
                file = Path.of(parentPath, script.getPhysicalPath()).toFile();
            } else {
                throw notFoundException("Script content", origin).get();
            }

            final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
            return ResponseEntity.status(HttpStatus.SC_OK)
                                 .headers(header)
                                 .contentLength(file.length())
                                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                 .body(resource);
        } catch (IOException e) {
            throw new ArrowheadException("Unknown IO Error", HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_SCRIPT_DESCRIPTION, response = ScriptListResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_SCRIPT_URI)
    @ResponseBody
    public ScriptListResponseDto readAll(
            @ApiParam(value = "Filter for standard identification. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_IDENTIFICATION, required = false) final String identification,
            @ApiParam(value = "Filter for domain. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_MIP_DOMAIN, required = false) final String domainName,
            @ApiParam(value = "Filter for layer. Exact match.")
            @RequestParam(name = PARAMETER_LAYER, required = false) final Layer layer,
            @ApiParam(value = "Filter for operating system. Exact match.")
            @RequestParam(name = PARAMETER_OS, required = false) final OS os,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, createMgmtOrigin(READ_ALL_SCRIPT_URI));

        final Script probe = new Script();
        probe.setLayer(layer);
        probe.setOs(os);

        final Mip mip = new Mip();
        if (Objects.nonNull(identification)) {
            final Standard std = new Standard();
            std.setIdentification(identification);
            mip.setStandard(std);
        }
        if (Objects.nonNull(domainName)) {
            final MipDomain domain = new MipDomain(domainName);
            mip.setDomain(domain);
        }
        probe.setMip(mip);

        final Example<Script> example = Example.of(probe, ExampleMatcher.matchingAll());
        final Page<Script> scripts = service.pageByExample(example, pageParameters.createPageable(sortField));
        return new ScriptListResponseDto(scripts.map(this::convert));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_SCRIPT_DESCRIPTION, response = ScriptResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = UPDATE_SCRIPT_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_SCRIPT_URI)
    @ResponseBody
    public ScriptResponseDto update(@ApiParam(value = MIP_IDENTIFIER_DESCRIPTION)
                                    @PathVariable(PARAMETER_MIP_IDENTIFIER) final String identifier,
                                    @PathVariable(PARAMETER_OS) final Layer layer,
                                    @PathVariable(PARAMETER_LAYER) final OS os,
                                    @RequestBody final ScriptRequestDto dto) {
        logger.debug("update started ...");
        final String origin = createMgmtOrigin(UPDATE_SCRIPT_URI);
        final Script oldScript = findScript(identifier, layer, os, origin);
        final Script newScript = service.replace(oldScript, MscvDtoConverter.convert(dto));
        return convert(newScript);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_SCRIPT_CONTENT_DESCRIPTION, response = ScriptResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_SCRIPT_CONTENT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = UPDATE_SCRIPT_CONTENT_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_SCRIPT_CONTENT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(value = UPDATE_SCRIPT_CONTENT_URI, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ScriptResponseDto updateContent(@ApiParam(value = MIP_IDENTIFIER_DESCRIPTION)
                                           @PathVariable(PARAMETER_MIP_IDENTIFIER) final String identifier,
                                           @PathVariable(PARAMETER_OS) final Layer layer,
                                           @PathVariable(PARAMETER_LAYER) final OS os,
                                           @RequestParam("file") MultipartFile file) {
        logger.debug("update started ...");
        final String origin = createMgmtOrigin(UPDATE_SCRIPT_CONTENT_URI);
        final Script script = findScript(identifier, layer, os, origin);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (final InputStream inputStream = file.getInputStream()) {
            StreamUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new ArrowheadException("Error during file upload", HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }

        final Script uploadedScript = service.replace(script, outputStream);
        return convert(uploadedScript);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_SCRIPT_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_SCRIPT_URI)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@ApiParam(value = MIP_IDENTIFIER_DESCRIPTION)
                       @PathVariable(PARAMETER_MIP_IDENTIFIER) final String identifier,
                       @PathVariable(PARAMETER_OS) final Layer layer,
                       @PathVariable(PARAMETER_LAYER) final OS os) {
        logger.debug("delete started ...");
        final String origin = createMgmtOrigin(DELETE_SCRIPT_URI);
        validation.verifyIdentifier(identifier, origin);
        validation.verifyOs(os, origin);
        validation.verifyLayer(layer, origin);

        service.delete(identifier, layer, os);
    }

    private Script findScript(final String identifier, final Layer layer, final OS os, final String origin) {
        logger.debug("findScript started ...");
        validation.verifyIdentifier(identifier, origin);
        validation.verifyOs(os, origin);
        validation.verifyLayer(layer, origin);

        final Optional<Script> optionalScript = service.findScriptFor(identifier, layer, os);
        return optionalScript.orElseThrow(notFoundException("Script", origin));
    }

    private ScriptResponseDto convert(final Script script) {
        final ScriptResponseDto dto = MscvDtoConverter.convert(script);
        final String uri = service.createUriPath(script);
        dto.setContentUri(uri);

        return dto;
    }

    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }
}

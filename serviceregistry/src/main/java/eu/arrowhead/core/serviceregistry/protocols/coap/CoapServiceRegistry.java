package eu.arrowhead.core.serviceregistry.protocols.coap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.coap.AhCoapServer;
import eu.arrowhead.common.coap.configuration.CoapCertificates;
import eu.arrowhead.common.coap.configuration.CoapCredentials;
import eu.arrowhead.common.coap.configuration.CoapServerConfiguration;
import eu.arrowhead.common.coap.tools.CoapTools;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

import java.time.format.DateTimeParseException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoapServiceRegistry {

    // =================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(CoapServiceRegistry.class);
    
    private AhCoapServer coapServer;

    @Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
    private boolean gatekeeperIsPresent;

    @Autowired
    private ServiceRegistryDBService serviceRegistryDBService;

    @Autowired
    private CommonNamePartVerifier cnVerifier;

    @Value(CoreCommonConstants.$COAP_SERVER_ADDRESS_ENABLED)
    private boolean coapServerEnabled;

    @Value(CoreCommonConstants.$COAP_SERVER_ADDRESS)
    private String coapServerAddress;

    @Value(CoreCommonConstants.$COAP_SERVER_PORT)
    private int coapServerPort;

    @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
    private boolean serverSslEnabled;

    @Value(CommonConstants.$KEYSTORE_TYPE_WD)
    private String keyStoreType;

    @Value(CommonConstants.$KEYSTORE_PATH_WD)
    private String keyStorePath;

    @Value(CommonConstants.$KEYSTORE_PASSWORD_WD)
    private String keyStorePassword;

    @Value(CommonConstants.$KEY_PASSWORD_WD)
    private String keyPassword;

    @Value(CommonConstants.$TRUSTSTORE_PATH_WD)
    private String trustStorePath;

    @Value(CommonConstants.$TRUSTSTORE_PASSWORD_WD)
    private String trustStorePassword;

    @Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
    private boolean useStrictServiceDefinitionVerifier;

    final String DIRECTION_KEY = "direction";
    final String DIRECTION_DEFAULT = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE;
    final String NULL_DEFAULT = null;
    final String PAGE_KEY = "page";
    final int PAGE_DEFAULT = -1;
    final String ID_KEY = "id";
    final int ID_DEFAULT = 0;
    final String SIZE_KEY = "size";
    final int SIZE_DEFAULT = -1;
    final String SORT_KEY = "sortField";
    final String SORT_DEFAULT = CoreCommonConstants.COMMON_FIELD_NAME_ID;
    final String UNREGISTER_SERVICE_DEFINITION_KEY = "service_definition";
    final String UNREGISTER_SERVICE_PROVIDER_ADDRESS_KEY = "address";
    final String UNREGISTER_SERVICE_PROVIDER_PORT_KEY = "port";
    final String UNREGISTER_SERVICE_PROVIDER_SYSTEM_NAME_KEY = "system_name";

    private static final String SERVICE_DEFINITION_REQUIREMENT_WRONG_FORMAT_ERROR_MESSAGE = "Service definition requirement has invalid format. Service definition only contains maximum 63 character of letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";
    private static final String SYSTEM_NAME_NULL_ERROR_MESSAGE = " System name must have value ";
    private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain maximum 63 character of letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";
    private static final String SYSTEM_PORT_NULL_ERROR_MESSAGE = " System port must have value ";

    final String URL_PATH_ALL = "all";
    final String URL_PATH_GROUPED = "grouped";
    final String URL_PATH_ID = "id";
    final String URL_PATH_MGMT = "mgmt";
    final String URL_PATH_MULTI = "multi";
    final String URL_PATH_PULL_SYSTEMS = "pull-systems";
    final String URL_PATH_QUERY = "query";
    final String URL_PATH_REGISTER = "register";
    final String URL_PATH_REGISTER_SYSTEM = "register-system";
    final String URL_PATH_SERVICE_DEFINITION = "servicedef";
    final String URL_PATH_SERVICES = "services";
    final String URL_PATH_SYSTEM = "system";
    final String URL_PATH_SYSTEMS = "systems";
    final String URL_PATH_UNREGISTER = "unregister";
    final String URL_PATH_UNREGISTER_SYSTEM = "unregister-system";

    // =================================================================================================
    // methods
    // -------------------------------------------------------------------------------------------------
    @PostConstruct
    public void init() {
        logger.info("CoAP Protocol Init");

        if (coapServerEnabled) {
            logger.info("CoAP Protocol Enabled");
            CoapServerConfiguration coapServerConfiguration = new CoapServerConfiguration(
                    coapServerAddress,
                    coapServerPort,
                    serverSslEnabled,
                    new CoapCredentials(
                            keyStorePath,
                            keyStorePassword,
                            keyPassword,
                            "serviceregistry-coap"),
                    new CoapCertificates(
                            "coap-root",
                            trustStorePassword,
                            trustStorePath));
            coapServer = new AhCoapServer(coapServerConfiguration);
            initializateResources();
            coapServer.start();
        } else {
            logger.info("CoAP Protocol Disabled");
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (coapServerEnabled) {
            logger.info("Killing CoAP Server");
            coapServer.stop();
        }
    }

    // =================================================================================================
    // assistant methods
    // -------------------------------------------------------------------------------------------------//-------------------------------------------------------------------------------------------------
    private void initializateResources() {
        coapServer.add(new EchoResource());
        coapServer.add(new QueryResource());
        coapServer.add(new RegisterResource());
        coapServer.add(new RegisterSystemResource());
        coapServer.add(new UnregisterResource());
        coapServer.add(new UnregisterSystemResource());
    }

    // =================================================================================================
    // CoAP resources
    // -------------------------------------------------------------------------------------------------
    class EchoResource extends CoapResource {

        public EchoResource() {
            super("echo");
            getAttributes().setTitle("Echo Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(
                    ResponseCode.CONTENT,
                    "Got it!",
                    MediaTypeRegistry.TEXT_PLAIN);
        }

    }

    // -------------------------------------------------------------------------------------------------
    class QueryResource extends CoapResource {

        private final ObjectMapper mapper = new ObjectMapper();

        public QueryResource() {
            super(URL_PATH_QUERY);
            add(new QueryMulti());
            getAttributes().setTitle("Query Resource");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            try {
                ServiceQueryFormDTO serviceQueryFormDTO = mapper.readValue(exchange.getRequestText(),
                        ServiceQueryFormDTO.class);

                if (Utilities.isEmpty(serviceQueryFormDTO.getServiceDefinitionRequirement())) {
                    throw new Exception("Service definition requirement is null or blank");
                }
                exchange.respond(
                        ResponseCode.CONTENT,
                        mapper.writeValueAsString(serviceRegistryDBService.queryRegistry(serviceQueryFormDTO)),
                        MediaTypeRegistry.APPLICATION_JSON);
            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // -------------------------------------------------------------------------------------------------
    class QueryMulti extends CoapResource {

        private final ObjectMapper mapper = new ObjectMapper();

        public QueryMulti() {
            super(URL_PATH_MULTI);
            getAttributes().setTitle("Multi Query Resource");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            try {
                ServiceQueryFormListDTO forms = mapper.readValue(exchange.getRequestText(),
                        ServiceQueryFormListDTO.class);

                String badRequestTxt = checkQueryFormList(forms);
                if (badRequestTxt == null) {

                    exchange.respond(
                            ResponseCode.CONTENT,
                            mapper.writeValueAsString(serviceRegistryDBService.multiQueryRegistry(forms)),
                            MediaTypeRegistry.APPLICATION_JSON);
                } else {
                    exchange.respond(
                            ResponseCode.BAD_REQUEST,
                            badRequestTxt,
                            MediaTypeRegistry.TEXT_PLAIN);
                }

            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // -------------------------------------------------------------------------------------------------
    class RegisterResource extends CoapResource {

        private final ObjectMapper mapper = new ObjectMapper();

        public RegisterResource() {
            super(URL_PATH_REGISTER);
            getAttributes().setTitle("Register Resource");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            try {

                ServiceRegistryRequestDTO serviceRegistryRequestDTO = mapper.readValue(exchange.getRequestText(),
                        ServiceRegistryRequestDTO.class);

                if (Utilities.isEmpty(serviceRegistryRequestDTO.getServiceDefinition())) {
                    throw new Exception("Service definition is null or blank");
                }

                if (!Utilities.isEmpty(serviceRegistryRequestDTO.getEndOfValidity())) {
                    try {
                        Utilities.parseUTCStringToLocalZonedDateTime(
                                serviceRegistryRequestDTO.getEndOfValidity().trim());
                    } catch (final DateTimeParseException ex) {
                        throw new Exception(
                                "End of validity is specified in the wrong format. Please provide UTC time using pattern.");
                    }
                }

                ServiceSecurityType securityType = null;
                if (serviceRegistryRequestDTO.getSecure() != null) {
                    for (final ServiceSecurityType type : ServiceSecurityType.values()) {
                        if (type.name().equalsIgnoreCase(serviceRegistryRequestDTO.getSecure())) {
                            securityType = type;
                            break;
                        }
                    }

                    if (securityType == null) {
                        throw new Exception("Security type is not valid.");
                    }
                } else {
                    securityType = ServiceSecurityType.NOT_SECURE;
                }

                if (securityType != ServiceSecurityType.NOT_SECURE
                        && serviceRegistryRequestDTO.getProviderSystem().getAuthenticationInfo() == null) {
                    throw new Exception(
                            "Security type is in conflict with the availability of the authentication info.");
                }

                exchange.respond(
                        ResponseCode.CONTENT,
                        mapper.writeValueAsString(
                                serviceRegistryDBService.registerServiceResponse(serviceRegistryRequestDTO)),
                        MediaTypeRegistry.APPLICATION_JSON);
            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // -------------------------------------------------------------------------------------------------
    class RegisterSystemResource extends CoapResource {

        private final ObjectMapper mapper = new ObjectMapper();

        public RegisterSystemResource() {
            super(URL_PATH_REGISTER_SYSTEM);
            getAttributes().setTitle("Register System Resource");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            try {

                SystemRequestDTO dto = mapper.readValue(exchange.getRequestText(), SystemRequestDTO.class);
                callCreateSystem(exchange, dto);
            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // -------------------------------------------------------------------------------------------------
    class UnregisterResource extends CoapResource {

        public UnregisterResource() {
            super(URL_PATH_UNREGISTER);
            getAttributes().setTitle("Unegister Resource");
        }

        @Override
        public void handleDELETE(CoapExchange exchange) {
            try {
                Map<String, String> queries = CoapTools.getQueryParams(exchange);

                String serviceDefinition = CoapTools.getParam(queries, UNREGISTER_SERVICE_DEFINITION_KEY, NULL_DEFAULT);
                String providerName = CoapTools.getParam(queries, UNREGISTER_SERVICE_PROVIDER_SYSTEM_NAME_KEY,
                        NULL_DEFAULT);
                String providerAddress = CoapTools.getParam(queries, UNREGISTER_SERVICE_PROVIDER_ADDRESS_KEY,
                        NULL_DEFAULT);
                int providerPort = CoapTools.getParam(queries, UNREGISTER_SERVICE_PROVIDER_PORT_KEY, 0);

                if (Utilities.isEmpty(serviceDefinition)) {
                    throw new Exception("Service definition is blank");
                }

                if (Utilities.isEmpty(providerName)) {
                    throw new Exception("Name of the provider system is blank");
                }

                if (Utilities.isEmpty(providerAddress)) {
                    throw new Exception("Address of the provider system is blank");
                }

                if (providerPort < CommonConstants.SYSTEM_PORT_RANGE_MIN
                        || providerPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
                    throw new Exception("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and "
                            + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
                }
                exchange.respond(ResponseCode.VALID);
            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // -------------------------------------------------------------------------------------------------
    class UnregisterSystemResource extends CoapResource {


        public UnregisterSystemResource() {
            super(URL_PATH_UNREGISTER_SYSTEM);
            getAttributes().setTitle("Unegister Resource");
        }

        @Override
        public void handleDELETE(CoapExchange exchange) {
            try {
                Map<String, String> queries = CoapTools.getQueryParams(exchange);

                String systemName = CoapTools.getParam(queries,
                        CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, NULL_DEFAULT);
                String address = CoapTools.getParam(queries,
                        CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, NULL_DEFAULT);
                int port = CoapTools.getParam(queries, CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT,
                        0);

                try {

                    final String checkedAddress = checkUnregisterSystemParameters(systemName, address, port);
                    serviceRegistryDBService.removeSystemByNameAndAddressAndPort(systemName, checkedAddress, port);

                } catch (BadPayloadException ex) {
                    exchange.respond(
                            ResponseCode.BAD_REQUEST,
                            ex.getMessage(),
                            MediaTypeRegistry.TEXT_PLAIN);
                }

                exchange.respond(ResponseCode.VALID);
            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // =================================================================================================
    // assistant methods
    // -------------------------------------------------------------------------------------------------
    private String checkQueryFormList(final ServiceQueryFormListDTO forms) {

        if (forms == null || forms.getForms() == null || forms.getForms().isEmpty()) {
            return "Form list is null";
        }

        for (final ServiceQueryFormDTO form : forms.getForms()) {
            if (form == null) {
                return "A form is null";
            }

            if (Utilities.isEmpty(form.getServiceDefinitionRequirement())) {
                return "Service definition requirement is null or blank";
            }

            if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(form.getServiceDefinitionRequirement())) {
                return SERVICE_DEFINITION_REQUIREMENT_WRONG_FORMAT_ERROR_MESSAGE;
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------------------------------
    private void callCreateSystem(CoapExchange exchange, final SystemRequestDTO dto) {
        ObjectMapper mapper = new ObjectMapper();

        String badPayloadMessage = checkSystemRequest(dto, true);
        if (badPayloadMessage != null) {
            exchange.respond(
                    ResponseCode.BAD_REQUEST,
                    badPayloadMessage,
                    MediaTypeRegistry.APPLICATION_JSON);
            return;
        }

        final String systemName = dto.getSystemName().toLowerCase().trim();
        final String address = dto.getAddress().toLowerCase().trim();
        final int port = dto.getPort();
        final String authenticationInfo = dto.getAuthenticationInfo();
        final Map<String, String> metadata = dto.getMetadata();

        try {
            exchange.respond(
                    ResponseCode.CONTENT,
                    mapper.writeValueAsString(
                            serviceRegistryDBService.createSystemResponse(systemName, address, port, authenticationInfo,
                                    metadata)),
                    MediaTypeRegistry.APPLICATION_JSON);
        } catch (

        Exception ex) {
            exchange.respond(
                    ResponseCode.INTERNAL_SERVER_ERROR,
                    ex.getMessage(),
                    MediaTypeRegistry.TEXT_PLAIN);
        }
    }

    // -------------------------------------------------------------------------------------------------
    private String checkSystemRequest(final SystemRequestDTO dto,
            final boolean checkReservedCoreSystemNames) {

        if (dto == null) {
            return "System is null.";
        }

        if (Utilities.isEmpty(dto.getSystemName())) {
            return SYSTEM_NAME_NULL_ERROR_MESSAGE;
        }

        if (checkReservedCoreSystemNames) {
            for (final CoreSystem coreSysteam : CoreSystem.values()) {
                if (coreSysteam.name().equalsIgnoreCase(dto.getSystemName().trim())) {
                    return "System name '" + dto.getSystemName() + "' is a reserved arrowhead core system name.";
                }
            }
        }

        if (!cnVerifier.isValid(dto.getSystemName())) {
            return SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE;
        }

        if (dto.getPort() == null) {
            return SYSTEM_PORT_NULL_ERROR_MESSAGE;
        }

        final int validatedPort = dto.getPort();
        if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN
                || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            return "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and "
                    + CommonConstants.SYSTEM_PORT_RANGE_MAX;
        }

        return null;
    }

    // -------------------------------------------------------------------------------------------------
    private String checkUnregisterSystemParameters(final String systemName, final String address, final int port) {

        final String origin = CommonConstants.SERVICEREGISTRY_URI
                + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI;
        if (Utilities.isEmpty(systemName)) {
            throw new BadPayloadException("Name of the application system is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (!cnVerifier.isValid(systemName)) {
            throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and "
                    + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
        }

        return address;
    }
}
package eu.arrowhead.core.orchestrator.protocols.coap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.coap.AhCoapServer;
import eu.arrowhead.common.coap.configuration.CoapCertificates;
import eu.arrowhead.common.coap.configuration.CoapCredentials;
import eu.arrowhead.common.coap.configuration.CoapServerConfiguration;
import eu.arrowhead.common.coap.tools.CoapTools;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoapOrchestrator {
    // =================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(CoapOrchestrator.class);
    private AhCoapServer coapServer;

    @Value(CoreCommonConstants.$ORCHESTRATOR_USE_FLEXIBLE_STORE_WD)
    private boolean useFlexibleStore;

    @Autowired
    private OrchestratorService orchestratorService;

    @Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
    private boolean gatekeeperIsPresent;

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

    final String URL_PATH_ORCHESTRATION = "orchestration2";
    final String URL_PATH_ID = "id";

    private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
    private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
    private static final String GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE = " can not be served. Orchestrator runs in NO GATEKEEPER mode.";
    private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";

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
                            "orchestrator-coap"),
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
    // -------------------------------------------------------------------------------------------------
    private void initializateResources() {
        coapServer.add(new EchoResource());
        coapServer.add(new OrchestrationResource());
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
    class OrchestrationResource extends CoapResource {
        private final ObjectMapper mapper = new ObjectMapper();

        public OrchestrationResource() {
            super(URL_PATH_ORCHESTRATION);
            add(new OrchestrationIdResource());
            getAttributes().setTitle("Start Orchestration Process");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {

            try {
                OrchestrationFormRequestDTO request = mapper.readValue(exchange.getRequestText(),
                        OrchestrationFormRequestDTO.class);

                checkOrchestratorFormRequestDTO(request);

                if (request.getOrchestrationFlags().getOrDefault(Flag.EXTERNAL_SERVICE_REQUEST, false)) {
                    if (!gatekeeperIsPresent) {
                        throw new BadPayloadException(
                                "External service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE,
                                HttpStatus.SC_BAD_REQUEST, URL_PATH_ORCHESTRATION);
                    }

                    exchange.respond(
                            ResponseCode.CONTENT,
                            mapper.writeValueAsString(orchestratorService.externalServiceRequest(request)),
                            MediaTypeRegistry.APPLICATION_JSON);
                    return;

                } else if (request.getOrchestrationFlags().getOrDefault(Flag.TRIGGER_INTER_CLOUD, false)) {
                    if (!gatekeeperIsPresent) {
                        throw new BadPayloadException(
                                "Forced inter cloud service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE,
                                HttpStatus.SC_BAD_REQUEST, URL_PATH_ORCHESTRATION);
                    }

                    exchange.respond(
                            ResponseCode.CONTENT,
                            mapper.writeValueAsString(orchestratorService.triggerInterCloud(request)),
                            MediaTypeRegistry.APPLICATION_JSON);
                    return;
                } else if (!request.getOrchestrationFlags().getOrDefault(Flag.OVERRIDE_STORE, false)) {
                    exchange.respond(
                            ResponseCode.CONTENT,
                            mapper.writeValueAsString(orchestratorService.orchestrationFromStore(request)),
                            MediaTypeRegistry.APPLICATION_JSON);
                    return;
                } else {
                    exchange.respond(
                            ResponseCode.CONTENT,
                            mapper.writeValueAsString(orchestratorService.dynamicOrchestration(request, false)),
                            MediaTypeRegistry.APPLICATION_JSON);
                    return;
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
    class OrchestrationIdResource extends CoapResource {
        private final ObjectMapper mapper = new ObjectMapper();

        public OrchestrationIdResource() {
            super(URL_PATH_ID);
            getAttributes()
                    .setTitle("Start ochestration process from the ochestrator store based on consumer system id");
        }

        @Override
        public Resource getChild(String name) {
            return this;
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            try {
                int systemId = Integer.parseInt(CoapTools.getUrlPathValue(exchange, URL_PATH_ID));

                if (systemId < 1) {
                    throw new Exception(String.format("Id %d not valid!", systemId));
                }

                if (useFlexibleStore) {
                    throw new BadPayloadException("Orchestrator use flexible store!", HttpStatus.SC_BAD_REQUEST);
                }

                if (systemId < 1) {
                    throw new BadPayloadException("Consumer system : " + ID_NOT_VALID_ERROR_MESSAGE,
                            HttpStatus.SC_BAD_REQUEST);
                }

                exchange.respond(
                        ResponseCode.CONTENT,
                        mapper.writeValueAsString(orchestratorService.storeOchestrationProcessResponse(systemId)),
                        MediaTypeRegistry.APPLICATION_JSON);
                return;

            } catch (Exception ex) {
                exchange.respond(
                        ResponseCode.BAD_REQUEST,
                        ex.getMessage(),
                        MediaTypeRegistry.TEXT_PLAIN);
            }
        }

    }

    // =================================================================================================
    // assistant methods

    // -------------------------------------------------------------------------------------------------
    private void checkOrchestratorFormRequestDTO(final OrchestrationFormRequestDTO request) {
        logger.debug("checkOrchestratorFormRequestDTO started...");

        if (request == null) {
            throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST);
        }

        request.validateCrossParameterConstraints();

        // Requester system
        checkSystemRequestDTO(request.getRequesterSystem());

        // Requester cloud
        if (request.getRequesterCloud() != null) {
            checkCloudRequestDTO(request.getRequesterCloud());
        }

        // Requested service
        if (request.getRequestedService() != null
                && Utilities.isEmpty(request.getRequestedService().getServiceDefinitionRequirement())) {
            throw new BadPayloadException(
                    "Requested service definition requirement" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE,
                    HttpStatus.SC_BAD_REQUEST);
        }

        // Preferred Providers
        if (request.getPreferredProviders() != null) {
            for (final PreferredProviderDataDTO provider : request.getPreferredProviders()) {
                checkSystemRequestDTO(provider.getProviderSystem());
                if (provider.getProviderCloud() != null) {
                    checkCloudRequestDTO(provider.getProviderCloud());
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------
    private void checkSystemRequestDTO(final SystemRequestDTO system) {
        logger.debug("checkSystemRequestDTO started...");

        if (system == null) {
            throw new BadPayloadException("System" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST);
        }

        if (Utilities.isEmpty(system.getSystemName())) {
            throw new BadPayloadException("System name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE,
                    HttpStatus.SC_BAD_REQUEST);
        }

        if (Utilities.isEmpty(system.getAddress())) {
            throw new BadPayloadException("System address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE,
                    HttpStatus.SC_BAD_REQUEST);
        }

        if (system.getPort() == null) {
            throw new BadPayloadException("System port" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST);
        }

        final int validatedPort = system.getPort().intValue();
        if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN
                || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN
                    + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST);
        }
    }

    // -------------------------------------------------------------------------------------------------
    private void checkCloudRequestDTO(final CloudRequestDTO cloud) {
        logger.debug("checkCloudRequestDTO started...");

        if (cloud == null) {
            throw new BadPayloadException("Cloud" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST);
        }

        if (Utilities.isEmpty(cloud.getOperator())) {
            throw new BadPayloadException("Cloud operator" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE,
                    HttpStatus.SC_BAD_REQUEST);
        }

        if (Utilities.isEmpty(cloud.getName())) {
            throw new BadPayloadException("Cloud name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE,
                    HttpStatus.SC_BAD_REQUEST);
        }
    }

}

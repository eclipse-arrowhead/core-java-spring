package eu.arrowhead.core.translator.services.fiware;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.core.translator.services.fiware.common.FiwareEntity;
import eu.arrowhead.core.translator.services.fiware.common.SenML;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

public class ArrowheadDriver {

    //=================================================================================================
    // members
    private static final Logger logger = LogManager.getLogger(ArrowheadDriver.class);
    private final HttpService httpService;
    private final SSLProperties sslProperties;
    private final CoreSystemRegistrationProperties coreSystemRegistrationProperties;
    private final int translatorPort;
    private final String myIpAddress;

    public ArrowheadDriver(int translatorPort, HttpService httpService, SSLProperties sslProperties, CoreSystemRegistrationProperties coreSystemRegistrationProperties) {
        this.translatorPort = translatorPort;
        this.httpService = httpService;
        this.sslProperties = sslProperties;
        this.coreSystemRegistrationProperties = coreSystemRegistrationProperties;
        myIpAddress = getMyIpAddress();
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public void serviceRegistryRegisterAllServices(FiwareEntity entity) {
        Assert.notNull(entity, "entity is null.");
        logger.debug("serviceRegistryRegisterAllServices: " + entity.getId());
        logger.debug(createRegisterUri());
        UriComponents registerUri = createRegisterUri();

        getServicesNames(entity).forEach(serviceName -> {
            logger.debug("Register serviceName: " + serviceName);
            try {
                logger.debug("Registering {} {} {}", entity.getId(), entity.getType(), serviceName);
                ServiceRegistryRequestDTO request = createServiceRegistryFromFiwareService(entity.getId(), entity.getType(), serviceName);
                ResponseEntity<?> response = httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
                logger.debug(response.getStatusCodeValue() + " " + response.getBody());
            } catch (Exception ex) {
                logger.warn("Exception: " + ex.getLocalizedMessage());
                logger.debug("Stacktrace", ex);
            }
        });

    }

    
    //-------------------------------------------------------------------------------------------------
    public SenML[] getDataFromService(UriComponents url) {
        ResponseEntity<SenML[]> httpRsp = httpService.sendRequest(url, HttpMethod.GET, SenML[].class);
        return httpRsp.getBody();
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceRegistryListResponseDTO serviceRegistryListAllServices() {
        UriComponents mgmtUri = createMgmtUri();
        ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(mgmtUri, HttpMethod.GET , ServiceRegistryListResponseDTO.class);
        return response.getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public void serviceRegistryUnregisterAllServices(FiwareEntity entity) {
        Assert.notNull(entity, "entity is null.");
        logger.debug("serviceRegistryUnegisterAllServices: " + entity.getId());
        getServicesNames(entity).forEach(serviceName -> {
            try {
                ResponseEntity<?> response = httpService.sendRequest(createUnregisterUri(entity, serviceName), HttpMethod.DELETE, Void.class);
                logger.debug(response.getStatusCodeValue() + " " + response.getBody());
            } catch (Exception ex) {
                logger.warn("Exception: " + ex.getLocalizedMessage());
                logger.debug("Stacktrace", ex);
            }
        });
    }

    //=================================================================================================
    // assistant methods
    //-------------------------------------------------------------------------------------------------
    private String getMyIpAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException ex) {
            return "127.0.0.1";
        }
    }

    //-------------------------------------------------------------------------------------------------
    private UriComponents createRegisterUri() {
        logger.debug("createRegisterUri started...");
        String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registerUriStr);
    }

    //-------------------------------------------------------------------------------------------------
    private UriComponents createMgmtUri() {
        logger.debug("createMgmtUri started...");
        String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + "/mgmt";
        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registerUriStr);
    }

    //-------------------------------------------------------------------------------------------------
    private UriComponents createUnregisterUri(FiwareEntity entity, String serviceName) {
        Assert.notNull(entity, "entity is null.");
        Assert.notNull(serviceName, "serviceName is null.");
        Assert.isTrue(!serviceName.isEmpty(), "serviceName is empty.");
        logger.debug("createRegisterUri started...");
        String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        String unregisterUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;

        MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>(4);
        queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, List.of(entity.getId()));
        queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS, List.of(myIpAddress));
        queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT, List.of(String.valueOf(translatorPort)));
        queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, List.of(getServiceDefinition(entity.getType(), serviceName)));

        return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), queryMap, unregisterUriStr);
    }

    //-------------------------------------------------------------------------------------------------
    private ServiceRegistryRequestDTO createServiceRegistryFromFiwareService(String id, String type, String serviceName) {
        Assert.notNull(id, "id is null.");
        Assert.notNull(type, "type is null.");
        Assert.notNull(serviceName, "serviceName is null.");
        Assert.isTrue(!id.isEmpty(), "id empty.");
        Assert.isTrue(!type.isEmpty(), "type empty.");
        Assert.isTrue(!serviceName.isEmpty(), "serviceName empty.");
        ServiceRegistryRequestDTO result = new ServiceRegistryRequestDTO();
        final List<String> interfaces = sslProperties.isSslEnabled() ? List.of(CommonConstants.HTTP_SECURE_JSON) : List.of(CommonConstants.HTTP_INSECURE_JSON);
        result.setProviderSystem(getSystemRequestDTO(id));
        result.setServiceDefinition(getServiceDefinition(type, serviceName));
        logger.debug("Uri: " + getServiceUri(id, serviceName));
        result.setServiceUri(getServiceUri(id, serviceName));
        result.setSecure(sslProperties.isSslEnabled() ? ServiceSecurityType.CERTIFICATE.name() : ServiceSecurityType.NOT_SECURE.name());
        result.setInterfaces(interfaces);
        return result;
    }

    //-------------------------------------------------------------------------------------------------
    private SystemRequestDTO getSystemRequestDTO(String id) {

        final SystemRequestDTO result = new SystemRequestDTO();
        result.setSystemName(id);
        result.setAddress(myIpAddress);
        result.setPort(translatorPort);

        return result;
    }

    //-------------------------------------------------------------------------------------------------
    private String getServiceUri(String entityId, String serviceName) {
        return String.format("/translator/plugin/service/%s/%s",
                /*sslProperties.isSslEnabled() ? "https" : "http",
                myIpAddress,
                translatorPort,*/
                entityId,
                serviceName
        );
    }

    //-------------------------------------------------------------------------------------------------
    private String getServiceDefinition(String type, String serviceName) {
        return String.format("fiware %s - %s", type, serviceName);
    }

    //-------------------------------------------------------------------------------------------------
    private ArrayList<String> getServicesNames(FiwareEntity entity) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry:entity.any().entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

}

/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.drivers;

import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class DriverUtilities {
	
	//=================================================================================================
	// members

    private final Logger logger = LogManager.getLogger(DriverUtilities.class);
    private final CoreSystemRegistrationProperties coreSystemProps;
    private final HttpService httpService;
    private final SSLProperties sslProperties;

    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String, Object> arrowheadContext;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Autowired
    public DriverUtilities(final CoreSystemRegistrationProperties coreSystemProps,
                           final HttpService httpService,
                           final SSLProperties sslProperties) {
        super();
        this.coreSystemProps = coreSystemProps;
        this.httpService = httpService;
        this.sslProperties = sslProperties;
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
	public <T> T getContext(final String key) {
        return (T) arrowheadContext.get(key);
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
	public <T> T getContext(final String key, final T defaultValue) {
        return (T) arrowheadContext.getOrDefault(key, defaultValue);
    }

    //-------------------------------------------------------------------------------------------------
    public boolean containsContext(final String key) {
        return arrowheadContext.containsKey(key);
    }

    //-------------------------------------------------------------------------------------------------
    public boolean pingService(final UriComponents echoUri) {
        try {
            httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
            logger.debug("Service at {} is accessible...", echoUri);
            return true;
        } catch (final Exception e) {
            logger.debug("Service at {} is not accessible: {}: {}", echoUri, e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    //-------------------------------------------------------------------------------------------------
    public boolean pingService(final ServiceSecurityType securityType, final SystemResponseDTO system, final String systemUri) {
        final UriComponents echoUri = createUri(securityType, system, systemUri + CommonConstants.ECHO_URI);
        return pingService(echoUri);
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents findUri(final CoreSystemService service) throws DriverException {
        try {
            return findUriByOrchestrator(service);
        } catch (final DriverException e) {
            logger.debug(e.getMessage());
        } catch (final Exception e) {
            logger.info("Unable to find '{}' via Orchestrator. Retrying using ServiceRegistry. Error: {}: {}",
                        service.getServiceDefinition(), e.getClass().getSimpleName(), e.getMessage());
        }

        return findUriByServiceRegistry(service);
    }

    //-------------------------------------------------------------------------------------------------
    public UriComponents findUriByContext(final CoreSystemService service) throws DriverException {
        Assert.notNull(service, "CoreSystemService must not be null");
        logger.debug("Searching for '{}' uri in context ...", service.getServiceDefinition());
        final String key = getCoreSystemServiceKey(service);
        if (containsContext(key)) {
            final UriComponents serviceUri = getContext(key);
            final UriComponents echoUri = createEchoUri(serviceUri);
            if (pingService(echoUri)) {
                return serviceUri;
            }
        }

        throw DriverException.notFoundByArrowheadContext(service);
    }

    //-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO findByServiceRegistry(final CoreSystemService service, final boolean pingTarget) throws DriverException {
        Assert.notNull(service, "CoreSystemService must not be null");
        logger.debug("findByServiceRegistry started...");
        final UriComponents queryUri = getServiceRegistryQueryUri();
        final ServiceQueryFormDTO form = new ServiceQueryFormDTO.Builder(service.getServiceDefinition())
                .interfaces(getInterface())
                .build();
        final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
        final ServiceQueryResultDTO result = response.getBody();

        if (Objects.isNull(result) || Objects.isNull(result.getServiceQueryData()) || result.getServiceQueryData().isEmpty()) {
            throw DriverException.notFoundByServiceRegistry(service);
        }

        final List<ServiceRegistryResponseDTO> serviceQueryData = result.getServiceQueryData();

        for (int reverseIndex = serviceQueryData.size() - 1; reverseIndex >= 0; reverseIndex--) {
            final ServiceRegistryResponseDTO entry = serviceQueryData.get(reverseIndex);
            final UriComponents uri = createUri(entry);
            if (!pingTarget) {
                arrowheadContext.putIfAbsent(getCoreSystemServiceKey(service), uri);
                return entry;
            } else if (pingService(createEchoUri(uri))) {
                arrowheadContext.putIfAbsent(getCoreSystemServiceKey(service), uri);
                return entry;
            }
        }

        throw DriverException.notFoundByServiceRegistry(service);
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents findUriByServiceRegistry(final CoreSystemService service) throws DriverException {

        try {
            return findUriByContext(service);
        } catch (final DriverException e) {
            logger.debug(e.getMessage());
        }

        logger.debug("findUriByServiceRegistry started...");
        final ServiceRegistryResponseDTO entry = findByServiceRegistry(service, true);
        return createUri(entry);
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents findUriByOrchestrator(final CoreSystemService service) throws DriverException {
        Assert.notNull(service, "CoreSystemService must not be null");

        try {
            return findUriByContext(service);
        } catch (final DriverException e) {
            logger.debug(e.getMessage());
        }

        logger.debug("findUriByOrchestrator started...");

        final UriComponents queryUri = getOrchestrationQueryUri();
        final SystemRequestDTO requester = new SystemRequestDTO();
        requester.setAddress(coreSystemProps.getCoreSystemDomainName());
        requester.setPort(coreSystemProps.getCoreSystemDomainPort());
        requester.setSystemName(coreSystemProps.getCoreSystemName());

        final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO.Builder(service.getServiceDefinition())
                .interfaces(getInterface())
                .build();

        final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO
                .Builder(requester)
                .requestedService(serviceQueryForm)
                .flag(OrchestrationFlags.Flag.OVERRIDE_STORE, true)
                .build();

        final ResponseEntity<OrchestrationResponseDTO> httpResponse = httpService
                .sendRequest(queryUri, HttpMethod.POST, OrchestrationResponseDTO.class, orchestrationForm);
        final OrchestrationResponseDTO response = httpResponse.getBody();

        if (Objects.isNull(response) || Objects.isNull(response.getResponse()) || response.getResponse().isEmpty()) {
            throw DriverException.notFoundByOrchestrator(service);
        }

        final List<OrchestrationResultDTO> result = response.getResponse();

        for (int reverseIndex = result.size() - 1; reverseIndex >= 0; reverseIndex--) {
            final OrchestrationResultDTO entry = result.get(reverseIndex);
            final UriComponents uri = createUri(entry);
            if (pingService(createEchoUri(uri))) {
                arrowheadContext.put(getCoreSystemServiceKey(service), uri);
                return uri;
            }
        }

        throw DriverException.notFoundByOrchestrator(service);
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents createUri(final ServiceRegistryResponseDTO entry) {
        Assert.notNull(entry, "ServiceRegistryResponseDTO must not be null");
        return createUri(entry.getSecure(), entry.getProvider(), entry.getServiceUri());
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents createUri(final OrchestrationResultDTO entry) {
        Assert.notNull(entry, "OrchestrationResultDTO must not be null");
        return createUri(entry.getSecure(), entry.getProvider(), entry.getServiceUri());
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents createUri(final ServiceSecurityType securityType, final SystemResponseDTO system, final String serviceUri) {
        Assert.notNull(system, "SystemResponseDTO must not be null");
        return Utilities.createURI(getScheme(securityType),
                                   system.getAddress(),
                                   system.getPort(),
                                   serviceUri);
    }

    //-------------------------------------------------------------------------------------------------
	public UriComponents createEchoUri(final UriComponents serviceUri) {
        Assert.notNull(serviceUri, "UriComponents must not be null");
        return UriComponentsBuilder.newInstance()
                                   .scheme(serviceUri.getScheme())
                                   .host(serviceUri.getHost())
                                   .port(serviceUri.getPort())
                                   .path(extractSystemUriPath(serviceUri))
                                   .path(CommonConstants.ECHO_URI)
                                   .build();
    }

    //-------------------------------------------------------------------------------------------------
    public UriComponents createCustomUri(final UriComponents serviceUri, final String systemUri, final String postfix) {
        Assert.notNull(serviceUri, "UriComponents must not be null");
        return UriComponentsBuilder.newInstance()
                                   .uriComponents(serviceUri)
                                   .replacePath(systemUri)
                                   .path(postfix)
                                   .build();
    }

    //-------------------------------------------------------------------------------------------------
    public UriComponents createCustomUri(final UriComponents serviceUri, final String postfix) {
        Assert.notNull(serviceUri, "UriComponents must not be null");
        return UriComponentsBuilder.newInstance()
                                   .uriComponents(serviceUri)
                                   .path(extractSystemUriPath(serviceUri))
                                   .path(postfix)
                                   .build();
    }

    //-------------------------------------------------------------------------------------------------
    public String getCoreSystemServiceKey(final CoreSystemService service) {
        Assert.notNull(service, "CoreSystemService must not be null");
        return service.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    }

    //-------------------------------------------------------------------------------------------------
    public SystemRequestDTO getCoreSystemRequestDTO() {
        logger.debug("getCoreSystemRequestDTO started...");

        final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
        final SystemRequestDTO result = new SystemRequestDTO();
        result.setSystemName(coreSystemProps.getCoreSystem().name().toLowerCase());
        result.setAddress(coreSystemProps.getCoreSystemDomainName());
        result.setPort(coreSystemProps.getCoreSystemDomainPort());

        if (sslProperties.isSslEnabled() && Objects.nonNull(publicKey)) {
            result.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }

        return result;
    }
    
    //=================================================================================================
	// assistant methods

    //-------------------------------------------------------------------------------------------------
    protected UriComponents getServiceRegistryQueryUri() {
        logger.debug("getServiceRegistryQueryUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_URI)) {
            return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_URI);
        } else {
            final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
            final String registerUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_QUERY_URI;
            final UriComponents uri = Utilities.createURI(scheme, coreSystemProps.getServiceRegistryAddress(),
                                                          coreSystemProps.getServiceRegistryPort(), registerUriStr);
            arrowheadContext.putIfAbsent(CoreCommonConstants.SR_QUERY_URI, uri);
            return uri;
        }
    }

    //-------------------------------------------------------------------------------------------------
    protected UriComponents getOrchestrationQueryUri() throws DriverException {
        logger.debug("getOrchestrationQueryUri started...");
        return findUriByServiceRegistry(CoreSystemService.ORCHESTRATION_SERVICE);
    }

    //-------------------------------------------------------------------------------------------------
    protected String getScheme(final ServiceSecurityType securityType) {
        return securityType == ServiceSecurityType.NOT_SECURE ? CommonConstants.HTTP : CommonConstants.HTTPS;
    }

    //-------------------------------------------------------------------------------------------------
    protected String getInterface() {
        return sslProperties.isSslEnabled() ? CommonConstants.HTTP_SECURE_JSON : CommonConstants.HTTP_INSECURE_JSON;
    }

    //-------------------------------------------------------------------------------------------------
    private String extractSystemUriPath(final UriComponents components) {
        final List<String> pathSegments = components.getPathSegments();
        return pathSegments.isEmpty() ? components.getPath() : pathSegments.get(0);
    }
    
    //=================================================================================================
	// nested classes

    //-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	public static class DriverException extends UnavailableServerException {

        protected DriverException(final String message, final HttpStatus httpStatus) {
            super(message, httpStatus.value());
        }

        protected DriverException(final String msg, final int errorCode, final String origin, final Throwable cause) {
            super(msg, errorCode, origin, cause);
        }

        private static DriverException notFoundByArrowheadContext(final CoreSystemService service) {
            return new DriverException("Unable to find " + service.getServiceDefinition() + " via ArrowheadContext", HttpStatus.SERVICE_UNAVAILABLE);
        }

        private static DriverException notFoundByServiceRegistry(final CoreSystemService service) {
            return new DriverException("Unable to find " + service.getServiceDefinition() + " via ServiceRegistry", HttpStatus.SERVICE_UNAVAILABLE);
        }

        private static DriverException notFoundByOrchestrator(final CoreSystemService service) {
            return new DriverException("Unable to find " + service.getServiceDefinition() + " via Orchestrator", HttpStatus.SERVICE_UNAVAILABLE);
        }

        public DriverException createExceptionWith(final HttpStatus errorCode, final String origin) {
            return new DriverException(getMessage(), errorCode.value(), origin, getCause());
        }

        public DriverException createExceptionWith(final int errorCode, final String origin) {
            return new DriverException(getMessage(), errorCode, origin, getCause());
        }
    }
}
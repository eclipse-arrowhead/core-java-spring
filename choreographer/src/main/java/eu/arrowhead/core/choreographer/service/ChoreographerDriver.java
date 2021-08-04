package eu.arrowhead.core.choreographer.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class ChoreographerDriver {

    //=================================================================================================
    // members

    private static final Logger logger = LogManager.getLogger(ChoreographerDriver.class);

    @Autowired
    private HttpService httpService;

    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String,Object> arrowheadContext;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ServiceRegistryListResponseDTO queryServiceRegistryByServiceDefinitionList(final List<String> serviceDefinitions) {
        logger.debug("queryServiceRegistryByServiceDefinitionList started...");
        Assert.notNull(serviceDefinitions, "Service Definition list is null.");

        final UriComponents queryByServiceDefinitionListUri = getQueryByServiceDefinitionListUri();
        final ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(queryByServiceDefinitionListUri, HttpMethod.POST, ServiceRegistryListResponseDTO.class, serviceDefinitions);

        return response.getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public SystemResponseDTO registerSystem(final SystemRequestDTO request) {
    	logger.debug("registerSystem started...");
        Assert.notNull(request, "SystemRequestDTO is null.");
        
        final UriComponents uri = getRegisterSystemUri();
        return httpService.sendRequest(uri, HttpMethod.POST, SystemResponseDTO.class, request).getBody();
    }
    
    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private UriComponents getQueryByServiceDefinitionListUri() {
        logger.debug("getQueryByServiceDefinitionListUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SERVICE_DEFINITION_LIST_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SERVICE_DEFINITION_LIST_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry Query By Service Definition URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry Query By Service Definition URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getRegisterSystemUri() {
    	logger.debug("getRegisterSystemUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry Register System URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry Register System URI.");
    }
}

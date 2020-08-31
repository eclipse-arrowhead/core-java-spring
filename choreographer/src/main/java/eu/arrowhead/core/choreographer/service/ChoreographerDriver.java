package eu.arrowhead.core.choreographer.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    public ServiceRegistryListResponseDTO queryServiceRegistryByServiceDefinitionList(final List<String> serviceDefinitions) {
        logger.debug("queryServiceRegistryByServiceDefinitionList started...");
        Assert.notNull(serviceDefinitions, "Service Definition list is null.");

        final UriComponents queryByServiceDefinitionListUri = getQueryByServiceDefinitionListUri();
        final ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(queryByServiceDefinitionListUri, HttpMethod.POST, ServiceRegistryListResponseDTO.class, serviceDefinitions);

        return response.getBody();
    }

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
}

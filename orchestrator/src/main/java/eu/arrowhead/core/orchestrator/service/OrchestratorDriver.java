package eu.arrowhead.core.orchestrator.service;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class OrchestratorDriver {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(OrchestratorDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	// The two boolean parameters override the corresponding settings in the form
	public ServiceQueryResultDTO queryServiceRegistry(final ServiceQueryFormDTO form, final boolean metadataSearch, final boolean pingProviders) {
		logger.debug("queryServiceRegistry started...");
		
		// overriding settings
		form.setPingProviders(pingProviders);
		if (!metadataSearch) {
			form.setMetadataRequirements(null);
		}
		
		final UriComponents queryUri = getQueryUri();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
		final ServiceQueryResultDTO result = response.getBody();
		
		//TODO: cont
		
		return result;
	}

	//=================================================================================================
	// assistant methods
	
	private UriComponents getQueryUri() {
		if (arrowheadContext.containsKey(CommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
	}


}
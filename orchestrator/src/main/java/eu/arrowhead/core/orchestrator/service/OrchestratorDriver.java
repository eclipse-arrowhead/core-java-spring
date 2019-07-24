package eu.arrowhead.core.orchestrator.service;

import java.util.Collections;
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
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
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
		Assert.notNull(form, "Form is null.");
		
		// overriding settings
		form.setPingProviders(pingProviders);
		if (!metadataSearch) {
			form.setMetadataRequirements(null);
		}
		
		final UriComponents queryUri = getQueryUri();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO queryServiceRegistryBySystemId(final long consumerSystemId) {
		logger.debug("queryByIdServiceRegistry started...");
		Assert.isTrue(consumerSystemId > 0, "ConsumerSystemId is less then 1.");
		
		final UriComponents queryBySystemIdUri = getQueryBySystemIdUri().expand(
				Collections.singletonMap(CommonConstants.COMMON_FIELD_NAME_ID, consumerSystemId + ""));
		
		final ResponseEntity<SystemResponseDTO> response = httpService.sendRequest(queryBySystemIdUri, HttpMethod.GET, SystemResponseDTO.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO queryServiceRegistryBySystemRequestDTO(SystemRequestDTO consumerSystemRequestDTO) {
		logger.debug("queryServiceRegistryBySystemRequestDTO started...");
		Assert.notNull(consumerSystemRequestDTO, "ConsumerSystemRequestDTO is null.");
		

		final UriComponents queryBySystemDTOUri = getQueryBySystemDTOUri();
		final ResponseEntity<SystemResponseDTO> response = httpService.sendRequest(queryBySystemDTOUri, HttpMethod.POST, SystemResponseDTO.class, consumerSystemRequestDTO);
		
		return response.getBody();
	}
	//-------------------------------------------------------------------------------------------------
	public List<OrchestrationResultDTO> generateAuthTokens(final OrchestrationFormRequestDTO request, final List<OrchestrationResultDTO> orList) {
		logger.debug("generateAuthTokens started...");
		
		//TODO: implement this
		
		return orList;
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryUri() {
		logger.debug("getQueryUri started...");
		
		if (arrowheadContext.containsKey(CommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryBySystemIdUri() {
		logger.debug("getQueryByIdUri started...");
		
		if (arrowheadContext.containsKey(CommonConstants.SR_QUERY_BY_SYSTEM_ID_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CommonConstants.SR_QUERY_BY_SYSTEM_ID_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query By Id URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query By Id URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryBySystemDTOUri() {
		logger.debug("getQueryBySystemDTOUri started...");
		
		if (arrowheadContext.containsKey(CommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query By DTO URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query By DTO URI.");
	}

}
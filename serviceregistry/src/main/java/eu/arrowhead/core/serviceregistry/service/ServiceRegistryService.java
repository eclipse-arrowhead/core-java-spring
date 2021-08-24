package eu.arrowhead.core.serviceregistry.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.KeyValuesDTO;

@Service
public class ServiceRegistryService {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;
	
	@Value(CoreCommonConstants.$ALLOW_SELF_ADDRESSING_WD)
	private boolean allowSelfAddressing;
	
	@Value(CoreCommonConstants.$ALLOW_NON_ROUTABLE_ADDRESSING_WD)
	private boolean allowNonRoutableAddressing;

	private final Logger logger = LogManager.getLogger(ServiceRegistryService.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public KeyValuesDTO getPublicConfig() { //TODO juint
		logger.debug("getPublicConfig started...");
		
		final Map<String,String> map = new HashMap<>();
		map.put(CoreCommonConstants.USE_STRICT_SERVICE_DEFINITION_VERIFIER, String.valueOf(useStrictServiceDefinitionVerifier));
		map.put(CoreCommonConstants.ALLOW_SELF_ADDRESSING, String.valueOf(allowSelfAddressing));
		map.put(CoreCommonConstants.ALLOW_NON_ROUTABLE_ADDRESSING, String.valueOf(allowNonRoutableAddressing));
		return new KeyValuesDTO(map);
	}
}

package eu.arrowhead.core.qos.manager;

import java.util.Map;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

public interface QoSVerifier {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean verify(final OrchestrationResultDTO result, final Map<String,String> qosRequirements, final Map<String,String> commands);
}
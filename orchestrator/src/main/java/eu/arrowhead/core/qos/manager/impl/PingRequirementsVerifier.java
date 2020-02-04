package eu.arrowhead.core.qos.manager.impl;

import java.util.Map;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.core.qos.manager.QoSVerifier;

public class PingRequirementsVerifier implements QoSVerifier {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean verify(final OrchestrationResultDTO result, final Map<String,String> qosRequirements, final Map<String,String> commands) {
		// TODO Auto-generated method stub
		return true;
	}
}
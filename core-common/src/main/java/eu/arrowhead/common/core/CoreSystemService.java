package eu.arrowhead.common.core;

import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

public enum CoreSystemService {

	//=================================================================================================
	// elements
	
	// Authorization services
	AUTH_CONTROL_INTRA_SERVICE(CommonConstants.CORE_SERVICE_AUTH_CONTROL_INTRA, CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_INTRA_CHECK_URI),
	AUTH_CONTROL_INTER_SERVICE(CommonConstants.CORE_SERVICE_AUTH_CONTROL_INTER, CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_INTER_CHECK_URI),
	AUTH_TOKEN_GENERATION_SERVICE(CommonConstants.CORE_SERVICE_AUTH_TOKEN_GENERATION, CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_TOKEN_URI),
	AUTH_PUBLIC_KEY_SERVICE(CommonConstants.CORE_SERVICE_AUTH_PUBLIC_KEY, CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_KEY_URI),
	
	// Orchestrator services
	ORCHESTRATION_SERVICE(CommonConstants.CORE_SERVICE_ORCH_PROCESS, CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS),
	
	// Gatekeeper services
	GATEKEEPER_GLOBAL_SERVICE_DISCOVERY(CommonConstants.CORE_SERVICE_GATEKEEPER_GSD, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GSD_SERVICE),
	GATEKEEPER_INTER_CLOUD_NEGOTIATION(CommonConstants.CORE_SERVICE_GATEKEEPER_ICN, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_ICN_SERVICE),
	
	// EventHandler services
	EVENT_PUBLISH(CommonConstants.CORE_SERVICE_EVENT_HANDLER_PUBLISH, CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH),
	EVENT_SUBSCRIBE(CommonConstants.CORE_SERVICE_EVENT_HANDLER_SUBSCRIBE, CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE);
	
	//TODO: additional services 
	
	//=================================================================================================
	// members
	
	private final String serviceDefinition;
	private final String serviceUri;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	public String getServiceUri() { return serviceUri; }
	
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreSystemService(final String serviceDefinition, final String serviceUri) {
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), serviceUri);
		
		this.serviceDefinition = serviceDefinition;
		this.serviceUri = serviceUri;
	}
}
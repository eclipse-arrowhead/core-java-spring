package eu.arrowhead.common.core;

import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;

public enum CoreSystemService {

	//=================================================================================================
	// elements
	
	// Authorization services
	AUTH_CONTROL_INTRA_SERVICE(CoreCommonConstants.CORE_SERVICE_AUTH_CONTROL_INTRA, CoreCommonConstants.AUTHORIZATION_URI + CoreCommonConstants.OP_AUTH_INTRA_CHECK_URI),
	AUTH_CONTROL_INTER_SERVICE(CoreCommonConstants.CORE_SERVICE_AUTH_CONTROL_INTER, CoreCommonConstants.AUTHORIZATION_URI + CoreCommonConstants.OP_AUTH_INTER_CHECK_URI),
	AUTH_TOKEN_GENERATION_SERVICE(CoreCommonConstants.CORE_SERVICE_AUTH_TOKEN_GENERATION, CoreCommonConstants.AUTHORIZATION_URI + CoreCommonConstants.OP_AUTH_TOKEN_URI),
	AUTH_PUBLIC_KEY_SERVICE(CoreCommonConstants.CORE_SERVICE_AUTH_PUBLIC_KEY, CoreCommonConstants.AUTHORIZATION_URI + CoreCommonConstants.OP_AUTH_KEY_URI),
	
	// Orchestrator services
	ORCHESTRATION_SERVICE(CoreCommonConstants.CORE_SERVICE_ORCH_PROCESS, CoreCommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.OP_ORCH_PROCESS),
	
	// Gatekeeper services
	GATEKEEPER_GLOBAL_SERVICE_DISCOVERY(CoreCommonConstants.CORE_SERVICE_GATEKEEPER_GSD, CoreCommonConstants.GATEKEEPER_URI + CoreCommonConstants.OP_GATEKEEPER_GSD_SERVICE),
	GATEKEEPER_INTER_CLOUD_NEGOTIATION(CoreCommonConstants.CORE_SERVICE_GATEKEEPER_ICN, CoreCommonConstants.GATEKEEPER_URI + CoreCommonConstants.OP_GATEKEEPER_ICN_SERVICE),
	
	// Gateway services
	GATEWAY_PUBLIC_KEY_SERVICE(CoreCommonConstants.CORE_SERVICE_GATEWAY_PUBLIC_KEY, CoreCommonConstants.GATEWAY_URI + CoreCommonConstants.OP_GATEWAY_KEY_URI),
	GATEWAY_PROVIDER_SERVICE(CoreCommonConstants.CORE_SERVICE_GATEWAY_CONNECT_PROVIDER, CoreCommonConstants.GATEWAY_URI + CoreCommonConstants.OP_GATEWAY_CONNECT_PROVIDER_URI),
	GATEWAY_CONSUMER_SERVICE(CoreCommonConstants.CORE_SERVICE_GATEWAY_CONNECT_CONSUMER, CoreCommonConstants.GATEWAY_URI + CoreCommonConstants.OP_GATEWAY_CONNECT_CONSUMER_URI),
	
	// EventHandler services
	EVENT_PUBLISH(CoreCommonConstants.CORE_SERVICE_EVENT_HANDLER_PUBLISH, CoreCommonConstants.EVENT_HANDLER_URI + CoreCommonConstants.OP_EVENT_HANDLER_PUBLISH),
	EVENT_SUBSCRIBE(CoreCommonConstants.CORE_SERVICE_EVENT_HANDLER_SUBSCRIBE, CoreCommonConstants.EVENT_HANDLER_URI + CoreCommonConstants.OP_EVENT_HANDLER_SUBSCRIBE);
	
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
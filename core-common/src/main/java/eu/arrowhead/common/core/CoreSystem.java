package eu.arrowhead.common.core;

import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;

public enum CoreSystem {
	
	//=================================================================================================
	// elements
	
	SERVICE_REGISTRY(Defaults.DEFAULT_SERVICE_REGISTRY_PORT, null),
	AUTHORIZATION(Defaults.DEFAULT_AUTHORIZATION_PORT, List.of(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE, CoreSystemService.AUTH_CONTROL_INTER_SERVICE,
															   CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, CoreSystemService.AUTH_PUBLIC_KEY_SERVICE)),
	ORCHESTRATOR(Defaults.DEFAULT_ORCHESTRATOR_PORT, List.of(CoreSystemService.ORCHESTRATION_SERVICE)), 
	GATEKEEPER(Defaults.DEFAULT_GATEKEEPER_PORT, List.of(CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY, CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION)),
	GATEWAY(Defaults.DEFAULT_GATEWAY_PORT, List.of()), // TODO: add services,
	EVENT_HANDLER(Defaults.DEFAULT_EVENT_HANDLER_PORT, List.of(CoreSystemService.EVENT_PUBLISH, CoreSystemService.EVENT_SUBSCRIBE)),
	CERTIFICATE_AUTHORITY(Defaults.DEFAULT_CERTIFICATE_AUTHORITY_PORT, List.of()); // TODO: add services		
	
	//=================================================================================================
	// members
	
	private final int defaultPort;
	private final List<CoreSystemService> services;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public int getDefaultPort() { return defaultPort; }
	public List<CoreSystemService> getServices() { return services; }
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreSystem(final int defaultPort, final List<CoreSystemService> services) {
		Assert.isTrue(defaultPort > CommonConstants.SYSTEM_PORT_RANGE_MIN && defaultPort < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Default port is invalid.");
		this.services = services != null ? Collections.unmodifiableList(services) : List.of();
		this.defaultPort = defaultPort;
	}
}
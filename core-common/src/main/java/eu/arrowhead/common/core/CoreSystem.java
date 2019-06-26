package eu.arrowhead.common.core;

import java.util.Collections;
import java.util.List;

public enum CoreSystem {
	
	//=================================================================================================
	// elements
	
	SERVICE_REGISTRY(null),
	AUTHORIZATION(List.of(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE, CoreSystemService.AUTH_CONTROL_INTER_SERVICE, CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, 
						  CoreSystemService.AUTH_PUBLIC_KEY_SERVICE)),
	ORCHESTRATOR(List.of()), // TODO: add services
	GATEKEEPER(List.of()), // TODO: add services
	GATEWAY(List.of()), // TODO: add services,
	EVENT_HANDLER(List.of()), // TODO: add services
	CERTIFICATE_AUTHORITY(List.of()); // TODO: add services		
	
	//=================================================================================================
	// members
	
	private final List<CoreSystemService> services;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<CoreSystemService> getServices() { return services; }
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreSystem(final List<CoreSystemService> services) {
		this.services = services != null ? Collections.unmodifiableList(services) : List.of();
	}
}
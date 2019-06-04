package eu.arrowhead.common.dto;

import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;

public class DTOConverter {

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertSystemToSystemResponseDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		return new SystemResponseDTO(system.getSystemName(), system.getAddress(), system.getPort(), system.getAuthenticationInfo());		
	}
	
	public static ServiceDefinitionResponseDTO convertServiceToServiceResponseDTO (final ServiceDefinition service) {
		Assert.notNull(service, "Service is null");
		
		return new ServiceDefinitionResponseDTO(service.getId(), service.getServiceDefinition(), String.valueOf(service.getCreatedAt()), String.valueOf(service.getUpdatedAt()));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOConverter() {
		throw new UnsupportedOperationException();
	}
	
}

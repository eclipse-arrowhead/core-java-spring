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
	
	public static ServiceResponseDTO convertServiceToServiceResponseDTO (final ServiceDefinition service) {
		Assert.notNull(service, "Service is null");
		
		return new ServiceResponseDTO(service.getId(), service.getServiceDefinition(), String.valueOf(service.getCreatedAt()), String.valueOf(service.getUpdatedAt()));
	}
	
	public ServiceDefinition convertServiceRequestDTOToServiceDefinition(ServiceRequestDTO serviceRequestDTO) {
		Assert.notNull(serviceRequestDTO, "Service is null");
		Assert.notNull(serviceRequestDTO.getServiceDefinition(), "Service definition is null");
		
		return new ServiceDefinition(serviceRequestDTO.getServiceDefinition());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOConverter() {
		throw new UnsupportedOperationException();
	}
	
}

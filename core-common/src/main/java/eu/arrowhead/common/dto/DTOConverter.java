package eu.arrowhead.common.dto;

import eu.arrowhead.common.database.entity.System;
import org.springframework.util.Assert;

public class DTOConverter {

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertSystemToSystemResponseDTO(System system) {
		Assert.notNull(system, "System is null");
		
		return new SystemResponseDTO(system.getSystemName(), system.getAddress(), system.getPort(), system.getAuthenticationInfo());		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOConverter() {
		throw new UnsupportedOperationException();
	}
	
}

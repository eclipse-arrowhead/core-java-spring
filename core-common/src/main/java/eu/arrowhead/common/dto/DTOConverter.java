package eu.arrowhead.common.dto;

import eu.arrowhead.common.database.entity.System;

public class DTOConverter {

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO convertSystemToSystemResponseDTO(System system) {

		return new SystemResponseDTO(system.getSystemName(), system.getAddress(), system.getPort(), system.getAuthenticationInfo());		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	
	
}

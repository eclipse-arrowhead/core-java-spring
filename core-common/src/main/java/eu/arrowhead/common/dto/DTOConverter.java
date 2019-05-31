package eu.arrowhead.common.dto;

import eu.arrowhead.common.database.entity.System;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

public class DTOConverter {

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SystemResponseDTO convertSystemToSystemResponseDTO(final System system) {
		Assert.notNull(system, "System is null");
		
		return new SystemResponseDTO(system.getSystemName(), system.getAddress(), system.getPort(), system.getAuthenticationInfo());		
	}
	
	//-------------------------------------------------------------------------------------------------
	public static SystemListResponseDTO convertSystemEntryListToSystemListResponseDTO(final Page<System> systemEntryList) {
		Assert.notNull(systemEntryList, "systemEntryList is null");
		
		final long totalNumberOfSystems = systemEntryList.getTotalElements();		
		
		final SystemListResponseDTO systemListResponseDTO = new SystemListResponseDTO();
		
		systemListResponseDTO.setTotalNumberOfSystems(totalNumberOfSystems);
		systemListResponseDTO.setSystemResponeDTOList(sytemEntryListToSystemResponeDTOList(systemEntryList.getContent()));
		
		return systemListResponseDTO;
		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	
	private DTOConverter() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	
	private static List<SystemResponseDTO> sytemEntryListToSystemResponeDTOList(final List<System> systemList) {
		
		final List<SystemResponseDTO> systemResponseDTOs = new ArrayList<SystemResponseDTO>();
		
		for (System system : systemList) {
			systemResponseDTOs.add(convertSystemToSystemResponseDTO(system));
		}
		
		return systemResponseDTOs;
		
	}
	
}

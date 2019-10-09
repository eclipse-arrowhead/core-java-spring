package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@JsonInclude(Include.NON_NULL)
public class AutoCompleteDataResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2436219204484930542L;
	
	private List<IdValueDTO> serviceList;
	private List<SystemResponseDTO> systemList;
	private List<IdValueDTO> interfaceList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AutoCompleteDataResponseDTO() {}
	
	public AutoCompleteDataResponseDTO(final List<IdValueDTO> serviceList, final List<SystemResponseDTO> systemList, final List<IdValueDTO> interfaceList) {
		this.serviceList = serviceList;
		this.systemList = systemList;
		this.interfaceList = interfaceList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<IdValueDTO> getServiceList() { return serviceList; }
	public List<SystemResponseDTO> getSystemList() { return systemList; }
	public List<IdValueDTO> getInterfaceList() { return interfaceList; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceList(final List<IdValueDTO> serviceList) { this.serviceList = serviceList; }
	public void setSystemList(final List<SystemResponseDTO> systemList) { this.systemList = systemList; }
	public void setInterfaceList(final List<IdValueDTO> interfaceList) { this.interfaceList = interfaceList; }
}
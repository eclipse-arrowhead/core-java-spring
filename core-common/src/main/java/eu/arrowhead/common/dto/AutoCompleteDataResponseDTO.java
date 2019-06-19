package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AutoCompleteDataResponseDTO implements Serializable {

	private static final long serialVersionUID = 2436219204484930542L;
	
	//=================================================================================================
	// members
	
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
	public List<IdValueDTO> getServiceList() {return serviceList;}
	public List<SystemResponseDTO> getSystemList() {return systemList;}
	public List<IdValueDTO> getInterfaceList() {return interfaceList;}
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceList(final List<IdValueDTO> serviceList) {this.serviceList = serviceList;}
	public void setSystemList(final List<SystemResponseDTO> systemList) {this.systemList = systemList;}
	public void setInterfaceList(final List<IdValueDTO> interfaceList) {this.interfaceList = interfaceList;}
}

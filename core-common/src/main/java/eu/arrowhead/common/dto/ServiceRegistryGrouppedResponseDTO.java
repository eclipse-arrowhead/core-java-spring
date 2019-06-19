package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class ServiceRegistryGrouppedResponseDTO implements Serializable{

	private static final long serialVersionUID = 8834608315964460311L;

	//=================================================================================================
	// members
	
	private List<ServicesGrouppedBySystemsResponseDTO> servicesGrouppedBySystems;
	private List<ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO> servicesGrouppedByServiceDefinitionAndInterface;
	private AutoCompleteDataResponseDTO  autoCompleteData;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public ServiceRegistryGrouppedResponseDTO() {}
	
	public ServiceRegistryGrouppedResponseDTO(final List<ServicesGrouppedBySystemsResponseDTO> servicesGrouppedBySystems,	final List<ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO> servicesGrouppedByServiceDefinitionAndInterface, 
			final AutoCompleteDataResponseDTO autoCompleteData) {
		this.servicesGrouppedBySystems = servicesGrouppedBySystems;
		this.servicesGrouppedByServiceDefinitionAndInterface = servicesGrouppedByServiceDefinitionAndInterface;
		this.autoCompleteData = autoCompleteData;
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<ServicesGrouppedBySystemsResponseDTO> getServicesGrouppedBySystems() {return servicesGrouppedBySystems;}
	public List<ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO> getServicesGrouppedByServiceDefinitionAndInterface() {return servicesGrouppedByServiceDefinitionAndInterface;}
	public AutoCompleteDataResponseDTO getAutoCompleteData() {return autoCompleteData;}

	//-------------------------------------------------------------------------------------------------	
	public void setServicesGrouppedBySystems(final List<ServicesGrouppedBySystemsResponseDTO> servicesGrouppedBySystems) {this.servicesGrouppedBySystems = servicesGrouppedBySystems;}
	public void setServicesGrouppedByServiceDefinitionAndInterface(final List<ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO> servicesGrouppedByServiceDefinitionAndInterface) {
		this.servicesGrouppedByServiceDefinitionAndInterface = servicesGrouppedByServiceDefinitionAndInterface;
	}
	public void setAutoCompleteData(final AutoCompleteDataResponseDTO autoCompleteData) {this.autoCompleteData = autoCompleteData;}	
}

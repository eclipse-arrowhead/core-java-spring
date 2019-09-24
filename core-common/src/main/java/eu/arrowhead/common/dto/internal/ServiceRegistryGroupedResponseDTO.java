package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ServiceRegistryGroupedResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8834608315964460311L;
	
	private List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems;
	private List<ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO> servicesGroupedByServiceDefinitionAndInterface;
	private AutoCompleteDataResponseDTO  autoCompleteData;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public ServiceRegistryGroupedResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryGroupedResponseDTO(final List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems,
											 final List<ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO> servicesGroupedByServiceDefinitionAndInterface, 
											 final AutoCompleteDataResponseDTO autoCompleteData) {
		this.servicesGroupedBySystems = servicesGroupedBySystems;
		this.servicesGroupedByServiceDefinitionAndInterface = servicesGroupedByServiceDefinitionAndInterface;
		this.autoCompleteData = autoCompleteData;
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<ServicesGroupedBySystemsResponseDTO> getServicesGroupedBySystems() { return servicesGroupedBySystems; }
	public List<ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO> getServicesGroupedByServiceDefinitionAndInterface() { return servicesGroupedByServiceDefinitionAndInterface; }
	public AutoCompleteDataResponseDTO getAutoCompleteData() { return autoCompleteData; }

	//-------------------------------------------------------------------------------------------------	
	public void setServicesGroupedBySystems(final List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems) { this.servicesGroupedBySystems = servicesGroupedBySystems; }
	public void setServicesGroupedByServiceDefinitionAndInterface(final List<ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO> servicesGroupedByServiceDefinitionAndInterface) {
		this.servicesGroupedByServiceDefinitionAndInterface = servicesGroupedByServiceDefinitionAndInterface;
	}
	public void setAutoCompleteData(final AutoCompleteDataResponseDTO autoCompleteData) { this.autoCompleteData = autoCompleteData; }	
}
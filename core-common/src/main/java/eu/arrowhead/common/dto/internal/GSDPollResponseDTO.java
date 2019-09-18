package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;

public class GSDPollResponseDTO implements Serializable, ErrorWrapperDTO {

	//=================================================================================================
	// members
		
	private static final long serialVersionUID = 2276405706860702389L;
	
	private CloudResponseDTO providerCloud;
	private String requiredServiceDefinition;
	private List<String> availableInterfaces;
	private Integer numOfProviders; 
	private Map<String,String> serviceMetadata;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------	
	public GSDPollResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO(final CloudResponseDTO providerCloud, final String requiredServiceDefinition, final List<String> availableInterfaces, final Integer numOfProviders, 
							  final Map<String,String> serviceMetadata) {
		this.providerCloud = providerCloud;
		this.requiredServiceDefinition = requiredServiceDefinition;
		this.availableInterfaces = availableInterfaces;
		this.numOfProviders = numOfProviders;
		this.serviceMetadata = serviceMetadata;
	}

	//-------------------------------------------------------------------------------------------------	
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public String getRequiredServiceDefinition() { return requiredServiceDefinition; }
	public List<String> getAvailableInterfaces() { return availableInterfaces; }	
	public Integer getNumOfProviders() { return numOfProviders; }
	public Map<String,String> getServiceMetadata() { return serviceMetadata; }

	//-------------------------------------------------------------------------------------------------	
	public void setProviderCloud(final CloudResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setRequiredServiceDefinition(final String requiredServiceDefinition) { this.requiredServiceDefinition = requiredServiceDefinition; }
	public void setAvailableInterfaces(final List<String> availableInterfaces) { this.availableInterfaces = availableInterfaces; } 	
	public void setNumOfProviders(final Integer numOfProviders) { this.numOfProviders = numOfProviders; }
	public void setServiceMetadata(final Map<String,String> serviceMetadata) { this.serviceMetadata = serviceMetadata; }

	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	@Override
	public boolean isError() {
		return false;
	}
}
package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GSDPollResponseDTO implements Serializable {

	//=================================================================================================
	// members
		
	private static final long serialVersionUID = 2276405706860702389L;
	
	private CloudWithRelaysResponseDTO providerCloud;
	private String requiredServiceDefinition;
	private List<String> availableInterfaces;
	private Map<String,String> serviceMetadata;
	private Integer serviceVersion;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------	
	public GSDPollResponseDTO() {}
	
	public GSDPollResponseDTO(final CloudWithRelaysResponseDTO providerCloud, final String requiredServiceDefinition,
							 final List<String> availableInterfaces, final Map<String, String> serviceMetadata, final Integer serviceVersion) {
		this.providerCloud = providerCloud;
		this.requiredServiceDefinition = requiredServiceDefinition;
		this.availableInterfaces = availableInterfaces;
		this.serviceMetadata = serviceMetadata;
		this.serviceVersion = serviceVersion;
	}

	//-------------------------------------------------------------------------------------------------	
	public CloudWithRelaysResponseDTO getProviderCloud() { return providerCloud; }
	public String getRequiredServiceDefinition() { return requiredServiceDefinition; }
	public List<String> getAvailableInterfaces() { return availableInterfaces; }
	public Map<String, String> getServiceMetadata() { return serviceMetadata; }
	public Integer getServiceVersion() { return serviceVersion; }

	//-------------------------------------------------------------------------------------------------	
	public void setProviderCloud(final CloudWithRelaysResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setRequiredServiceDefinition(final String requiredServiceDefinition) { this.requiredServiceDefinition = requiredServiceDefinition; }
	public void setAvailableInterfaces(final List<String> availableInterfaces) { this.availableInterfaces = availableInterfaces; }
	public void setServiceMetadata(final Map<String, String> serviceMetadata) { this.serviceMetadata = serviceMetadata; }
	public void setServiceVersion(final Integer serviceVersion) { this.serviceVersion = serviceVersion; }
}
package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class ServiceRegistryRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5556318810695050255L;

	private ServiceDefinitionRequestDTO serviceDefinition;
	private SystemRequestDTO providerSystem;
	private String serviceUri;
	private String endOfValidity;
	private Boolean secure;
	private String metadata;
	private Integer version;
	private List<String> interfaces;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO getServiceDefinition() { return serviceDefinition; }
	public SystemRequestDTO getProviderSystem() { return providerSystem; }
	public String getServiceUri() { return serviceUri; }
	public String getEndOfValidity() { return endOfValidity; }
	public Boolean getSecure() { return secure; }
	public String getMetadata() { return metadata; }
	public Integer getVersion() { return version; }
	public List<String> getInterfaces() { return interfaces; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final ServiceDefinitionRequestDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setProviderSystem(final SystemRequestDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setSecure(final Boolean secure) { this.secure = secure; }
	public void setMetadata(final String metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }
	public void setInterfaces(final List<String> interfaces) { this.interfaces = interfaces; }
}
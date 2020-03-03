package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ServiceRegistryRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	public static final String KEY_RECOMMENDED_ORCHESTRATION_TIME = "recommendedOrchestrationTime"; // in seconds
	
	private static final long serialVersionUID = -3805773665976065056L;
	
	private String serviceDefinition;
	private SystemRequestDTO providerSystem;
	private String serviceUri;
	private String endOfValidity;
	private String secure;
	private Map<String,String> metadata;
	private Integer version;
	private List<String> interfaces;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	public SystemRequestDTO getProviderSystem() { return providerSystem; }
	public String getServiceUri() { return serviceUri; }
	public String getEndOfValidity() { return endOfValidity; }
	public String getSecure() { return secure; }
	public Map<String,String> getMetadata() { return metadata; }
	public Integer getVersion() { return version; }
	public List<String> getInterfaces() { return interfaces; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setProviderSystem(final SystemRequestDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setSecure(final String secure) { this.secure = secure; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }
	public void setInterfaces(final List<String> interfaces) { this.interfaces = interfaces; }
} 
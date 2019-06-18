package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ServiceQueryFormDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -2699823381165019336L;
	
	private String serviceDefinitionRequirement;
	private List<String> interfaceRequirements; // if specified at least one of the interfaces must match
	private List<ServiceSecurityType> securityRequirements; // if specified at least one of the types must match
	private Map<String,String> metadataRequirements; // if specified the whole content of the map must match
	private Integer versionRequirement; // if specified version must match
	private Integer minVersionRequirement; // if specified version must be equals or higher; ignored if versionRequirement is specified
	private Integer maxVersionRequirement; // if specified version must be equals or lower; ignored if versionRequirement is specified
	
	private boolean pingProviders = false;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinitionRequirement() { return serviceDefinitionRequirement; }
	public List<String> getInterfaceRequirements() { return interfaceRequirements; }
	public List<ServiceSecurityType> getSecurityRequirements() { return securityRequirements; }
	public Map<String,String> getMetadataRequirements() { return metadataRequirements; }
	public Integer getVersionRequirement() { return versionRequirement; }
	public Integer getMinVersionRequirement() { return minVersionRequirement; }
	public Integer getMaxVersionRequirement() { return maxVersionRequirement; }
	public boolean getPingProviders() { return pingProviders; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionRequirement(final String serviceDefinitionRequirement) { this.serviceDefinitionRequirement = serviceDefinitionRequirement; }
	public void setInterfaceRequirements(final List<String> interfaceRequirements) { this.interfaceRequirements = interfaceRequirements; }
	public void setSecurityRequirements(final List<ServiceSecurityType> securityRequirements) { this.securityRequirements = securityRequirements; }
	public void setMetadataRequirements(final Map<String,String> metadataRequirements) { this.metadataRequirements = metadataRequirements; }
	public void setVersionRequirement(final Integer versionRequirement) { this.versionRequirement = versionRequirement; }
	public void setMinVersionRequirement(final Integer minVersionRequirement) { this.minVersionRequirement = minVersionRequirement; }
	public void setMaxVersionRequirement(final Integer maxVersionRequirement) { this.maxVersionRequirement = maxVersionRequirement; }
	public void setPingProviders(final boolean pingProviders) { this.pingProviders = pingProviders; }
}
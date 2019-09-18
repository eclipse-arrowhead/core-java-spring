package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

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
	public ServiceQueryFormDTO() {}

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
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ServiceQueryFormDTO(final Builder builder) {
		this.serviceDefinitionRequirement = builder.serviceDefinitionRequirement;
		this.interfaceRequirements = builder.interfaceRequirements;
		this.securityRequirements = builder.securityRequirements;
		this.metadataRequirements = builder.metadataRequirements;
		this.versionRequirement = builder.versionRequirement;
		this.minVersionRequirement = builder.minVersionRequirement;
		this.maxVersionRequirement = builder.maxVersionRequirement;
		this.pingProviders = builder.pingProviders;
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	public static class Builder {
		
		//=================================================================================================
		// members

		private final String serviceDefinitionRequirement;
		private List<String> interfaceRequirements; 
		private List<ServiceSecurityType> securityRequirements; 
		private Map<String,String> metadataRequirements; 
		private Integer versionRequirement; 
		private Integer minVersionRequirement; 
		private Integer maxVersionRequirement; 
		
		private boolean pingProviders = false;

		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public Builder(final String serviceDefinitionRequirement) {
			Assert.isTrue(serviceDefinitionRequirement != null && !serviceDefinitionRequirement.isBlank(), "serviceDefinitionRequirement is null or blank.");
			this.serviceDefinitionRequirement = serviceDefinitionRequirement;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder interfaces(final String... interfaceRequirements) {
			this.interfaceRequirements = interfaceRequirements == null || interfaceRequirements.length == 0 ? null : Arrays.asList(interfaceRequirements);
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public Builder security(final ServiceSecurityType... securityRequirements) {
			this.securityRequirements = securityRequirements == null || securityRequirements.length == 0 ? null : Arrays.asList(securityRequirements);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder metadata(final Map<String,String> metadataRequirements) {
			this.metadataRequirements = metadataRequirements;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder metadata(final String key, final String value) {
			Assert.isTrue(key != null && !key.isBlank(), "Key is null or blank");
			
			if (this.metadataRequirements == null) {
				this.metadataRequirements = new HashMap<>();
			}
			this.metadataRequirements.put(key, value);
			
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder version(final Integer versionRequirement) {
			this.versionRequirement = versionRequirement;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder version(final Integer minVersionRequirement, final Integer maxVersionRequirement) {
			this.minVersionRequirement = minVersionRequirement;
			this.maxVersionRequirement = maxVersionRequirement;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder pingProviders(final boolean pingProviders) {
			this.pingProviders = pingProviders;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public ServiceQueryFormDTO build() {
			return new ServiceQueryFormDTO(this);
		}
	}
}
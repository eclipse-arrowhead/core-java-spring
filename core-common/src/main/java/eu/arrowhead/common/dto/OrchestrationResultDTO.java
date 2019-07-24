package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

public class OrchestrationResultDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6425527229383724551L;
	
	private SystemResponseDTO provider;
	private ServiceDefinitionResponseDTO service;
	private String serviceUri;
	private ServiceSecurityType secure;
	private Map<String,String> metadata;
	private List<ServiceInterfaceResponseDTO> interfaces;
	private Integer version;
	
	private String authorizationToken;
	private List<OrchestratorWarnings> warnings = new ArrayList<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO(final SystemResponseDTO provider, final ServiceDefinitionResponseDTO service, final String serviceUri, final ServiceSecurityType secure, 
								  final Map<String,String> metadata, final List<ServiceInterfaceResponseDTO> interfaces, final Integer version, final String authorizationToken,
								  final List<OrchestratorWarnings> warnings) {
		Assert.notNull(provider, "provider is null.");
		Assert.notNull(service, "service is null.");
		Assert.isTrue(interfaces != null && !interfaces.isEmpty(), "interfaces is null or empty.");
		
		this.provider = provider;
		this.service = service;
		this.serviceUri = serviceUri;
		this.secure = secure;
		this.metadata = metadata;
		this.interfaces = interfaces;
		this.version = version;
		this.authorizationToken = authorizationToken;
		this.warnings = warnings;
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO(final SystemResponseDTO provider, final ServiceDefinitionResponseDTO service, final String serviceUri, final ServiceSecurityType secure, 
								  final Map<String,String> metadata, final List<ServiceInterfaceResponseDTO> interfaces, final Integer version) {
		this(provider, service, serviceUri, secure, metadata, interfaces, version, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getProvider() { return provider; }
	public ServiceDefinitionResponseDTO getService() { return service; }
	public String getServiceUri() { return serviceUri; }
	public ServiceSecurityType getSecure() { return secure; }
	public Map<String,String> getMetadata() { return metadata; }
	public List<ServiceInterfaceResponseDTO> getInterfaces() { return interfaces; }
	public Integer getVersion() { return version; }
	public String getAuthorizationToken() { return authorizationToken; }
	public List<OrchestratorWarnings> getWarnings() { return warnings; }
	
	//-------------------------------------------------------------------------------------------------
	public void setProvider(final SystemResponseDTO provider) { this.provider = provider; }
	public void setService(final ServiceDefinitionResponseDTO service) { this.service = service; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setSecure(final ServiceSecurityType secure) { this.secure = secure; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setInterfaces(final List<ServiceInterfaceResponseDTO> interfaces) { this.interfaces = interfaces; }
	public void setVersion(final Integer version) { this.version = version; }
	public void setAuthorizationToken(final String authorizationToken) { this.authorizationToken = authorizationToken; }
	public void setWarnings(final List<OrchestratorWarnings> warnings) { this.warnings = warnings; }
}
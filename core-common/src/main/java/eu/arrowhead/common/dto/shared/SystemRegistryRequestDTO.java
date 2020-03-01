package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -635438605292398404L;
	
	private SystemRequestDTO system;
	private DeviceRequestDTO provider;
	private String endOfValidity;
	private Map<String,String> metadata;
	private Integer version;

	//=================================================================================================
	// methods

	public SystemRegistryRequestDTO() {
	}

	public SystemRegistryRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
									final Map<String, String> metadata, final Integer version) {
		this.system = system;
		this.provider = provider;
		this.endOfValidity = endOfValidity;
		this.metadata = metadata;
		this.version = version;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getSystem() { return system; }
	public DeviceRequestDTO getProvider() { return provider; }
	public String getEndOfValidity() { return endOfValidity; }
	public Map<String,String> getMetadata() { return metadata; }
	public Integer getVersion() { return version; }

	//-------------------------------------------------------------------------------------------------
	public void setSystem(final SystemRequestDTO system) { this.system = system; }
	public void setProvider(final DeviceRequestDTO provider) { this.provider = provider; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }
}
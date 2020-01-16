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
	
	private SystemResponseDTO system;
	private DeviceResponseDTO provider;
	private String endOfValidity;
	private Map<String,String> metadata;
	private int version;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getSystem() { return system; }
	public DeviceResponseDTO getProvider() { return provider; }
	public String getEndOfValidity() { return endOfValidity; }
	public Map<String,String> getMetadata() { return metadata; }
	public int getVersion() { return version; }

	//-------------------------------------------------------------------------------------------------
	public void setSystem(final SystemResponseDTO system) { this.system = system; }
	public void setProvider(final DeviceResponseDTO provider) { this.provider = provider; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final int version) { this.version = version; }
}
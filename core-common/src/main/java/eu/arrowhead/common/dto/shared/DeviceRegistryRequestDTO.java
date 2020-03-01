package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class DeviceRegistryRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -635438605292398404L;
	
	private DeviceRequestDTO device;
	private String endOfValidity;
	private Map<String,String> metadata;
	private Integer version;

	//=================================================================================================
	// methods

	public DeviceRegistryRequestDTO() {
	}

	//-------------------------------------------------------------------------------------------------
	public DeviceRegistryRequestDTO(final DeviceRequestDTO device, final String endOfValidity, final Map<String, String> metadata, final Integer version) {
		this.device = device;
		this.endOfValidity = endOfValidity;
		this.metadata = metadata;
		this.version = version;
	}

	//-------------------------------------------------------------------------------------------------
	public DeviceRequestDTO getDevice() { return device; }
	public String getEndOfValidity() { return endOfValidity; }
	public Map<String,String> getMetadata() { return metadata; }
	public Integer getVersion() { return version; }

	//-------------------------------------------------------------------------------------------------
	public void setDevice(final DeviceRequestDTO device) { this.device = device; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }
}
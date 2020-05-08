package eu.arrowhead.common.dto.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.arrowhead.common.dto.shared.DeviceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DeviceRegistryListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 3892383727230105100L;

	private List<DeviceRegistryResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceRegistryListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public DeviceRegistryListResponseDTO(final List<DeviceRegistryResponseDTO> data) {
		this(data, data.size());
	}

	//-------------------------------------------------------------------------------------------------
	public DeviceRegistryListResponseDTO(final List<DeviceRegistryResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<DeviceRegistryResponseDTO> getData() {return data;}
	public long getCount() {return count;}
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<DeviceRegistryResponseDTO> data) {this.data = data;}
	public void setCount(final long count) {this.count = count;}	
}
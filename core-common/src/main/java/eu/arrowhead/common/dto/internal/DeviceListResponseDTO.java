package eu.arrowhead.common.dto.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DeviceListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1661484009332215820L;

	private List<DeviceResponseDTO> data = new ArrayList<>();
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public DeviceListResponseDTO(final List<DeviceResponseDTO> deviceResponseDTOList, final int totalNumberOfSystems) {
		super();
		this.data = deviceResponseDTOList;
		this.count = totalNumberOfSystems;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<DeviceResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<DeviceResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}
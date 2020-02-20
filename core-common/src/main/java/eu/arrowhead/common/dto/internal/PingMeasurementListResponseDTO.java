package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class PingMeasurementListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1261528929899029505L;	

	private List<PingMeasurementResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementListResponseDTO(final List<PingMeasurementResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<PingMeasurementResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<PingMeasurementResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}
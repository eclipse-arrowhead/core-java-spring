package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class QoSInterDirectPingMeasurementListResponseDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2079369313852875325L;
	
	private List<QoSInterDirectPingMeasurementResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectPingMeasurementListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectPingMeasurementListResponseDTO(final List<QoSInterDirectPingMeasurementResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<QoSInterDirectPingMeasurementResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<QoSInterDirectPingMeasurementResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class QoSIntraPingMeasurementListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1261528929899029505L;	

	private List<QoSIntraPingMeasurementResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementListResponseDTO(final List<QoSIntraPingMeasurementResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<QoSIntraPingMeasurementResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<QoSIntraPingMeasurementResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}
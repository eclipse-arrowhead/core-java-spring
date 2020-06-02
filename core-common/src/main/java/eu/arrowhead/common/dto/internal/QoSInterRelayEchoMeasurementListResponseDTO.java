package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class QoSInterRelayEchoMeasurementListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 7632688877032376191L;
	
	private List<QoSInterRelayEchoMeasurementResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementListResponseDTO(final List<QoSInterRelayEchoMeasurementResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<QoSInterRelayEchoMeasurementResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<QoSInterRelayEchoMeasurementResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}

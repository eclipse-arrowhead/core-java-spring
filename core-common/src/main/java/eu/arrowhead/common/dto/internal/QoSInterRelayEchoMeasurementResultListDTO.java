package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class QoSInterRelayEchoMeasurementResultListDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8872059871518589197L;
	
	private List<QoSInterRelayEchoMeasurementResultDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementResultListDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementResultListDTO(final List<QoSInterRelayEchoMeasurementResultDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<QoSInterRelayEchoMeasurementResultDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<QoSInterRelayEchoMeasurementResultDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}

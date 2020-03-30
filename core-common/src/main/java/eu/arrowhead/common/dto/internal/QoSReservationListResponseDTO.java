package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class QoSReservationListResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 4655717418817449554L;

	private List<QoSReservationResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSReservationListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSReservationListResponseDTO(final List<QoSReservationResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<QoSReservationResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<QoSReservationResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	

}

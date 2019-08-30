package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class EventFilterListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -677758800436027812L;
	
	private List<EventFilterResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventFilterListResponseDTO(final List<EventFilterResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<EventFilterResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<EventFilterResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}

package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class CloudWithRelaysListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6232775950375371424L;
	
	private List<CloudWithRelaysResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysListResponseDTO(final List<CloudWithRelaysResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<CloudWithRelaysResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<CloudWithRelaysResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}
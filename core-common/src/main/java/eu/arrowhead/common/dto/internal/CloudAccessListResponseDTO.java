package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class CloudAccessListResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 1493703097974010312L;

	private List<CloudAccessResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public CloudAccessListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudAccessListResponseDTO(final List<CloudAccessResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<CloudAccessResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<CloudAccessResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	

}

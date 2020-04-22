package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class CloudWithRelaysAndPublicRelaysListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 7672271505929868952L;

	private List<CloudWithRelaysAndPublicRelaysResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysListResponseDTO(final List<CloudWithRelaysAndPublicRelaysResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<CloudWithRelaysAndPublicRelaysResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<CloudWithRelaysAndPublicRelaysResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
}
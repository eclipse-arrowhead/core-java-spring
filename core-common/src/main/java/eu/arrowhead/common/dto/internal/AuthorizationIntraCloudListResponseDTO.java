package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class AuthorizationIntraCloudListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -3996357127599109268L;
	
	private List<AuthorizationIntraCloudResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudListResponseDTO(final List<AuthorizationIntraCloudResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<AuthorizationIntraCloudResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<AuthorizationIntraCloudResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}
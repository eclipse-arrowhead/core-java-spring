package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationInterCloudListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 893654899355518416L;
	
	private List<AuthorizationInterCloudResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudListResponseDTO(final List<AuthorizationInterCloudResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<AuthorizationInterCloudResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<AuthorizationInterCloudResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}
package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class InterCloudAuthorizationListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 893654899355518416L;
	
	private List<InterCloudAuthorizationResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudAuthorizationListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudAuthorizationListResponseDTO(final List<InterCloudAuthorizationResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<InterCloudAuthorizationResponseDTO> getData() {return data;}
	public long getCount() {return count;}
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<InterCloudAuthorizationResponseDTO> data) {this.data = data;}
	public void setCount(final long count) {this.count = count;}	
}

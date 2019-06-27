package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class IntraCloudAuthorizationListResponseDTO implements Serializable {

	private static final long serialVersionUID = -3996357127599109268L;

	//=================================================================================================
	// members
	
	private List<IntraCloudAuthorizationResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationListResponseDTO(final List<IntraCloudAuthorizationResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<IntraCloudAuthorizationResponseDTO> getData() {return data;}
	public long getCount() {return count;}
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<IntraCloudAuthorizationResponseDTO> data) {this.data = data;}
	public void setCount(final long count) {this.count = count;}	
}

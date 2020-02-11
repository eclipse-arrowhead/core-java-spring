package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

public class SubscriptionListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -677758800436027812L;
	
	private List<SubscriptionResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionListResponseDTO(final List<SubscriptionResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<SubscriptionResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<SubscriptionResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}
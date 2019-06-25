package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class InterCloudAuthorizationRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5349360773532348565L;
	
	private Long cloudId; 
	private List<Long> serviceDefinitionIdList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudAuthorizationRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public InterCloudAuthorizationRequestDTO(long cloudId,
			List<Long> serviceDefinitionIdList) {

		this.cloudId = cloudId;
		this.serviceDefinitionIdList = serviceDefinitionIdList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() { return cloudId; }
	public List<Long> getServiceDefinitionIdList() { return serviceDefinitionIdList;	}
	
	//-------------------------------------------------------------------------------------------------
	public void setCloudId(Long cloudId) { this.cloudId = cloudId; }
	public void setServiceDefinitionIdList(List<Long> serviceDefinitionIdList) { this.serviceDefinitionIdList = serviceDefinitionIdList; }
}




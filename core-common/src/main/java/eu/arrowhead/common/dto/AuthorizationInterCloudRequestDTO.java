package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationInterCloudRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5349360773532348565L;
	
	private Long cloudId; 
	private List<Long> serviceDefinitionIdList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public AuthorizationInterCloudRequestDTO(final Long cloudId,
			final List<Long> serviceDefinitionIdList) {

		this.cloudId = cloudId;
		this.serviceDefinitionIdList = serviceDefinitionIdList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() { return cloudId; }
	public List<Long> getServiceDefinitionIdList() { return serviceDefinitionIdList;	}
	
	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final Long cloudId) { this.cloudId = cloudId; }
	public void setServiceDefinitionIdList(final List<Long> serviceDefinitionIdList) { this.serviceDefinitionIdList = serviceDefinitionIdList; }
}




package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class QoSTemporaryLockRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7584022504960220540L;
	
	private SystemRequestDTO requester;
	private List<OrchestrationResultDTO> orList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockRequestDTO(final SystemRequestDTO requester, final List<OrchestrationResultDTO> orList) {		
		this.requester = requester;
		this.orList = orList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getRequester() { return requester; }
	public List<OrchestrationResultDTO> getOrList() { return orList; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequester(final SystemRequestDTO requester) { this.requester = requester; }
	public void setOrList(final List<OrchestrationResultDTO> orList) { this.orList = orList; }
}

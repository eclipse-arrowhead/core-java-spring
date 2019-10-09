package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Map;

public class OrchestratorStoreModifyPriorityRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 9141560687987073900L;

	private Map<Long,Integer> priorityMap;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreModifyPriorityRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreModifyPriorityRequestDTO(final Map<Long,Integer> priorityMap) {
		this.priorityMap = priorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<Long,Integer> getPriorityMap() { return priorityMap; }

	//-------------------------------------------------------------------------------------------------
	public void setPriorityMap(final Map<Long,Integer> priorityMap) { this.priorityMap = priorityMap; }
}
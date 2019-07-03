package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;

public class OrchestratorStoreModifyPriorityRequestDTO implements Serializable {

	private static final long serialVersionUID = 9141560687987073900L;

	//=================================================================================================
	// members
	

	Map<Long, Integer> priorityMap;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreModifyPriorityRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreModifyPriorityRequestDTO(Map<Long, Integer> priorityMap) {
		super();
		this.priorityMap = priorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<Long, Integer> getPriorityMap() { return priorityMap; }

	//-------------------------------------------------------------------------------------------------
	public void setPriorityMap(Map<Long, Integer> priorityMap) { this.priorityMap = priorityMap; }

}

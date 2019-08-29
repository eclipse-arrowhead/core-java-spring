package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;

public class EventPublishResponseDTO implements Serializable {
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3603417813940031123L;
	
	private Map<String, Boolean> attemptedDeliveryMap;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishResponseDTO(Map<String, Boolean> attemptedDeliveryMap) {
		
		this.attemptedDeliveryMap = attemptedDeliveryMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<String, Boolean> getAttemptedDeliveryMap() {	return attemptedDeliveryMap; }


	//-------------------------------------------------------------------------------------------------
	public void setAttemptedDeliveryMap(Map<String, Boolean> attemptedDeliveryMap) { this.attemptedDeliveryMap = attemptedDeliveryMap; }

}

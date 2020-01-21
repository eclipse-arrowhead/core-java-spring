package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class EventTypeRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1177806380638957855L;
	
	private String eventTypeName;
		
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public EventTypeRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventTypeRequestDTO(final String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getEventTypeName() { return eventTypeName; }

	//-------------------------------------------------------------------------------------------------
	public void setEventTypeName(final String eventTypeName) { this.eventTypeName = eventTypeName; }
}
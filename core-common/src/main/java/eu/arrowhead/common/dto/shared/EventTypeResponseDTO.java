package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class EventTypeResponseDTO implements Serializable {
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6112584843302681040L;
	
	private long id;
	private String eventTypeName;
	private String createdAt;
	private String updatedAt;
		
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public EventTypeResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public EventTypeResponseDTO(final long id, final String eventTypeName, final String createdAt, final String updatedAt) {
		this.id = id;
		this.eventTypeName = eventTypeName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getEventTypeName() { return eventTypeName; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setEventTypeName(final String eventTypeName) { this.eventTypeName = eventTypeName; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}
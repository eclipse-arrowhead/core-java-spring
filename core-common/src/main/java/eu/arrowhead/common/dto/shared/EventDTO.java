package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;

public class EventDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6788896769029136585L;
	
	private String eventType;
	private Map<String,String> metaData;
	private String payload;
	private String timeStamp;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventDTO(final String eventType, final Map<String,String> metaData, final String payload, final String timeStamp) {
		this.eventType = eventType;
		this.metaData = metaData;
		this.payload = payload;
		this.timeStamp = timeStamp;
	}	

	//-------------------------------------------------------------------------------------------------
	public String getEventType() {	return eventType; }
	public Map<String,String> getMetaData() { return metaData; }
	public String getPayload() { return payload; }
	public String getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final String eventType) { this.eventType = eventType; }
	public void setMetaData( final Map<String,String> metaData ) { this.metaData = metaData; }
	public void setPayload(final String startDate) { this.payload = startDate; }
	public void setTimeStamp(final String endDate) { this.timeStamp = endDate; }
}
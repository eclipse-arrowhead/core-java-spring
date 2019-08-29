package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;

public class EventPublishRequestDTO implements Serializable {
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -5708154911939798799L;
	
	private String eventType;
	private SystemRequestDTO source;
	private Map<String, String> metaData;
	private String deliveryCompleteUri;
	private String payload;
	private String timeStamp;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishRequestDTO( final String eventType, final SystemRequestDTO source, final Map<String, String> metaData,
			final String deliveryCompleteUri, final String payload, final String timeStamp ) {
		
		this.eventType = eventType;
		this.source = source;
		this.metaData = metaData;
		this.deliveryCompleteUri = deliveryCompleteUri;
		this.payload = payload;
		this.timeStamp = timeStamp;
	}	

	//-------------------------------------------------------------------------------------------------
	public String getEventType() {	return eventType; }
	public SystemRequestDTO getSource() {	return source; }
	public Map<String, String> getMetaData() { return metaData; }
	public String getDeliveryCompleteUri() { return deliveryCompleteUri; }
	public String getPayload() { return payload; }
	public String getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType( final String eventType ) { this.eventType = eventType; }
	public void setSource( final SystemRequestDTO subscriberSystem ) { this.source = subscriberSystem; }
	public void setMetaData( final Map<String, String> metaData ) { this.metaData = metaData; }
	public void setDeliveryCompleteUri( final String notifyUri ) { this.deliveryCompleteUri = notifyUri; }
	public void setPayload( final String startDate ) { this.payload = startDate; }
	public void setTimeStamp( final String endDate ) { this.timeStamp = endDate; }

}

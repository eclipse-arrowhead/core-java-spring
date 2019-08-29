package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class EventFilterResponseDTO implements Serializable{

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3014273687545696205L;
	
	private long id;
	private EventTypeRequestDTO eventType;
	private SystemRequestDTO subscriberSystem;
	private Map<String, String> filterMetaData;
	private String notifyUri;
	private boolean matchMetaData;
	private String startDate;
	private String endDate;
	private Set<SystemRequestDTO> sources;
	private String createdAt;
	private String updatedAt;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventFilterResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventFilterResponseDTO( final long id, final EventTypeRequestDTO eventType, final SystemRequestDTO subscriberSystem,
			final Map<String, String> filterMetaData, final String notifyUri, final boolean matchMetaData, final String startDate,
			final String endDate, final Set<SystemRequestDTO> sources, final String createdAt, final String updatedAt ) {
		super();
		this.id = id;
		this.eventType = eventType;
		this.subscriberSystem = subscriberSystem;
		this.filterMetaData = filterMetaData;
		this.notifyUri = notifyUri;
		this.matchMetaData = matchMetaData;
		this.startDate = startDate;
		this.endDate = endDate;
		this.sources = sources;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public EventTypeRequestDTO getEventType() {	return eventType; }
	public SystemRequestDTO getSubscriberSystem() {	return subscriberSystem; }
	public Map<String, String> getFilterMetaData() { return filterMetaData; }
	public String getNotifyUri() { return notifyUri; }
	public Boolean getMatchMetaData() {	return matchMetaData; }
	public String getStartDate() { return startDate; }
	public String getEndDate() { return endDate; }
	public Set<SystemRequestDTO> getSources() {	return sources;	}
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId( final long id ) { this.id = id; }
	public void setEventType( final EventTypeRequestDTO eventType ) { this.eventType = eventType; }
	public void setSubscriberSystem( final SystemRequestDTO subscriberSystem ) { this.subscriberSystem = subscriberSystem; }
	public void setFilterMetaData( final Map<String, String> filterMetaData ) { this.filterMetaData = filterMetaData; }
	public void setNotifyUri( final String notifyUri ) { this.notifyUri = notifyUri; }
	public void setMatchMetaData( final Boolean matchMetaData ) { this.matchMetaData = matchMetaData; }
	public void setStartDate( final String startDate ) { this.startDate = startDate; }
	public void setEndDate( final String endDate ) { this.endDate = endDate; }
	public void setSources(  final Set<SystemRequestDTO> sources ) { this.sources = sources;	}
	public void setCreatedAt( final String createdAt ) { this.createdAt = createdAt; }
	public void setUpdatedAt( final String updatedAt ) { this.updatedAt = updatedAt; }
	
}

package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class EventFilterRequestDTO implements Serializable{
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6411715397801888776L;
	
	private EventTypeRequestDTO eventType;
	private SystemRequestDTO subscriberSystem;
	private Map<String, String> filterMetaData;
	private String notifyUri;
	private Boolean matchMetaData;
	private String startDate;
	private String endDate;
	private Set<SystemRequestDTO> sources;
	
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventFilterRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------

	public EventFilterRequestDTO( EventTypeRequestDTO eventType, SystemRequestDTO subscriberSystem,
			Map<String, String> filterMetaData, String notifyUri, Boolean matchMetaData, String startDate,
			String endDate, Set<SystemRequestDTO> sources ) {
		
		this.eventType = eventType;
		this.subscriberSystem = subscriberSystem;
		this.filterMetaData = filterMetaData;
		this.notifyUri = notifyUri;
		this.matchMetaData = matchMetaData;
		this.startDate = startDate;
		this.endDate = endDate;
		this.sources = sources;
	}

	//-------------------------------------------------------------------------------------------------
	public EventTypeRequestDTO getEventType() {	return eventType; }
	public SystemRequestDTO getSubscriberSystem() {	return subscriberSystem; }
	public Map<String, String> getFilterMetaData() { return filterMetaData; }
	public String getNotifyUri() { return notifyUri; }
	public Boolean getMatchMetaData() {	return matchMetaData; }
	public String getStartDate() { return startDate; }
	public String getEndDate() { return endDate; }
	public Set<SystemRequestDTO> getSources() {	return sources;	}

	//-------------------------------------------------------------------------------------------------
	public void setEventType( EventTypeRequestDTO eventType ) { this.eventType = eventType; }
	public void setSubscriberSystem( SystemRequestDTO subscriberSystem ) { this.subscriberSystem = subscriberSystem; }
	public void setFilterMetaData(Map<String, String> filterMetaData) { this.filterMetaData = filterMetaData; }
	public void setNotifyUri(String notifyUri) { this.notifyUri = notifyUri; }
	public void setMatchMetaData(Boolean matchMetaData) { this.matchMetaData = matchMetaData; }
	public void setStartDate(String startDate) { this.startDate = startDate; }
	public void setEndDate(String endDate) { this.endDate = endDate; }
	public void setSources(Set<SystemRequestDTO> sources) { this.sources = sources;	}

}

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class SubscriptionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6411715397801888776L;
	
	private String eventType;
	private SystemRequestDTO subscriberSystem;
	private Map<String,String> filterMetaData;
	private String notifyUri;
	private boolean matchMetaData = false;
	private String startDate;
	private String endDate;
	private Set<SystemRequestDTO> sources;
	
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionRequestDTO(final String eventType, final SystemRequestDTO subscriberSystem, final Map<String,String> filterMetaData, final String notifyUri, final boolean matchMetaData,
								  final String startDate, final String endDate, final Set<SystemRequestDTO> sources ) {
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
	public String getEventType() {	return eventType; }
	public SystemRequestDTO getSubscriberSystem() {	return subscriberSystem; }
	public Map<String,String> getFilterMetaData() { return filterMetaData; }
	public String getNotifyUri() { return notifyUri; }
	public boolean getMatchMetaData() {	return matchMetaData; }
	public String getStartDate() { return startDate; }
	public String getEndDate() { return endDate; }
	public Set<SystemRequestDTO> getSources() {	return sources;	}

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final String eventType) { this.eventType = eventType; }
	public void setSubscriberSystem(final SystemRequestDTO subscriberSystem) { this.subscriberSystem = subscriberSystem; }
	public void setFilterMetaData(final Map<String,String> filterMetaData) { this.filterMetaData = filterMetaData; }
	public void setNotifyUri(final String notifyUri) { this.notifyUri = notifyUri; }
	public void setMatchMetaData(final boolean matchMetaData) { this.matchMetaData = matchMetaData; }
	public void setStartDate(final String startDate) { this.startDate = startDate; }
	public void setEndDate(final String endDate) { this.endDate = endDate; }
	public void setSources(final Set<SystemRequestDTO> sources) { this.sources = sources; }
}
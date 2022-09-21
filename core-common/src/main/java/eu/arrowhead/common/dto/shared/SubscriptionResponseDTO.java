/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SubscriptionResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3014273687545696205L;
	
	private long id;
	private EventTypeResponseDTO eventType;
	private SystemResponseDTO subscriberSystem;
	private Map<String,String> filterMetaData;
	private String notifyUri;
	private boolean matchMetaData;
	private String startDate;
	private String endDate;
	private Set<SystemResponseDTO> sources;
	private String createdAt;
	private String updatedAt;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO(final long id, final EventTypeResponseDTO eventType, final SystemResponseDTO subscriberSystem, final Map<String,String> filterMetaData, final String notifyUri,
								   final boolean matchMetaData, final String startDate, final String endDate, final Set<SystemResponseDTO> sources, final String createdAt, final String updatedAt) {
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
	public EventTypeResponseDTO getEventType() { return eventType; }
	public SystemResponseDTO getSubscriberSystem() { return subscriberSystem; }
	public Map<String,String> getFilterMetaData() { return filterMetaData; }
	public String getNotifyUri() { return notifyUri; }
	public boolean getMatchMetaData() {	return matchMetaData; }
	public String getStartDate() { return startDate; }
	public String getEndDate() { return endDate; }
	public Set<SystemResponseDTO> getSources() { return sources; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setEventType(final EventTypeResponseDTO eventType) { this.eventType = eventType; }
	public void setSubscriberSystem(final SystemResponseDTO subscriberSystem) { this.subscriberSystem = subscriberSystem; }
	public void setFilterMetaData(final Map<String,String> filterMetaData) { this.filterMetaData = filterMetaData; }
	public void setNotifyUri(final String notifyUri) { this.notifyUri = notifyUri; }
	public void setMatchMetaData(final boolean matchMetaData) { this.matchMetaData = matchMetaData; }
	public void setStartDate(final String startDate) { this.startDate = startDate; }
	public void setEndDate(final String endDate) { this.endDate = endDate; }
	public void setSources(final Set<SystemResponseDTO> sources) { this.sources = sources;	}
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}
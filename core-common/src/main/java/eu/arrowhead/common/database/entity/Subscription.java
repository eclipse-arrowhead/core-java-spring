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

package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "eventTypeId", "systemId" }))
public class Subscription {

	//=================================================================================================
	// members

	public static final List<String> SORTABLE_FIELDS_BY = List.of( "id", "updatedAt", "createdAt"); //NOSONAR
	
	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne( fetch = FetchType.EAGER)
	@JoinColumn( name = "eventTypeId", referencedColumnName = "id", nullable = false)
	private EventType eventType;
	
	@ManyToOne( fetch = FetchType.EAGER)
	@JoinColumn( name = "systemId", referencedColumnName = "id", nullable = false)
	private System subscriberSystem;
	
	@Column(nullable = true, columnDefinition = "MEDIUMTEXT")
	private String filterMetaData;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String notifyUri;
	
	@Column( nullable = false)
	private boolean matchMetaData = false;
	
	@Column( nullable = false)
	private boolean onlyPredefinedPublishers = false;
	
	@Column(nullable = true)
	private ZonedDateTime startDate;
	
	@Column(nullable = true)
	private ZonedDateTime endDate;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "subscriptionEntry", fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<SubscriptionPublisherConnection> publisherConnections = new HashSet<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Subscription() {}
	
	//-------------------------------------------------------------------------------------------------
	public Subscription(final EventType eventType, final System subscriberSystem, final String filterMetaData, final String notifyUri, final boolean matchMetaData,
					    final boolean onlyPredefinedPublishers, final ZonedDateTime startDate, final ZonedDateTime endDate) {
		this.eventType = eventType;
		this.subscriberSystem = subscriberSystem;
		this.filterMetaData = filterMetaData;
		this.notifyUri = notifyUri;
		this.matchMetaData = matchMetaData;
		this.onlyPredefinedPublishers = onlyPredefinedPublishers;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}

	//-------------------------------------------------------------------------------------------------
	@PreUpdate
	public void onUpdate() { 
		this.updatedAt = ZonedDateTime.now();
	}
	
	//-------------------------------------------------------------------------------------------------	
	public long getId() { return id; }
	public EventType getEventType() { return eventType;	}
	public System getSubscriberSystem() { return subscriberSystem; }
	public String getFilterMetaData() { return filterMetaData; }
	public String getNotifyUri() { return notifyUri; }
	public boolean isMatchMetaData() { return matchMetaData; }
	public boolean isOnlyPredefinedPublishers() { return onlyPredefinedPublishers; }
	public ZonedDateTime getStartDate() { return startDate; }
	public ZonedDateTime getEndDate() { return endDate; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }	
	public Set<SubscriptionPublisherConnection> getPublisherConnections() {	return publisherConnections; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setEventType(final EventType eventType) {	this.eventType = eventType;	}
	public void setSubscriberSystem(final System subscriberSystem) { this.subscriberSystem = subscriberSystem; }
	public void setFilterMetaData(final String filterMetaData) { this.filterMetaData = filterMetaData; }
	public void setNotifyUri(final String notifyUri) { this.notifyUri = notifyUri; }
	public void setMatchMetaData(final boolean matchMetaData) { this.matchMetaData = matchMetaData; }
	public void setOnlyPredefinedPublishers(final boolean onlyPredefinedPublishers) { this.onlyPredefinedPublishers = onlyPredefinedPublishers; }
	public void setStartDate(final ZonedDateTime startDate) { this.startDate = startDate; }
	public void setEndDate(final ZonedDateTime endDate) { this.endDate = endDate; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setPublisherConnections(final Set<SubscriptionPublisherConnection> publisherConnections) { this.publisherConnections = publisherConnections; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Subscription [id=" + id + ", eventType=" + eventType + ", subscriberSystem=" + subscriberSystem
				+ ", matchMetaData=" + matchMetaData + ", onlyPredefinedPublishers=" + onlyPredefinedPublishers +  "]";
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		final Subscription other = (Subscription) obj;
		
		return id == other.id;
	}
}
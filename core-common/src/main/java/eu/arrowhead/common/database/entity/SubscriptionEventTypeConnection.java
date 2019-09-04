package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"subscriptionId", "eventTypeId"}))
public class SubscriptionEventTypeConnection {
	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "subscriptionId", referencedColumnName = "id", nullable = false)
	private Subscription subscriptionEntry;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "eventTypeId", referencedColumnName = "id", nullable = false)
	private EventType eventType;
	
	@Column(nullable = false )
	private boolean authorized;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionEventTypeConnection() {}

	//-------------------------------------------------------------------------------------------------
	public SubscriptionEventTypeConnection(final Subscription subscriptionEntry, final EventType eventType) {
		this.subscriptionEntry = subscriptionEntry;
		this.eventType = eventType;
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
	public Subscription getSubscriptionEntry() { return subscriptionEntry; }
	public EventType getEventType() { return eventType; }
	public boolean isAuhtorized() { return authorized; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSubscriptionEntry(final Subscription subscriptionEntry) { this.subscriptionEntry = subscriptionEntry; }
	public void setEventType(final EventType eventType) { this.eventType = eventType; }
	public void setAuthorized(final boolean authorized) { this.authorized = authorized; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "SubscriptionEventTypeConnection [id = " + id + 
				", subscriptionEntry = " + subscriptionEntry + 
				", eventType = " + eventType + 
				", authorized = " + authorized + "]";
	}
}

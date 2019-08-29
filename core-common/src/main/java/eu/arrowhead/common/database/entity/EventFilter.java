package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.List;

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

@Entity
public class EventFilter {

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
	@JoinColumn( name = "consumerSystemId", referencedColumnName = "id", nullable = false)
	private System consumerSystem;
	
	@Column(nullable = true)
	private String sources;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String filterMetaData;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String notifyUri;
	
	@Column( nullable = false)
	private boolean matchMetaData;
	
	@Column(nullable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime startDate;
	
	@Column(nullable = true, columnDefinition = "TIMESTAMP")
	private ZonedDateTime endDate;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public EventFilter() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventFilter(final EventType eventType, final System consumerSystem, final String filterMetaData,
			final String notifyUri, final boolean matchMetaData, final ZonedDateTime startDate, final ZonedDateTime endDate, final String sources) {

		this.eventType = eventType;
		this.consumerSystem = consumerSystem;
		this.filterMetaData = filterMetaData;
		this.notifyUri = notifyUri;
		this.matchMetaData = matchMetaData;
		this.startDate = startDate;
		this.endDate = endDate;
		this.sources = sources;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}

	//-------------------------------------------------------------------------------------------------
	@PreUpdate
	public void onUpdate() { this.updatedAt = ZonedDateTime.now(); }
	public long getId() { return id; }
	public EventType getEventType() { return eventType;	}
	public System getConsumerSystem() { return consumerSystem; }
	public String getFilterMetaData() { return filterMetaData; }
	public String getNotifyUri() { return notifyUri; }
	public boolean isMatchMetaData() { return matchMetaData; }
	public ZonedDateTime getStartDate() { return startDate; }
	public ZonedDateTime getEndDate() { return endDate; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public String getSources() { return sources; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId( final long id) { this.id = id; }
	public void setEventType( final EventType eventType) {	this.eventType = eventType;	}
	public void setConsumerSystem( final System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setFilterMetaData( final String filterMetaData) { this.filterMetaData = filterMetaData; }
	public void setNotifyUri( final String notifyUri) { this.notifyUri = notifyUri; }
	public void setMatchMetaData( final boolean matchMetaData) { this.matchMetaData = matchMetaData; }
	public void setStartDate( final ZonedDateTime startDate) { this.startDate = startDate; }
	public void setEndDate( final ZonedDateTime endDate) { this.endDate = endDate; }
	public void setCreatedAt( final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt( final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setSources( final String sources) { this.sources = sources; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "EventFilter [id=" + id + ", eventType=" + eventType + ", consumerSystem=" + consumerSystem
				+ ", matchMetaData=" + matchMetaData + "]";
	}
}
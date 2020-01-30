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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "qos_reservation")
public class QoSReservation {
	
	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "reservedServiceId", referencedColumnName = "id", nullable = false, unique = true)
	private ServiceRegistry reservedService;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "consumerCloudId", referencedColumnName = "id", nullable = true)
	private Cloud consumerCloud;
	
	private String consumerSystemName;
	private String consumerAddress;
	private int consumerPort;
	
	private ZonedDateTime reservedTo;
	
	private boolean temporaryLock = false;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSReservation() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSReservation(final ServiceRegistry reservedService, final Cloud consumerCloud, final String consumerSystemName, final String consumerAddress, final int consumerPort, 
						  final ZonedDateTime reservedTo, final boolean temporaryLock) {
		this.reservedService = reservedService;
		this.consumerCloud = consumerCloud;
		this.consumerSystemName = consumerSystemName;
		this.consumerAddress = consumerAddress;
		this.consumerPort = consumerPort;
		this.reservedTo = reservedTo;
		this.temporaryLock = temporaryLock;
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
	public ServiceRegistry getReservedService() { return reservedService; }
	public Cloud getConsumerCloud() { return consumerCloud; }
	public String getConsumerSystemName() { return consumerSystemName; }
	public String getConsumerAddress() { return consumerAddress; }
	public int getConsumerPort() { return consumerPort; }
	public ZonedDateTime getReservedTo() { return reservedTo; }
	public boolean isTemporaryLock() { return temporaryLock; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setReservedService(final ServiceRegistry reservedService) { this.reservedService = reservedService; }
	public void setConsumerCloud(final Cloud consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setConsumerSystemName(final String consumerSystemName) { this.consumerSystemName = consumerSystemName; }
	public void setConsumerAddress(final String consumerAddress) { this.consumerAddress = consumerAddress; }
	public void setConsumerPort(final int consumerPort) { this.consumerPort = consumerPort; }
	public void setReservedTo(final ZonedDateTime reservedTo) { this.reservedTo = reservedTo; }
	public void setTemporaryLock(final boolean temporaryLock) { this.temporaryLock = temporaryLock; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
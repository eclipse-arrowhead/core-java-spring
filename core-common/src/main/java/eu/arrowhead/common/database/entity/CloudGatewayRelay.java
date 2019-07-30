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
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"cloudAId", "cloudBId", "relayId"}))
public class CloudGatewayRelay {

	//=================================================================================================
	// members

	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cloudAId", referencedColumnName = "id", nullable = false)
	private Cloud cloudA;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cloudBId", referencedColumnName = "id", nullable = false)
	private Cloud cloudB;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "relayId", referencedColumnName = "id", nullable = false)
	private Relay relay;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudGatewayRelay() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudGatewayRelay(final Cloud cloudA, final Cloud cloudB, final Relay relay) {
		this.cloudA = cloudA;
		this.cloudB = cloudB;
		this.relay = relay;
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
	public Cloud getCloudA() { return cloudA; }
	public Cloud getCloudB() { return cloudB; }
	public Relay getRelay() { return relay; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloudA(final Cloud cloudA) { this.cloudA = cloudA; }
	public void setCloudB(final Cloud cloudB) { this.cloudB = cloudB; }
	public void setRelay(final Relay relay) { this.relay = relay; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "CloudGatewayRelay [id = " + id + ", cloudA = " + cloudA + ", cloudB = " + cloudB + ", relay = " + relay + "]";
	}
}

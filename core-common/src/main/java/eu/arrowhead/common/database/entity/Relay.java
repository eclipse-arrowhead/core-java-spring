package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.RelayType;

@Entity
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"address", "port"}))
public class Relay {
	
	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String address;
	
	@Column (nullable = false)
	private int port;
	
	@Column (nullable = false)
	private boolean secure = false;
	
	@Column (nullable = false)
	private boolean privateRelation = false;
	
	@Column (nullable = false, columnDefinition = "varchar(" + Defaults.VARCHAR_BASIC + ") DEFAULT 'GENERAL_RELAY'")
	@Enumerated(EnumType.STRING)
	private RelayType type = RelayType.GENERAL_RELAY;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Relay() {}

	//-------------------------------------------------------------------------------------------------
	public Relay(final String address, final int port, final boolean secure, final boolean privateRelation, final RelayType type) {
		this.address = address;
		this.port = port;
		this.secure = secure;
		this.privateRelation = privateRelation;
		this.type = type;
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
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public boolean getSecure() { return secure; }
	public boolean getPrivateRelation() { return privateRelation; }
	public RelayType getType() { return type; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setPrivateRelation (final boolean privateRelation) { this.privateRelation = privateRelation; }
	public void setType(final RelayType type) { this.type = type; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Relay [id = " + id + ", address = " + address + ", port = " + port + ", type = " + type + "]";
	}
}
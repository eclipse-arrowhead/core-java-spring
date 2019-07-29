package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.Defaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "address", "port", "serviceUri" }))
public class CloudGatekeeper {

	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@OneToOne(optional = false)
	@JoinColumn(name = "cloudId", referencedColumnName = "id", unique = true, nullable = false)
	private Cloud cloud;

	@Column(nullable = false, length = Defaults.VARCHAR_BASIC)
	private String address;

	@Column(nullable = false)
	private int port;

	@Column(nullable = false, length = Defaults.VARCHAR_BASIC)
	private String serviceUri;
	
	@Column(nullable = true, length = Defaults.VARCHAR_EXTENDED)
	private String authenticationInfo;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudGatekeeper() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudGatekeeper(final Cloud cloud, final String address, final int port, final String serviceUri, final String authenticationInfo) {
		this.cloud = cloud;
		this.address = address;
		this.port = port;
		this.serviceUri = serviceUri;
		this.authenticationInfo = authenticationInfo;
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
	public Cloud getCloud() { return cloud; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getServiceUri() { return serviceUri; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloud(final Cloud cloud) { this.cloud = cloud; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "CloudGatekeeper [id=" + id + ", operator=" + cloud.getOperator() + ", name=" + cloud.getName() + ", address=" + address + ", port=" + port	+ ", serviceUri=" + serviceUri + "]";
	}
}
package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.Defaults;

@Entity
public class ServiceInterface {
	
	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
	private String interfaceName;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "serviceInterface", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistryInterfaceConnection> serviceConnections = new HashSet<>();
	
	@OneToMany(mappedBy = "serviceInterface", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloudInterfaceConnection> authorizationIntraCloudConnections = new HashSet<>();
	
	@OneToMany(mappedBy = "serviceInterface", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationInterCloudInterfaceConnection> authorizationInterCloudConnections = new HashSet<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceInterface() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterface(final String interfaceName) {
		this.interfaceName = interfaceName;
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
	public String getInterfaceName() { return interfaceName; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ServiceRegistryInterfaceConnection> getServiceConnections() { return serviceConnections; }
	public Set<AuthorizationIntraCloudInterfaceConnection> getAuthorizationIntraCloudConnections() { return authorizationIntraCloudConnections; }
	public Set<AuthorizationInterCloudInterfaceConnection> getAuthorizationInterCloudConnections() { return authorizationInterCloudConnections; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setInterfaceName(final String interfaceName) { this.interfaceName = interfaceName; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setServiceConnections(final Set<ServiceRegistryInterfaceConnection> serviceConnections) { this.serviceConnections = serviceConnections; }
	public void setAuthorizationIntraCloudConnections(final Set<AuthorizationIntraCloudInterfaceConnection> authorizationIntraCloudConnections) { this.authorizationIntraCloudConnections = authorizationIntraCloudConnections; }
	public void setAuthorizationInterCloudConnections(final Set<AuthorizationInterCloudInterfaceConnection> authorizationInterCloudConnections) { this.authorizationInterCloudConnections = authorizationInterCloudConnections; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "ServiceInterface [id = " + id + ", interfaceName = " + interfaceName + "]";
	}
}
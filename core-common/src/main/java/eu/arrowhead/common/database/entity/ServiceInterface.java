package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import eu.arrowhead.common.Defaults;

@Entity
public class ServiceInterface {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column (nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
	private String interface_;
	
	@Column (nullable = false)
	private ZonedDateTime createdAt = ZonedDateTime.now();
	
	@Column (nullable = false)
	private ZonedDateTime updatedAt = ZonedDateTime.now();
	
	@OneToMany (mappedBy = "serviceInterface", fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<ServiceRegistryInterfaceConnection> serviceConnections;

	public ServiceInterface() {
	
	}

	public ServiceInterface(String interface_) {
		this.interface_ = interface_;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getInterface_() {
		return interface_;
	}

	public void setInterface_(String interface_) {
		this.interface_ = interface_;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Set<ServiceRegistryInterfaceConnection> getServiceConnections() {
		return serviceConnections;
	}

	public void setServiceConnections(Set<ServiceRegistryInterfaceConnection> serviceConnections) {
		this.serviceConnections = serviceConnections;
	}

	@Override
	public String toString() {
		return "ServiceInterface [id=" + id + ", interface_=" + interface_ + "]";
	}
	
}

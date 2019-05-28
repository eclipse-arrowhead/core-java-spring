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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.Defaults;

@Entity
public class ServiceInterface {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column (nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
	private String interfaceName;
	
	@Column (nullable = false)
	private ZonedDateTime createdAt = ZonedDateTime.now();
	
	@Column (nullable = false)
	private ZonedDateTime updatedAt = ZonedDateTime.now();
	
	@OneToMany (mappedBy = "serviceInterface", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistryInterfaceConnection> serviceConnections = new HashSet<ServiceRegistryInterfaceConnection>();
	
	public ServiceInterface() {
	
	}

	public ServiceInterface(final String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(final String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Set<ServiceRegistryInterfaceConnection> getServiceConnections() {
		return serviceConnections;
	}

	public void setServiceConnections(final Set<ServiceRegistryInterfaceConnection> serviceConnections) {
		this.serviceConnections = serviceConnections;
	}

	@Override
	public String toString() {
		return "ServiceInterface [id=" + id + ", interfaceName=" + interfaceName + "]";
	}
	
}

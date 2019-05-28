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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.Defaults;

@Entity
@Table (name = "system_", uniqueConstraints = @UniqueConstraint(columnNames = {"systemName", "address", "port"}))
public class System {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String systemName;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String address;
	
	@Column (nullable = false)
	private int port;
	
	@Column (nullable = true, length = Defaults.VARCHAR_EXTENDED)
	private String authenticationInfo;
	
	@Column (nullable = false)
	private ZonedDateTime createdAt = ZonedDateTime.now();
	
	@Column (nullable = false)
	private ZonedDateTime updatedAt = ZonedDateTime.now();
	
	@OneToMany (mappedBy = "system", fetch = FetchType.LAZY, orphanRemoval = true)
	private Set<ServiceRegistry> serviceRegistryEntries = new HashSet<ServiceRegistry>();
	
	public System() {
		
	}

	public System(final String systemName, final String address, final int port, final String authenticationInfo) {
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(final String systemName) {
		this.systemName = systemName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(final String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
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
	
	public Set<ServiceRegistry> getServiceRegistryEntries() {
		return serviceRegistryEntries;
	}

	public void setServiceRegistryEntries(final Set<ServiceRegistry> serviceRegistryEntries) {
		this.serviceRegistryEntries = serviceRegistryEntries;
	}
	
	@Override
	public String toString() {
		return "System [id=" + id + ", systemName=" + systemName + ", address=" + address + ", port=" + port + "]";
	}
	
}

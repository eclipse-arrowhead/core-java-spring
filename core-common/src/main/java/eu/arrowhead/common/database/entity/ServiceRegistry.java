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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.Defaults;

@Entity
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"serviceId", "systemId"}))
public class ServiceRegistry {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "serviceId", referencedColumnName = "id", nullable = false)
	private ServiceDefinition serviceDefinition;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "systemId", referencedColumnName = "id", nullable = false)
	private System system;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String serviceUri;
	
	@Column (nullable = true)
	private ZonedDateTime endOfValidity;
	
	@Column (nullable = false)
	private boolean secure = false;
	
	@Column (nullable = true, columnDefinition = "TEXT")
	private String metadata;
	
	@Column (nullable = true)
	private int version = 1;
	
	@Column (nullable = false, columnDefinition = "TIMESTAMP")
	private ZonedDateTime createdAt = ZonedDateTime.now();
	
	@Column (nullable = false, columnDefinition = "TIMESTAMP")
	private ZonedDateTime updatedAt = ZonedDateTime.now();
	
	@OneToMany (mappedBy = "serviceRegistryEntry", fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistryInterfaceConnection> interfaceConnections = new HashSet<ServiceRegistryInterfaceConnection>();

	public ServiceRegistry() {
		
	}

	public ServiceRegistry(final ServiceDefinition serviceDefinition, final System system, final String serviceUri,
			final ZonedDateTime endOfValidity, final boolean secure, final String metadata, final int version) {
		this.serviceDefinition = serviceDefinition;
		this.system = system;
		this.serviceUri = serviceUri;
		this.endOfValidity = endOfValidity;
		this.secure = secure;
		this.metadata = metadata;
		this.version = version;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	public void setServiceDefinition(final ServiceDefinition serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(final System system) {
		this.system = system;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(final String serviceUri) {
		this.serviceUri = serviceUri;
	}

	public ZonedDateTime getEndOfValidity() {
		return endOfValidity;
	}

	public void setEndOfValidity(final ZonedDateTime endOfValidity) {
		this.endOfValidity = endOfValidity;
	}

	public boolean getSecure() {
		return secure;
	}

	public void setSecure(final boolean secure) {
		this.secure = secure;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(final String metadata) {
		this.metadata = metadata;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
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
	
	public Set<ServiceRegistryInterfaceConnection> getInterfaceConnections() {
		return interfaceConnections;
	}

	public void setInterfaceConnections(final Set<ServiceRegistryInterfaceConnection> interfaceConnections) {
		this.interfaceConnections = interfaceConnections;
	}

	@Override
	public String toString() {
		return "ServiceRegistry [id=" + id + ", serviceDefinition=" + serviceDefinition + ", system=" + system
				+ ", endOfValidity=" + endOfValidity + ", version=" + version + "]";
	}
		
}

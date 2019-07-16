package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.Defaults;

@Entity
@Table (name = "system_", uniqueConstraints = @UniqueConstraint(columnNames = {"systemName", "address", "port"}))
@NamedEntityGraph (name = "systemWithServiceRegistryEntries",
	attributeNodes = {
		@NamedAttributeNode (value = "serviceRegistryEntries")
	})
public class System {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "systemName", "address", "port"); //NOSONAR
	
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
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany (mappedBy = "system", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistry> serviceRegistryEntries = new HashSet<>();
	
	@OneToMany (mappedBy = "consumerSystem", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloud> authorizationsAsConsumer = new HashSet<>();
	
	@OneToMany (mappedBy = "providerSystem", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloud> authorizationsAsProvider = new HashSet<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public System() {}

	//-------------------------------------------------------------------------------------------------
	public System(final String systemName, final String address, final int port, final String authenticationInfo) {
		this.systemName = systemName;
		this.address = address;
		this.port = port;
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
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ServiceRegistry> getServiceRegistryEntries() { return serviceRegistryEntries; }
	public Set<AuthorizationIntraCloud> getAuthorizationsAsConsumer() { return authorizationsAsConsumer; }
	public Set<AuthorizationIntraCloud> getAuthorizationsAsProvider() { return authorizationsAsProvider; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setServiceRegistryEntries(final Set<ServiceRegistry> serviceRegistryEntries) { this.serviceRegistryEntries = serviceRegistryEntries; }
	public void setAuthorizationsAsConsumer(final Set<AuthorizationIntraCloud> authorizationsAsConsumer) { this.authorizationsAsConsumer = authorizationsAsConsumer; }
	public void setAuthorizationsAsProvider(final Set<AuthorizationIntraCloud> authorizationsAsProvider) { this.authorizationsAsProvider = authorizationsAsProvider; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "System [id = " + id + ", systemName = " + systemName + ", address = " + address + ", port = " + port + "]";
	}
}
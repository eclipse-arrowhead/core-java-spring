/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"serviceId", "systemId", "serviceUri"}))
public class ServiceRegistry {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt"); //NOSONAR
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "serviceId", referencedColumnName = "id", nullable = false)
	private ServiceDefinition serviceDefinition;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "systemId", referencedColumnName = "id", nullable = false)
	private System system;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String serviceUri = "";
	
	@Column(nullable = true)
	private ZonedDateTime endOfValidity;
	
	@Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ") DEFAULT 'NOT_SECURE'")
	@Enumerated(EnumType.STRING)
	private ServiceSecurityType secure = ServiceSecurityType.NOT_SECURE;
	
	@Column(nullable = true, columnDefinition = "MEDIUMTEXT")
	private String metadata;
	
	@Column(nullable = true)
	private Integer version = Defaults.DEFAULT_VERSION;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "serviceRegistryEntry", fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistryInterfaceConnection> interfaceConnections = new HashSet<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistry() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistry(final ServiceDefinition serviceDefinition, final System system, final String serviceUri, final ZonedDateTime endOfValidity, final ServiceSecurityType secure,
						   final String metadata, final Integer version) {
		this.serviceDefinition = serviceDefinition;
		this.system = system;
		this.serviceUri = serviceUri == null ? "" : serviceUri;
		this.endOfValidity = endOfValidity;
		this.secure = secure;
		this.metadata = metadata;
		this.version = version;
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
	public ServiceDefinition getServiceDefinition() { return serviceDefinition; }
	public System getSystem() {	return system; }
	public String getServiceUri() { return serviceUri; }
	public ZonedDateTime getEndOfValidity() { return endOfValidity; }
	public ServiceSecurityType getSecure() { return secure; }
	public String getMetadata() { return metadata; }
	public Integer getVersion() { return version; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ServiceRegistryInterfaceConnection> getInterfaceConnections() { return interfaceConnections; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setSystem(final System system) { this.system = system; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setEndOfValidity(final ZonedDateTime endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setSecure(final ServiceSecurityType secure) { this.secure = secure; }
	public void setMetadata(final String metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setInterfaceConnections(final Set<ServiceRegistryInterfaceConnection> interfaceConnections) { this.interfaceConnections = interfaceConnections; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "ServiceRegistry [id = " + id + ", serviceDefinition = " + serviceDefinition + ", system = " + system + ", serviceUri = " + serviceUri + ", endOfValidity = " + endOfValidity + ", version = " + version + "]";
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		final ServiceRegistry other = (ServiceRegistry) obj;
		
		return id == other.id;
	}
}
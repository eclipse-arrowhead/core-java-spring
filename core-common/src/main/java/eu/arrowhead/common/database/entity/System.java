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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.shared.AddressType;

@Entity
@Table(name = "system_", uniqueConstraints = @UniqueConstraint(columnNames = { "systemName", "address", "port" }))
@NamedEntityGraph(name = "systemWithServiceRegistryEntries",
				  attributeNodes = {
						  @NamedAttributeNode(value = "serviceRegistryEntries")
})
public class System {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "systemName", "address", "port"); //NOSONAR
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String systemName;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String address;
	
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private AddressType addressType;
	
	@Column(nullable = false)
	private int port;
	
	@Column(nullable = true, length = CoreDefaults.VARCHAR_EXTENDED)
	private String authenticationInfo;
	
	@Column(nullable = true, columnDefinition = "MEDIUMTEXT")
	private String metadata;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "system", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistry> serviceRegistryEntries = new HashSet<>();
	
	@OneToMany(mappedBy = "consumerSystem", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloud> authorizationsIntraCloudAsConsumer = new HashSet<>();
	
	@OneToMany(mappedBy = "providerSystem", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloud> authorizationsIntraCloudAsProvider = new HashSet<>();
	
	@OneToMany(mappedBy = "provider", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationInterCloud> authorizationsInterCloudAsProvider = new HashSet<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public System() {}

	//-------------------------------------------------------------------------------------------------
	public System(final String systemName, final String address, final AddressType addressType, final int port, final String authenticationInfo, final String metadata) {
		this.systemName = systemName;
		this.address = address;
		this.addressType = addressType;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.metadata = metadata;
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
	public AddressType getAddressType() { return addressType; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public String getMetadata() { return metadata; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ServiceRegistry> getServiceRegistryEntries() { return serviceRegistryEntries; }
	public Set<AuthorizationIntraCloud> getAuthorizationsIntraCloudAsConsumer() { return authorizationsIntraCloudAsConsumer; }
	public Set<AuthorizationIntraCloud> getAuthorizationsIntraCloudAsProvider() { return authorizationsIntraCloudAsProvider; }
	public Set<AuthorizationInterCloud> getAuthorizationsInterCloudAsProvider() { return authorizationsInterCloudAsProvider; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setAddressType(final AddressType addressType) { this.addressType = addressType; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setMetadata(final String metadata) { this.metadata = metadata; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setServiceRegistryEntries(final Set<ServiceRegistry> serviceRegistryEntries) { this.serviceRegistryEntries = serviceRegistryEntries; }
	public void setAuthorizationsIntraCloudAsConsumer(final Set<AuthorizationIntraCloud> authorizationsIntraCloudAsConsumer) { this.authorizationsIntraCloudAsConsumer = authorizationsIntraCloudAsConsumer; }
	public void setAuthorizationsIntraCloudAsProvider(final Set<AuthorizationIntraCloud> authorizationsIntraCloudAsProvider) { this.authorizationsIntraCloudAsProvider = authorizationsIntraCloudAsProvider; }
	public void setAuthorizationsInterCloudAsProvider(final Set<AuthorizationInterCloud> authorizationsInterCloudAsProvider) { this.authorizationsInterCloudAsProvider = authorizationsInterCloudAsProvider; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "System [id = " + id + ", systemName = " + systemName + ", address = " + address + ", port = " + port + "]";
	}
}
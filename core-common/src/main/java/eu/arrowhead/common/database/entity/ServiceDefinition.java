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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

@Entity
@NamedEntityGraph(name = "serviceDefinitionWithServiceRegistryEntries",
				  attributeNodes = {
						  @NamedAttributeNode (value = "serviceRegistryEntries")
})
public class ServiceDefinition {

	//=================================================================================================
	// members

	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt"); //NOSONAR

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
	private String serviceDefinition; //NOSONAR
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "serviceDefinition", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ServiceRegistry> serviceRegistryEntries = new HashSet<>();
	
	@OneToMany(mappedBy = "serviceDefinition", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloud> authorizationIntraClouds = new HashSet<>();
	
	@OneToMany(mappedBy = "serviceDefinition", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationInterCloud> authorizationInterClouds = new HashSet<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinition() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinition(final String serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
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
	public String getServiceDefinition() { return serviceDefinition; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ServiceRegistry> getServiceRegistryEntries() { return serviceRegistryEntries; }
	public Set<AuthorizationIntraCloud> getAuthorizationIntraClouds() { return authorizationIntraClouds; }
	public Set<AuthorizationInterCloud> getAuthorizationInterClouds() { return authorizationInterClouds; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setServiceRegistryEntries(final Set<ServiceRegistry> serviceRegistryEntries) { this.serviceRegistryEntries = serviceRegistryEntries; }
	public void setAuthorizationIntraClouds(final Set<AuthorizationIntraCloud> authorizationIntraClouds) { this.authorizationIntraClouds = authorizationIntraClouds; }
	public void setAuthorizationInterClouds(final Set<AuthorizationInterCloud> authorizationInterClouds) { this.authorizationInterClouds = authorizationInterClouds; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "ServiceDefinition [id = " + id + ", serviceDefinition = " + serviceDefinition + "]";
	}
}
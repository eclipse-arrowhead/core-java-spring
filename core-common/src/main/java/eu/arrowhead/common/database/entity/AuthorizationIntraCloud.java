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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"consumerSystemId", "providerSystemId", "serviceId"}))
public class AuthorizationIntraCloud {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt"); //NOSONAR

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "consumerSystemId", referencedColumnName = "id", nullable = false)
	private System consumerSystem;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "providerSystemId", referencedColumnName = "id", nullable = false)
	private System providerSystem; 
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "serviceId", referencedColumnName = "id", nullable = false)
	private ServiceDefinition serviceDefinition;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "authorizationIntraCloudEntry", fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationIntraCloudInterfaceConnection> interfaceConnections = new HashSet<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloud() {}

	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloud(final System consumerSystem, final System providerSystem, final ServiceDefinition serviceDefinition) {
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
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
	public System getConsumerSystem() { return consumerSystem; }
	public System getProviderSystem() { return providerSystem; }
	public ServiceDefinition getServiceDefinition() { return serviceDefinition; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<AuthorizationIntraCloudInterfaceConnection> getInterfaceConnections() { return interfaceConnections; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setConsumerSystem(final System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final System providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceDefinition(final ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setInterfaceConnections (final Set<AuthorizationIntraCloudInterfaceConnection> interfaceConnections) { this.interfaceConnections = interfaceConnections; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "AuthorizationIntraCloud [id = " + id + ", consumerSystem = " + consumerSystem + ", providerSystem = " + providerSystem + ", serviceDefinition = " + serviceDefinition + "]";
	}
}
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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.CoreCommonConstants;

@Entity
@Table(uniqueConstraints = {
			@UniqueConstraint(columnNames = {"serviceId", "consumerSystemId", "priority", "serviceInterfaceId"}),
			@UniqueConstraint(columnNames = {"serviceId", "consumerSystemId", "foreign_", "providerSystemId", "serviceInterfaceId"})
		})
public class OrchestratorStore {

	//=================================================================================================
	// members

	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", CoreCommonConstants.SORT_FIELD_PRIORITY); //NOSONAR
	public static final String FIELD_NAME_PRIORITY = "priority";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "serviceId", referencedColumnName = "id", nullable = false)
	private ServiceDefinition serviceDefinition;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "consumerSystemId", referencedColumnName = "id", nullable = false)
	private System consumerSystem;
	
	@Column(name = "foreign_", nullable = false)
	private boolean foreign;
	
	@Column(nullable = false)
	private long providerSystemId;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "serviceInterfaceId", referencedColumnName = "id", nullable = false)
	private ServiceInterface serviceInterface;
	
	@Column(nullable = false)
	private int priority;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String attribute;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStore() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStore(final ServiceDefinition serviceDefinition,	final System consumerSystem, final boolean foreign, final long providerSystemId, final ServiceInterface serviceInterface, 
							 final int priority, final String attribute) {
		
		this.serviceDefinition = serviceDefinition;
		this.consumerSystem = consumerSystem;
		this.foreign = foreign;
		this.providerSystemId = providerSystemId;
		this.serviceInterface = serviceInterface;
		this.priority = priority;
		this.attribute = attribute;
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
	public ServiceDefinition getServiceDefinition() { return serviceDefinition;	}
	public System getConsumerSystem() {	return consumerSystem; }
	public boolean isForeign() {return foreign; }
	public long getProviderSystemId() {	return providerSystemId; }
	public ServiceInterface getServiceInterface() { return serviceInterface;}
	public int getPriority() { return priority; }
	public String getAttribute() { return attribute; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(final System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setForeign(final boolean foreign) {this.foreign = foreign; }
	public void setProviderSystemId(final long providerSystemId) { this.providerSystemId = providerSystemId; }
	public void setServiceInterface(final ServiceInterface serviceInterface) { this.serviceInterface = serviceInterface ;}
	public void setPriority(final int priority) { this.priority = priority; }
	public void setAttribute(final String attribute) { this.attribute = attribute; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------	
	@Override
	public String toString() {
		return "OrchestratorStore [id = " + id + ", serviceDefinition = " + serviceDefinition + ", consumerSystem = " + consumerSystem + ", foreign = " + foreign + ", providerSystemid = " +
			   providerSystemId + ", serviceInterface = " + serviceInterface + ", priority = " + priority + ", attribute = " + attribute + ", createdAt = " + createdAt + ", updatedAt = " +
			   updatedAt + "]";
	}
}
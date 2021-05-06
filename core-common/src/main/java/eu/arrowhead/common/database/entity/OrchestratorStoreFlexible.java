/********************************************************************************
 * Copyright (c) 2021 AITIA
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
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
public class OrchestratorStoreFlexible {

	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "priority", "updatedAt", "createdAt");
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = true)
	private String consumerSystemName;
	
	@Column(nullable = true)
	private String providerSystemName;
	
	@Column(nullable = true, columnDefinition = "MEDIUMTEXT")
	private String consumerSystemMetadata;
	
	@Column(nullable = true, columnDefinition = "MEDIUMTEXT")
	private String providerSystemMetadata;
	
	@Column(nullable = true, columnDefinition = "MEDIUMTEXT")
	private String serviceMetadata;
	
	@Column(nullable = true)
	private String serviceInterfaceName;
	
	@Column(nullable = false)
	private String serviceDefinitionName;
	
	@Column(nullable = false)
	private int priority = Integer.MAX_VALUE;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexible() {}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexible(final String consumerSystemName, final String providerSystemName, final String consumerSystemMetadata, final String providerSystemMetadata,
									 final String serviceMetadata, final String serviceInterfaceName, final String serviceDefinitionName, final Integer priority) {
		this.consumerSystemName = consumerSystemName;
		this.providerSystemName = providerSystemName;
		this.consumerSystemMetadata = consumerSystemMetadata;
		this.providerSystemMetadata = providerSystemMetadata;
		this.serviceMetadata = serviceMetadata;
		this.serviceInterfaceName = serviceInterfaceName;
		this.serviceDefinitionName = serviceDefinitionName;
		this.priority = priority == null ? Integer.MAX_VALUE : priority;
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
	public String getConsumerSystemName() { return consumerSystemName; }
	public String getProviderSystemName() { return providerSystemName; }
	public String getConsumerSystemMetadata() { return consumerSystemMetadata; }
	public String getProviderSystemMetadata() { return providerSystemMetadata; }
	public String getServiceMetadata() { return serviceMetadata; }
	public String getServiceInterfaceName() { return serviceInterfaceName; }
	public String getServiceDefinitionName() { return serviceDefinitionName; }
	public int getPriority() { return priority; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setConsumerSystemName(final String consumerSystemName) { this.consumerSystemName = consumerSystemName; }
	public void setProviderSystemName(final String providerSystemName) { this.providerSystemName = providerSystemName; }
	public void setConsumerSystemMetadata(final String consumerSystemMetadata) { this.consumerSystemMetadata = consumerSystemMetadata; }
	public void setProviderSystemMetadata(final String providerSystemMetadata) { this.providerSystemMetadata = providerSystemMetadata; }
	public void setServiceMetadata(final String serviceMetadata) { this.serviceMetadata = serviceMetadata; }
	public void setServiceInterfaceName(final String serviceInterfaceName) { this.serviceInterfaceName = serviceInterfaceName; }
	public void setServiceDefinitionName(final String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
	public void setPriority(final int priority) { this.priority = priority; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		final String consumerName = consumerSystemName == null ? "null" : consumerSystemName;
		final String providerName = providerSystemName == null ? "null" : providerSystemName;
		final String consumerMeta = consumerSystemMetadata == null ? "null" : consumerSystemMetadata;
		final String providerMeta = providerSystemMetadata == null ? "null" : providerSystemMetadata;
		final String serviceMeta = serviceMetadata == null ? "null" :  serviceMetadata;
		final String interfaceName = serviceInterfaceName == null ? "null" :  serviceInterfaceName;
		return "OrchestratorStoreFlexible [id = " + id + ", consumerName = " + consumerName + ", providerName = " + providerName + ", consumerMeta = {" + consumerMeta +
			   "}, providerMeta = {" + providerMeta + "}, serviceMeta = {" + serviceMeta + "}, interfaceName = " + interfaceName + ", serviceDef = " + serviceDefinitionName + ", priority = " + priority + "]";
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
		
		final OrchestratorStoreFlexible other = (OrchestratorStoreFlexible) obj;
		
		return id == other.id;
	}
}

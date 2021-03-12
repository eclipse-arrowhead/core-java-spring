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

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"authorizationInterCloudId", "interfaceId"}))
public class AuthorizationInterCloudInterfaceConnection {

	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "authorizationInterCloudId", referencedColumnName = "id", nullable = false)
	private AuthorizationInterCloud authorizationInterCloudEntry; 
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "interfaceId", referencedColumnName = "id", nullable = false)
	private ServiceInterface serviceInterface;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudInterfaceConnection() {}

	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudInterfaceConnection(final AuthorizationInterCloud authorizationInterCloudEntry, final ServiceInterface serviceInterface) {
		this.authorizationInterCloudEntry = authorizationInterCloudEntry;
		this.serviceInterface = serviceInterface;
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
	public AuthorizationInterCloud getAuthorizationInterCloudEntry() { return authorizationInterCloudEntry; }
	public ServiceInterface getServiceInterface() { return serviceInterface; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setAuthorizationInterCloudEntry(final AuthorizationInterCloud authorizationInterCloudEntry) { this.authorizationInterCloudEntry = authorizationInterCloudEntry; }
	public void setServiceInterface(final ServiceInterface serviceInterface) { this.serviceInterface = serviceInterface; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "AuthorizationInterCloudInterfaceConnection [id = " + id + ", authorizationInterCloudEntry = " + authorizationInterCloudEntry + ", serviceInterface = " + serviceInterface + "]";
	}	
}
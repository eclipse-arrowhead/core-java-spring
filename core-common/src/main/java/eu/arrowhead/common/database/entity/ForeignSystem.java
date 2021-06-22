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

import eu.arrowhead.common.CoreDefaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"systemName", "address", "port"}))
public class ForeignSystem {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "systemName", "address", "port"); //NOSONAR
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "providerCloudId", referencedColumnName = "id", nullable = false)
	private Cloud providerCloud;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String systemName;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String address;
	
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
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ForeignSystem() {}

	//-------------------------------------------------------------------------------------------------
	public ForeignSystem(final Cloud providerCloud, final String systemName, final String address, final int port, final String authenticationInfo, final String metadata) {
		this.providerCloud = providerCloud;
		this.systemName = systemName;
		this.address = address;
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
	public Cloud getProviderCloud() {return providerCloud ;}
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public String getMetadata() { return metadata; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setProviderCloud(final Cloud providerCloud) {this.providerCloud = providerCloud; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setMetadata(final String metadata) { this.metadata = metadata; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "ForeignSystem [id = " + id + ", providerCloud[providerCloud.id= " + providerCloud.getId() + ", providerCloud.name = " + providerCloud.getName() + 
			   ", providerCloud.neighbor = " + providerCloud.getNeighbor() + ", providerCloud.secure = " + providerCloud.getSecure() + ", providerCloud.operator = " + providerCloud.getOperator() +
			   ", providerCloud.ownCloud = " + providerCloud.getOwnCloud() + " ], systemName = " + systemName + ", address = " + address + ", port = " + port + "]";
	}
}
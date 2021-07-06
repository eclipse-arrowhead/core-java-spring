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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"operator", "name"}))
public class Cloud {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "operator", "name", "updatedAt", "createdAt"); //NOSONAR
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String operator;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String name;
	
	@Column(nullable = false)
	private boolean secure = false;
	
	@Column(nullable = false)
	private boolean neighbor = false;
	
	@Column(nullable = false)
	private boolean ownCloud = false;
	
	@Column(nullable = true, length = CoreDefaults.VARCHAR_EXTENDED)
	private String authenticationInfo;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "cloud", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<AuthorizationInterCloud> authorizationInterClouds = new HashSet<>();
	
	@OneToMany(mappedBy = "cloud", fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<CloudGatekeeperRelay> gatekeeperRelays = new HashSet<>();
	
	@OneToMany(mappedBy = "cloud", fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<CloudGatewayRelay> gatewayRelays = new HashSet<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Cloud() {}

	//-------------------------------------------------------------------------------------------------
	public Cloud(final String operator, final String name, final boolean secure, final boolean neighbor, final boolean ownCloud, final String authenticationInfo) {
		this.operator = operator;
		this.name = name;
		this.secure = secure;
		this.neighbor = neighbor;
		this.ownCloud = ownCloud;
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
	public String getOperator() { return operator; }
	public String getName() { return name; }
	public boolean getSecure() { return secure; }
	public boolean getNeighbor() { return neighbor; }
	public boolean getOwnCloud() { return ownCloud; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<AuthorizationInterCloud> getAuthorizationInterClouds() { return authorizationInterClouds; }
	public Set<CloudGatekeeperRelay> getGatekeeperRelays() { return gatekeeperRelays; }
	public Set<CloudGatewayRelay> getGatewayRelays() { return gatewayRelays; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setOperator(final String operator) { this.operator = operator; }
	public void setName(final String name) { this.name = name; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setNeighbor(final boolean neighbor) { this.neighbor = neighbor; }
	public void setOwnCloud(final boolean ownCloud) { this.ownCloud = ownCloud; }
	public void setAuthenticationInfo (final String authenticationInfo) { this.authenticationInfo = authenticationInfo; } 
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setAuthorizationInterClouds(final Set<AuthorizationInterCloud> authorizationInterClouds) { this.authorizationInterClouds = authorizationInterClouds; }
	public void setGatekeeperRelays(final Set<CloudGatekeeperRelay> gatekeeperRelays) { this.gatekeeperRelays = gatekeeperRelays; }
	public void setGatewayRelays(final Set<CloudGatewayRelay> gatewayRelays) { this.gatewayRelays = gatewayRelays; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Cloud [id = " + id + ", operator = " + operator + ", name = " + name + "]";
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
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
		
		final Cloud other = (Cloud) obj;
		
		return id == other.id;
	}	
}
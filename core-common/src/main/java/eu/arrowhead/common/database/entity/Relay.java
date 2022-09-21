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
import eu.arrowhead.common.dto.internal.RelayType;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"address", "port"}))
@NamedEntityGraph(name = "relayWithCloudGatekeeperRelayEntries",
				  attributeNodes = {
						  @NamedAttributeNode(value = "cloudGatekeepers")
})
public class Relay {
	
	//=================================================================================================
	// members
	
	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "address", "port"); //NOSONAR
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String address;
	
	@Column(nullable = false)
	private int port;
	
	@Column(nullable = true)
	private String authenticationInfo;
	
	@Column(nullable = false)
	private boolean secure = false;
	
	@Column(nullable = false)
	private boolean exclusive = false;
	
	@Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ") DEFAULT 'GENERAL_RELAY'")
	@Enumerated(EnumType.STRING)
	private RelayType type = RelayType.GENERAL_RELAY;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "relay", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<CloudGatekeeperRelay> cloudGatekeepers = new HashSet<>();
	
	@OneToMany(mappedBy = "relay", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<CloudGatewayRelay> cloudGateways = new HashSet<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Relay() {}

	//-------------------------------------------------------------------------------------------------
	public Relay(final String address, final int port, final boolean secure, final boolean exclusive, final RelayType type) {
		this.address = address;
		this.port = port;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Relay(final String address, final int port, final String authenticationInfo, final boolean secure, final boolean exclusive, final RelayType type) {
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
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
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public boolean getSecure() { return secure; }
	public boolean getExclusive() { return exclusive; }
	public RelayType getType() { return type; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<CloudGatekeeperRelay> getCloudGatekeepers() { return cloudGatekeepers; }
	public Set<CloudGatewayRelay> getCloudGateways() { return cloudGateways; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setExclusive (final boolean exclusive) { this.exclusive = exclusive; }
	public void setType(final RelayType type) { this.type = type; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setCloudGatekeepers(final Set<CloudGatekeeperRelay> cloudGatekeepers) { this.cloudGatekeepers = cloudGatekeepers; }
	public void setCloudGateways(final Set<CloudGatewayRelay> cloudGateways) { this.cloudGateways = cloudGateways; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Relay [id = " + id + ", address = " + address + ", port = " + port + ", type = " + type + "]";
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
		final Relay other = (Relay) obj;
		
		return id == other.id;
	}
}
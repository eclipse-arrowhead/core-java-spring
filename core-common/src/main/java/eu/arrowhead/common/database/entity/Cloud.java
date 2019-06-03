package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
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

import eu.arrowhead.common.Defaults;

@Entity
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"operator", "name"}))
public class Cloud {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String operator;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String name;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String address;
	
	@Column (nullable = false)
	private int port;
	
	@Column (nullable = false, length = Defaults.VARCHAR_BASIC)
	private String gatekeeperServiceUri;
	
	@Column (nullable = true, length = Defaults.VARCHAR_EXTENDED)
	private String authenticationInfo;
	
	@Column (nullable = false)
	private boolean secure = false;
	
	@Column (nullable = false)
	private boolean neighbor = false;
	
	@Column (nullable = false)
	private boolean ownCloud = false;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	@OneToMany (mappedBy = "cloud", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private Set<InterCloudAuthorization> interCloudAuthorizations = new HashSet<>();
	
	public Cloud() {
		
	}

	public Cloud(final String operator, final String name, final String address, final int port, final String gatekeeperServiceUri,
			final String authenticationInfo, final boolean secure, final boolean neighbor, final boolean ownCloud) {
		this.operator = operator;
		this.name = name;
		this.address = address;
		this.port = port;
		this.gatekeeperServiceUri = gatekeeperServiceUri;
		this.authenticationInfo = authenticationInfo;
		this.secure = secure;
		this.neighbor = neighbor;
		this.ownCloud = ownCloud;
	}
	
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = ZonedDateTime.now();
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(final String operator) {
		this.operator = operator;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public String getGatekeeperServiceUri() {
		return gatekeeperServiceUri;
	}

	public void setGatekeeperServiceUri(final String gatekeeperServiceUri) {
		this.gatekeeperServiceUri = gatekeeperServiceUri;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(final String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public boolean getSecure() {
		return secure;
	}

	public void setSecure(final boolean secure) {
		this.secure = secure;
	}

	public boolean getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(final boolean neighbor) {
		this.neighbor = neighbor;
	}

	public boolean getOwnCloud() {
		return ownCloud;
	}

	public void setOwnCloud(final boolean ownCloud) {
		this.ownCloud = ownCloud;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
		
	public Set<InterCloudAuthorization> getInterCloudAuthorizations() {
		return interCloudAuthorizations;
	}

	public void setInterCloudAuthorizations(final Set<InterCloudAuthorization> interCloudAuthorizations) {
		this.interCloudAuthorizations = interCloudAuthorizations;
	}

	@Override
	public String toString() {
		return "Cloud [id=" + id + ", operator=" + operator + ", name=" + name + ", address=" + address + ", port="
				+ port + "]";
	}

}

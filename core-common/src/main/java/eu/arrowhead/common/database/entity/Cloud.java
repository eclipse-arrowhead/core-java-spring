package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
	private Boolean secure;
	
	@Column (nullable = false)
	private Boolean neighbor;
	
	@Column (nullable = false)
	private Boolean ownCloud;
	
	@Column (nullable = false)
	private ZonedDateTime createdAt = ZonedDateTime.now();
	
	@Column (nullable = false)
	private ZonedDateTime updatedAt = ZonedDateTime.now();

	public Cloud() {
		
	}

	public Cloud(String operator, String name, String address, int port, String gatekeeperServiceUri,
			String authenticationInfo, Boolean secure, Boolean neighbor, Boolean ownCloud) {
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getGatekeeperServiceUri() {
		return gatekeeperServiceUri;
	}

	public void setGatekeeperServiceUri(String gatekeeperServiceUri) {
		this.gatekeeperServiceUri = gatekeeperServiceUri;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public Boolean getSecure() {
		return secure;
	}

	public void setSecure(Boolean secure) {
		this.secure = secure;
	}

	public Boolean getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(Boolean neighbor) {
		this.neighbor = neighbor;
	}

	public Boolean getOwnCloud() {
		return ownCloud;
	}

	public void setOwnCloud(Boolean ownCloud) {
		this.ownCloud = ownCloud;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "Cloud [id=" + id + ", operator=" + operator + ", name=" + name + ", address=" + address + ", port="
				+ port + "]";
	}

}

package eu.arrowhead.common.dto;

import java.io.Serializable;

public class CloudResponseDTO implements Serializable {


	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5183291727291459583L;
	
	private long id;
	private String operator;
	private String name;
	private String address;
	private int port;
	private String gateKeeperServiceUri;
	private Boolean secure;
	private Boolean neighbor;
	private Boolean ownCloud;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO(long id, String operator, String name, String address, int port,
			String gateKeeperServiceUri, Boolean secure, Boolean neighbor, Boolean ownCloud, String createdAt,
			String updatedAt) {
		this.id = id;
		this.operator = operator;
		this.name = name;
		this.address = address;
		this.port = port;
		this.gateKeeperServiceUri = gateKeeperServiceUri;
		this.secure = secure;
		this.neighbor = neighbor;
		this.ownCloud = ownCloud;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;	}
	public String getOperator() { return operator; }
	public String getName() { return name; }
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public String getGateKeeperServiceUri() { return gateKeeperServiceUri; }
	public Boolean getSecure() { return secure; }
	public Boolean getNeighbor() { return neighbor; }
	public Boolean getOwnCloud() { return ownCloud; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	//-------------------------------------------------------------------------------------------------
	public void setId(long id) { this.id = id; }
	public void setOperator(final String operator) { this.operator = operator; }
	public void setName(final String name) { this.name = name; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setGateKeeperServiceUri(final String gateKeeperServiceUri) { this.gateKeeperServiceUri = gateKeeperServiceUri; }
	public void setSecure(final Boolean secure) { this.secure = secure; }
	public void setNeighbor(final Boolean neighbor) { this.neighbor = neighbor; }
	public void setOwnCloud(final Boolean ownCloud) { this.ownCloud = ownCloud; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
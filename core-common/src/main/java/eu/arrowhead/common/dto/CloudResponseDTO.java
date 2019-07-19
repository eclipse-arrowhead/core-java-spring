package eu.arrowhead.common.dto;

import java.io.Serializable;

public class CloudResponseDTO implements Serializable {


	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4612020802989025722L;
	
	private long id;
	private String operator;
	private String name;
	private String address;
	private Integer port;
	private String serviceUri;
	private boolean secure;
	private boolean neighbor;
	private boolean ownCloud;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO(final long id, final String operator, final String name, final boolean secure, final boolean neighbor, final boolean ownCloud, final String createdAt,
							final String updatedAt) {
		this(id, operator, name, null, null, null, secure, neighbor, ownCloud, createdAt, updatedAt);
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO(final long id, final String operator, final String name, final String address, final Integer port, final String serviceUri, final boolean secure,
							final boolean neighbor, final boolean ownCloud, final String createdAt,	final String updatedAt) {
		this.id = id;
		this.operator = operator;
		this.name = name;
		this.address = address;
		this.port = port;
		this.serviceUri = serviceUri;
		this.secure = secure;
		this.neighbor = neighbor;
		this.ownCloud = ownCloud;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;	}
	public String getName() { return name; }
	public String getOperator() { return operator; }
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public String getServiceUri() { return serviceUri; }
	public boolean getSecure() { return secure; }
	public boolean getNeighbor() { return neighbor; }
	public boolean getOwnCloud() { return ownCloud; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setName(final String name) { this.name = name; }
	public void setOperator(final String operator) { this.operator = operator; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setNeighbor(final boolean neighbor) { this.neighbor = neighbor; }
	public void setOwnCloud(final boolean ownCloud) { this.ownCloud = ownCloud; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}
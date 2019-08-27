package eu.arrowhead.common.dto;

import java.io.Serializable;

public class RelayResponseDTO implements Serializable {

	private static final long serialVersionUID = -8159263313404856979L;

	private long id;
	private String address;
	private int port;
	private boolean secure = false;
	private boolean exclusive = false;
	private RelayType type;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public RelayResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public RelayResponseDTO(final long id, final String address, final int port, final boolean secure, final boolean exclusive, final RelayType type, final String createdAt, final String updatedAt) {
		this.id = id;
		this.address = address;
		this.port = port;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public boolean isSecure() { return secure; }
	public boolean isExclusive() { return exclusive; }
	public RelayType getType() { return type; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setExclusive(final boolean exclusive) { this.exclusive = exclusive; }
	public void setType(final RelayType type) { this.type = type; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}
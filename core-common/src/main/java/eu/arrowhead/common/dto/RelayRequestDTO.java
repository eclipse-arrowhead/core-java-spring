package eu.arrowhead.common.dto;

import java.io.Serializable;

public class RelayRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -3457288931415932626L;

	private String address;
	private Integer port;
	private boolean secure = false;
	private boolean exclusive = false;
	private String type;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO(final String address, final Integer port, final boolean secure, final boolean exclusive, final String type) {
		this.address = address;
		this.port = port;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
	}

	//-------------------------------------------------------------------------------------------------
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public boolean isSecure() { return secure; }
	public boolean isExclusive() { return exclusive; }
	public String getType() { return type; }

	//-------------------------------------------------------------------------------------------------
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setExclusive(final boolean exclusive) { this.exclusive = exclusive; }
	public void setType(final String type) { this.type = type; }	
}

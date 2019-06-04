package eu.arrowhead.common.dto;

import java.io.Serializable;

public class SystemResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3919207845374510215L;
	
	private long sytemId;
	private String systemName;
	private String address;
	private int port;
	private String authenticationInfo;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	


	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo) {
		
		this.sytemId = systemId;
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getSytemId() { return sytemId;	}
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() {	return authenticationInfo; }
	public String getUpdatedAt() { return updatedAt; }
	public String getCreatedAt() { return createdAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSytemId(long sytemId) { this.sytemId = sytemId; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

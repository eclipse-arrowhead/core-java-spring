package eu.arrowhead.common.dto;

import java.io.Serializable;

public class SystemResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3919207845374510215L;
	
	private String systemName;
	private String address;
	private int port;
	private String authenticationInfo;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO(String systemName, String address, int port, String authenticationInfo) {
		
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() {	return authenticationInfo; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
}

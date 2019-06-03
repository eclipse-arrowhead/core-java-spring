package eu.arrowhead.common.dto;

import java.io.Serializable;

public class SystemRequestDTO implements Serializable {

	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3919207845374510215L;

	private Long userId; // only used in update requests
	private String systemName;
	private String address;
	private int port;
	private String authenticationInfo;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	public Long getUserId() { return userId;}
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port;	}
	public String getAuthenticationInfo() {	return authenticationInfo; }
	
	//-------------------------------------------------------------------------------------------------
	
	public void setUserId(Long userId) { this.userId = userId; }
	public void setSystemName(String systemName) { this.systemName = systemName; }
	public void setAddress(String address) { this.address = address; }
	public void setPort(int port) { this.port = port; }
	public void setAuthenticationInfo(String authenticationInfo) { this.authenticationInfo = authenticationInfo;	}
	
}

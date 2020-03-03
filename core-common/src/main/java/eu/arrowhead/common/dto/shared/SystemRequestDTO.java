package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Objects;

public class SystemRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3919207845374510215L;

	private String systemName;
	private String address;
	private Integer port;
	private String authenticationInfo;
	
	//=================================================================================================
	// methods


	public SystemRequestDTO() {
	}

	public SystemRequestDTO(final String systemName, final String address, final Integer port, final String authenticationInfo) {
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
	}

	//-------------------------------------------------------------------------------------------------
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public Integer getPort() { return port;	}
	public String getAuthenticationInfo() {	return authenticationInfo; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(address, port, systemName);
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
		
		final SystemRequestDTO other = (SystemRequestDTO) obj;
		
		return Objects.equals(address, other.address) && Objects.equals(port, other.port) && Objects.equals(systemName, other.systemName);
	}
}
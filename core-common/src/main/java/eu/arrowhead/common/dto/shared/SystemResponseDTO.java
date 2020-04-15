package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SystemResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3919207845374510215L;
	
	private long id;
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
	public SystemResponseDTO(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo, final String createdAt, final String upDatedAt) {
		this.id = systemId;
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.createdAt = createdAt;
		this.updatedAt = upDatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;	}
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() {	return authenticationInfo; }
	public String getUpdatedAt() { return updatedAt; }
	public String getCreatedAt() { return createdAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long sytemId) { this.id = sytemId; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
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
		
		final SystemResponseDTO other = (SystemResponseDTO) obj;
		
		return Objects.equals(address, other.address) && Objects.equals(port, other.port) && Objects.equals(systemName, other.systemName);
	}
}
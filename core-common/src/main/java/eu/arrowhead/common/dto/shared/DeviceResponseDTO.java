package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class DeviceResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 3919207845374510215L;

	private long id;
	private String deviceName;
	private String address;
	private String macAddress;
	private String authenticationInfo;
	private String createdAt;
	private String updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public DeviceResponseDTO(final long deviceId, final String deviceName, final String address, final String macAddress, final String authenticationInfo, final String createdAt, final String upDatedAt) {
		this.id = deviceId;
		this.deviceName = deviceName;
		this.address = address;
		this.macAddress = macAddress;
		this.authenticationInfo = authenticationInfo;
		this.createdAt = createdAt;
		this.updatedAt = upDatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;	}
	public String getDeviceName() { return deviceName; }
	public String getAddress() { return address; }
	public String getMacAddress() { return macAddress; }
	public String getAuthenticationInfo() {	return authenticationInfo; }
	public String getUpdatedAt() { return updatedAt; }
	public String getCreatedAt() { return createdAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long sytemId) { this.id = sytemId; }
	public void setDeviceName(final String deviceName) { this.deviceName = deviceName; }
	public void setAddress(final String address) { this.address = address; }
	public void setMacAddress(final String macAddress) { this.macAddress = macAddress; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(address, macAddress, deviceName);
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
		final DeviceResponseDTO other = (DeviceResponseDTO) obj;
		
		return Objects.equals(address, other.address) &&
				Objects.equals(macAddress, other.macAddress) &&
				Objects.equals(deviceName, other.deviceName);
	}
}
package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class DeviceRequestDTO implements Serializable
{

    //=================================================================================================
    // members

    private static final long serialVersionUID = 3919207845374510215L;

    private String deviceName;
    private String address;
    private String macAddress;
    private String authenticationInfo;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public DeviceRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
    public DeviceRequestDTO(final String deviceName, final String address, final String macAddress, final String authenticationInfo)
    {
        this.deviceName = deviceName;
        this.address = address;
        this.macAddress = macAddress;
        this.authenticationInfo = authenticationInfo;
    }

    //-------------------------------------------------------------------------------------------------
    public String getDeviceName() { return deviceName; }

    //-------------------------------------------------------------------------------------------------
    public void setDeviceName(final String deviceName) { this.deviceName = deviceName; }

    public String getAddress() { return address; }

    public void setAddress(final String address) { this.address = address; }

    public String getMacAddress() { return macAddress; }

    public void setMacAddress(final String macAddress) { this.macAddress = macAddress; }

    public String getAuthenticationInfo() { return authenticationInfo; }

    public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public int hashCode()
    {
        return Objects.hash(address, macAddress, deviceName);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final DeviceRequestDTO other = (DeviceRequestDTO) obj;

        return Objects.equals(address, other.address) &&
                Objects.equals(macAddress, other.macAddress) &&
                Objects.equals(deviceName, other.deviceName);
    }
}
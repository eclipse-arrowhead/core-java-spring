package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class OnboardingWithNameRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1L;

	private String deviceName;

	//=================================================================================================
	// methods
	//-------------------------------------------------------------------------------------------------
	public String getDeviceName() {	return deviceName; }

	//-------------------------------------------------------------------------------------------------
	public void setDeviceName(final String deviceName) { this.deviceName = deviceName; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(deviceName);
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
		final OnboardingWithNameRequestDTO other = (OnboardingWithNameRequestDTO) obj;
		
		return Objects.equals(deviceName, other.deviceName);
	}
}
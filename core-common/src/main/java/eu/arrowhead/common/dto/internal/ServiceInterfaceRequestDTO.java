package eu.arrowhead.common.dto.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public class ServiceInterfaceRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -5448099699710703982L;

	private String interfaceName;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceInterfaceRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterfaceRequestDTO(final String interfaceName) {
		this.interfaceName = interfaceName;
	}

	//-------------------------------------------------------------------------------------------------
	public String getInterfaceName() { return interfaceName; }

	//-------------------------------------------------------------------------------------------------
	public void setInterfaceName(final String interfaceName) { this.interfaceName = interfaceName; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}

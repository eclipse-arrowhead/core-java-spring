package eu.arrowhead.common.dto.internal;

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

}

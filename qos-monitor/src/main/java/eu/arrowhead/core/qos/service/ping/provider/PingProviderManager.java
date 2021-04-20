package eu.arrowhead.core.qos.service.ping.provider;

import java.util.List;

import eu.arrowhead.core.qos.dto.IcmpPingResponse;

public interface PingProviderManager {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<IcmpPingResponse> ping(final String address);

}

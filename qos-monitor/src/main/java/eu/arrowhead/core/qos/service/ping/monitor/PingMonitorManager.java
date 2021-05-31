package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.List;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;

public interface PingMonitorManager {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<IcmpPingResponse> ping(final String address) throws ArrowheadException;

	//-------------------------------------------------------------------------------------------------
	public void init() throws ArrowheadException;
}

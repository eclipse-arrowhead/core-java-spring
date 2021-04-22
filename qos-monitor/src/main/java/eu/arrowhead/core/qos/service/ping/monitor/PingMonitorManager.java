package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.List;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;

public interface PingMonitorManager {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<IcmpPingResponse> ping(final String address) throws ArrowheadException;
	//TODO 
	// start loop
	// check if provider orchestrated and active
	// if not orchestrat 
	// if yes ping
	// if pingprovider not available log
	
	//-------------------------------------------------------------------------------------------------
	public void init() throws ArrowheadException;//TODO try sr with timeout, trz orch with time out -> exit if uncussess
	//orch service pingprovider with time - finish int if uncucesss, 
	//in orhcestrator flags
	// metadatasearch true
	//matchmaking true
	//
	// matadatareq.. fill with shema
	// enableIntercloud false
}

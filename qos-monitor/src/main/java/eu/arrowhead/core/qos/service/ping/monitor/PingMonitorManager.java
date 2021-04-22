package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.List;

import eu.arrowhead.core.qos.dto.IcmpPingResponse;

public interface PingMonitorManager {//RENAME anything but PINGPROVIDER conflict with sr query

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<IcmpPingResponse> ping(final String address);//TODO throw SPECIALIZED EXCEPTION
	//TODO 
	// start loop
	// check if provider orchestrated and active
	// if not orchestrat 
	// if yes ping
	// if pingprovider not available log
	
	//-------------------------------------------------------------------------------------------------
	public void init();//TODO try sr with timeout, trz orch with time out -> exit if uncussess
	//orch service pingprovider with time - finish int if uncucesss, 
	//in orhcestrator flags
	// metadatasearch true
	//matchmaking true
	//
	// matadatareq.. fill with shema
	// enableIntercloud false
}

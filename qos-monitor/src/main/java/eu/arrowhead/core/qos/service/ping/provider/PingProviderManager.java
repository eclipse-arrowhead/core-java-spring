package eu.arrowhead.core.qos.service.ping.provider;

import java.util.List;

import eu.arrowhead.core.qos.dto.IcmpPingResponse;

public interface PingProviderManager {//RENAME anything but PINGPROVIDER conflict with sr query

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<IcmpPingResponse> ping(final String address);//TODO throw SPECIALIZED EXEPTION
	//TODO 
	// check if provider orchestrated 
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

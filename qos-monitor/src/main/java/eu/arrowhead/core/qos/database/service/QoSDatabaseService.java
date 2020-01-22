package eu.arrowhead.core.qos.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;

@Service
public class QoSDatabaseService {

	//=================================================================================================
	// members
	
	@Autowired
	private QoSIntraMeasurementRepository qosIntraMeasurementRepository;
	
	@Autowired
	private QoSIntraMeasurementPingRepository qosIntraMeasurementPingRepository;
}
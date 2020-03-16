package eu.arrowhead.core.qos.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@Service
public class RelayEchoService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private QoSDBService qosDBService;
	
	@Autowired
	private QoSMonitorDriver gosMonitorDriver;
	
	private final Logger logger = LogManager.getLogger(RelayEchoService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementListResponseDTO getInterRelayEchoMeasurements(final CloudRequestDTO request) {
		logger.debug("getInterRelayEchoMeasurements started...");
		
		List<QoSInterRelayMeasurement> measurements = qosDBService.getInterRelayMeasurementByCloud(validateAndGetCloud(request));
		
		List<QoSInterRelayEchoMeasurement> echoMeasurements = new ArrayList<>();
		for (QoSInterRelayMeasurement measurement : measurements) {
			Optional<QoSInterRelayEchoMeasurement> optional = qosDBService.getInterRelayEchoMeasurementByMeasurement(measurement);
			if (optional.isPresent()) {
				echoMeasurements.add(optional.get());
			}
		}
		
		return DTOConverter.convertQoSInterRelayEchoMeasurementPageToQoSInterRelayEchoMeasurementListResponseDTO(new PageImpl<>(echoMeasurements));
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO validateAndGetCloud(final CloudRequestDTO request) {
		logger.debug("validateAndGetCloud started...");
		
		if (request == null) {
			throw new InvalidParameterException("CloudRequestDTO is null.");
		}		
		
		if (Utilities.isEmpty(request.getOperator())) {
			throw new InvalidParameterException("Cloud operator is null or blank");
		}
		
		if (Utilities.isEmpty(request.getName())) {
			throw new InvalidParameterException("Cloud name is null or empty");
		}
		
		return gosMonitorDriver.queryGatekeeperCloudInfo(request.getOperator(), request.getName());
	}
}

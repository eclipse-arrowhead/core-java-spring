package eu.arrowhead.core.qos.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResultDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResultListDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@Service
public class RelayEchoService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private QoSDBService qosDBService;
	
	private final Logger logger = LogManager.getLogger(RelayEchoService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementResultListDTO calculateInterRelayEchoMeasurements(final CloudSystemFormDTO request) {
		logger.debug("calculateInterRelayEchoMeasurements started...");
		validateCloudSystemForm(request);
		
		final QoSIntraPingMeasurement systemPing = qosDBService.getIntraPingMeasurementBySystemId(request.getSystem().getId());
		
		final List<QoSInterRelayMeasurement> measurementList = qosDBService.getInterRelayEchoMeasurementByCloud(request.getCloud());
		final List<QoSInterRelayEchoMeasurementResultDTO> responseData = new ArrayList<>();
		for (final QoSInterRelayMeasurement relayMeasurement : measurementList) {
			final Optional<QoSInterRelayEchoMeasurement> relayResultOpt = qosDBService.getInterRelayEchoMeasurementByMeasurement(relayMeasurement);
			if (relayResultOpt.isEmpty()) {
				continue;
			}
			final QoSInterRelayEchoMeasurement relayResult = relayResultOpt.get();
			final QoSInterRelayEchoMeasurementResultDTO result = new QoSInterRelayEchoMeasurementResultDTO(request.getCloud(),
																										   request.getSystem(),
																										   DTOConverter.convertRelayToRelayResponseDTO(relayMeasurement.getRelay()),
																										   relayMeasurement.getMeasurementType(),
																										   systemPing.getLastAccessAt(),
																										   relayResult.getMinResponseTime() + systemPing.getMinResponseTime(),
																										   relayResult.getMaxResponseTime() + systemPing.getMaxResponseTime(),
																										   relayResult.getMeanResponseTimeWithTimeout() + systemPing.getMeanResponseTimeWithTimeout(),
																										   relayResult.getMeanResponseTimeWithoutTimeout() + systemPing.getMeanResponseTimeWithoutTimeout(),
																										   relayResult.getJitterWithTimeout() + systemPing.getJitterWithTimeout(),
																										   relayResult.getJitterWithoutTimeout() + systemPing.getJitterWithoutTimeout(),
																										   relayResult.getLostPerMeasurementPercent() + systemPing.getLostPerMeasurementPercent());
			responseData.add(result);
		}
		
		return new QoSInterRelayEchoMeasurementResultListDTO(responseData, responseData.size());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validateCloudSystemForm(final CloudSystemFormDTO request) {
		logger.debug("validateCloudSystemForm started...");
		
		if (request == null) {
			throw new InvalidParameterException("CloudRelayFormDTO is null.");
		}		
		
		if (request.getCloud() == null) {
			throw new InvalidParameterException("Cloud is null");
		}
		
		if (Utilities.isEmpty(request.getCloud().getOperator())) {
			throw new InvalidParameterException("Cloud operator is null or blank");
		}
		
		if (Utilities.isEmpty(request.getCloud().getName())) {
			throw new InvalidParameterException("Cloud name is null or empty");
		}
		
		if (request.getSystem() == null) {
			throw new InvalidParameterException("System is null");
		}
		
		if (request.getSystem().getId() < 1) {
			throw new InvalidParameterException("System id less than 1");
		}
		
		if (Utilities.isEmpty(request.getSystem().getSystemName())) {
			throw new InvalidParameterException("System name is null or blank");
		}
		
		if (Utilities.isEmpty(request.getSystem().getAddress())) {
			throw new InvalidParameterException("System address is null or empty");
		}
	}
}

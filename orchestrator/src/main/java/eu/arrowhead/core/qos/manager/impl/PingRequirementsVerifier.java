/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.qos.manager.impl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;
import eu.arrowhead.core.qos.manager.QoSVerifier;

public class PingRequirementsVerifier implements QoSVerifier {
	
	//=================================================================================================
	// members
	
	private static final List<String> relevantQoSRequirementKeys = List.of(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD,
																		   OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS,
																		   OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS); 
	
	@Value(CoreCommonConstants.$QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT)
	private boolean verifyNotMeasuredSystem; // result of verify if a system has no ping records
	
	@Value(CoreCommonConstants.$QOS_PING_MEASUREMENT_CACHE_THRESHOLD_WD)
	private int pingMeasurementCacheThreshold;
	
	@Autowired
	private OrchestratorDriver orchestratorDriver;
	
	private static final Logger logger = LogManager.getLogger(PingRequirementsVerifier.class);
	
	private final Map<Long,QoSIntraPingMeasurementResponseDTO> intraPingMeasurementCache = new ConcurrentHashMap<>();
	private final Map<String,QoSInterDirectPingMeasurementResponseDTO> interDirectPingMeasurementCache = new ConcurrentHashMap<>();
	private final Map<String,QoSInterRelayEchoMeasurementListResponseDTO> interRelayEchoMeasurementCache = new ConcurrentHashMap<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean verify(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verify started...");
		validateInput(parameters);
		
		if (!parameters.isInterCloud()) {			
			return verifyIntraCloudPingMeasurements(parameters);
		} else if(!parameters.isGatewayIsMandatory()) {
			return verifyInterCloudDirectPingMeasurements(parameters);
		} else {
			return verifyInterCloudRelayEchoAndPingMeasurements(parameters);
		}		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateInput(final QoSVerificationParameters parameters) {
		logger.debug("validateInput started...");
		
		Assert.notNull(parameters, "'parameters' is null");
		parameters.validateParameters();
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasRelevantQoSRequirement(final QoSVerificationParameters params) {
		logger.debug("hasRelevantQoSRequirement started...");
		
		if (params.getQosRequirements() == null || params.getQosRequirements().isEmpty()) { // no need to verify anything
			return false;
		}
		
		for (final String key : relevantQoSRequirementKeys) {
			if (params.getQosRequirements().containsKey(key)) {
				return true;
			}
		}

		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyIntraCloudPingMeasurements(final QoSVerificationParameters params) {
		logger.debug("verifyIntraCloudPingMeasuerements started...");
		Assert.isTrue(!params.isInterCloud(), "QoSVerificationParameters is Inter-Cloud, but Intra-Cloud ping verification was requested");
		
		if (!hasRelevantQoSRequirement(params)) { // no need to verify anything
			return true;
		}
		
		final QoSIntraPingMeasurementResponseDTO measurement = getIntraPingMeasurement(params.getProviderSystem().getId());
		
		if (!measurement.hasRecord()) { // no record => use related constant to determine output 
			return verifyNotMeasuredSystem;
		}
		
		if (!measurement.isAvailable()) {
			return false;
		}
		
		if (hasMaximumResponseTimeThreshold(params.getQosRequirements())) {
			final int threshold = getMaximumResponseTimeThreshold(params.getQosRequirements());
			if (measurement.getMaxResponseTime().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of maximum response time threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasAverageResponseTimeThreshold(params.getQosRequirements())) {
			final int threshold = getAverageResponseTimeThreshold(params.getQosRequirements());
			if (measurement.getMeanResponseTimeWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of average response time threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasJitterThreshold(params.getQosRequirements())) {
			final int threshold = getJitterThreshold(params.getQosRequirements());
			if (measurement.getJitterWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of jitter threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasRecentPacketLossThreshold(params.getQosRequirements())) {
			final double threshold = getRecentPacketLossThreshold(params.getQosRequirements());
			final double recentPacketLoss = 1 - measurement.getReceived() / (double) measurement.getSent();
			if (recentPacketLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of recent packet loss threshold", params.getProviderSystem().getSystemName(),params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasPacketLossThreshold(params.getQosRequirements())) {
			final double threshold = getPacketLossThreshold(params.getQosRequirements());
			final double packetLoss = 1 - measurement.getReceivedAll() / (double) measurement.getSentAll();
			if (packetLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of packet loss threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyInterCloudDirectPingMeasurements(final QoSVerificationParameters params) {
		logger.debug("verifyInterCloudDirectPingMeasurements started...");
		Assert.isTrue(params.isInterCloud(), "QoSVerificationParameters is not Inter-Cloud, but Inter-Cloud direct ping verification was requested");
		Assert.isTrue(!params.isGatewayIsMandatory(), "Gateway shouldn't be mandatory for Inter-Cloud direct ping verification");
		
		if (!hasRelevantQoSRequirement(params)) { // no need to verify anything
			return true;
		}
		
		final QoSInterDirectPingMeasurementResponseDTO measurement = getInterDirectPingMeasurement(new CloudSystemFormDTO(params.getProviderCloud(), params.getProviderSystem()));
		
		if (!measurement.hasRecord()) { // no record => use related constant to determine output 
			return verifyNotMeasuredSystem;
		}
		
		if (!measurement.isAvailable()) {
			return false;
		}
		
		if (hasMaximumResponseTimeThreshold(params.getQosRequirements())) {
			final int threshold = getMaximumResponseTimeThreshold(params.getQosRequirements());
			if (measurement.getMaxResponseTime().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of maximum response time threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasAverageResponseTimeThreshold(params.getQosRequirements())) {
			final int threshold = getAverageResponseTimeThreshold(params.getQosRequirements());
			if (measurement.getMeanResponseTimeWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of average response time threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasJitterThreshold(params.getQosRequirements())) {
			final int threshold = getJitterThreshold(params.getQosRequirements());
			if (measurement.getJitterWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of jitter threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasRecentPacketLossThreshold(params.getQosRequirements())) {
			final double threshold = getRecentPacketLossThreshold(params.getQosRequirements());
			final double recentPacketLoss = 1 - measurement.getReceived() / (double) measurement.getSent();
			if (recentPacketLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of recent packet loss threshold", params.getProviderSystem().getSystemName(),params.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasPacketLossThreshold(params.getQosRequirements())) {
			final double threshold = getPacketLossThreshold(params.getQosRequirements());
			final double packetLoss = 1 - measurement.getReceivedAll() / (double) measurement.getSentAll();
			if (packetLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of packet loss threshold", params.getProviderSystem().getSystemName(), params.getProviderSystem().getId());
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyInterCloudRelayEchoAndPingMeasurements(final QoSVerificationParameters params) {
		logger.debug("verifyInterCloudRelayEchoAndPingMeasurements started...");
		Assert.isTrue(params.isInterCloud(), "QoSVerificationParameters is not Inter-Cloud, but Inter-Cloud relay echo and ping verification was requested");
		Assert.isTrue(params.isGatewayIsMandatory(), "Gateway should be mandatory for Inter-Cloud relay echo and ping verification");
		
		if (!hasRelevantQoSRequirement(params)) { // verify only whether there are possible relays or not
			verifyAllPossibleRelays(params);
			return !params.getVerifiedRelays().isEmpty();
		}
		
		final QoSInterRelayEchoMeasurementListResponseDTO relayMeasurementList = getInterRelayEchoMeasurement(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(params.getProviderCloud()));
		
		if (relayMeasurementList.getData() == null || relayMeasurementList.getData().isEmpty()) { // no record => use related constant to determine output 
			if (verifyNotMeasuredSystem) {
				verifyAllPossibleRelays(params);
			}
			return verifyNotMeasuredSystem && !params.getVerifiedRelays().isEmpty();
		}

		if (!params.getProviderTargetCloudMeasurement().isProviderAvailable()) {
			return false;
		}
		
		for (final QoSInterRelayEchoMeasurementResponseDTO relayMeasurement : relayMeasurementList.getData()) {
			final String relayAddressPort = relayMeasurement.getMeasurement().getRelay().getAddress() + ":" + relayMeasurement.getMeasurement().getRelay().getPort();
			boolean verified =  true;			
			
			if (hasMaximumResponseTimeThreshold(params.getQosRequirements())) {
				final int threshold = getMaximumResponseTimeThreshold(params.getQosRequirements());
				final int measuredValue = relayMeasurement.getMaxResponseTime().intValue() +
										  params.getLocalReferencePingMeasurement().getMaxResponseTime().intValue() +
										  params.getProviderTargetCloudMeasurement().getMaxResponseTime().intValue();
				if (measuredValue > threshold) {
					logger.debug("Provider '{}' (via Relay: {}) removed because of maximum response time threshold", params.getProviderSystem().getSystemName(), relayAddressPort);
					verified = false;
				}
			}
			
			if (hasAverageResponseTimeThreshold(params.getQosRequirements())) {
				final int threshold = getAverageResponseTimeThreshold(params.getQosRequirements());
				final int measuredValue = relayMeasurement.getMeanResponseTimeWithoutTimeout().intValue() +
										  params.getLocalReferencePingMeasurement().getMeanResponseTimeWithoutTimeout().intValue() + 
										  params.getProviderTargetCloudMeasurement().getMeanResponseTimeWithoutTimeout().intValue();
				if (measuredValue > threshold) {
					logger.debug("Provider '{}' (via Relay: {}) removed because of average response time threshold", params.getProviderSystem().getSystemName(), relayAddressPort);
					verified = false;
				}
			}
			
			if (hasJitterThreshold(params.getQosRequirements())) {
				final int threshold = getJitterThreshold(params.getQosRequirements());
				final int measuredValue = relayMeasurement.getJitterWithoutTimeout().intValue() +
										  params.getLocalReferencePingMeasurement().getJitterWithoutTimeout().intValue() +
										  params.getProviderTargetCloudMeasurement().getJitterWithoutTimeout().intValue();
				if (measuredValue > threshold) {
					logger.debug("Provider '{}' (via Relay: {}) removed because of jitter threshold", params.getProviderSystem().getSystemName(), relayAddressPort);
					verified = false;
				}
			}
			
			if (hasRecentPacketLossThreshold(params.getQosRequirements())) {
				final double threshold = getRecentPacketLossThreshold(params.getQosRequirements());
				final double recentPacketLoss = 1 - (relayMeasurement.getReceived() + params.getLocalReferencePingMeasurement().getReceived() + params.getProviderTargetCloudMeasurement().getReceived())
												/ (double) (relayMeasurement.getSent() + params.getLocalReferencePingMeasurement().getSent() + params.getProviderTargetCloudMeasurement().getSent());
				if (recentPacketLoss > threshold) {
					logger.debug("Provider '{}' (via Relay: {}) removed because of recent packet loss threshold", params.getProviderSystem().getSystemName(), relayAddressPort);
					verified = false;
				}
			}
			
			if (hasPacketLossThreshold(params.getQosRequirements())) {
				final double threshold = getPacketLossThreshold(params.getQosRequirements());
				final double packetLoss = 1 - (relayMeasurement.getReceivedAll() + params.getLocalReferencePingMeasurement().getReceivedAll() + params.getProviderTargetCloudMeasurement().getReceivedAll())
										  / (double) (relayMeasurement.getSentAll() + params.getLocalReferencePingMeasurement().getSentAll() + params.getProviderTargetCloudMeasurement().getSentAll());
				if (packetLoss > threshold) {
					logger.debug("Provider '{}' (via Relay: {}) removed because of packet loss threshold", params.getProviderSystem().getSystemName(), relayAddressPort);
					verified =  false;
				}
			}
			
			if (verified) {
				params.getVerifiedRelays().add(relayMeasurement.getMeasurement().getRelay());
			}
			
		}
		
		if (params.getVerifiedRelays().isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasMaximumResponseTimeThreshold(final Map<String,String> qosRequirements) {
		return qosRequirements.containsKey(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasAverageResponseTimeThreshold(final Map<String,String> qosRequirements) {
		return qosRequirements.containsKey(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasJitterThreshold(final Map<String,String> qosRequirements) {
		return qosRequirements.containsKey(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasRecentPacketLossThreshold(final Map<String,String> qosRequirements) {
		return qosRequirements.containsKey(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean hasPacketLossThreshold(final Map<String,String> qosRequirements) {
		return qosRequirements.containsKey(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private int getMaximumResponseTimeThreshold(final Map<String,String> qosRequirements) {
		logger.debug("getMaximumResponseTimeThreshold started...");
		
		final String valueStr = qosRequirements.get(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD);
		try {
			final int value = Integer.parseInt(valueStr);
			if (value <= 0) {
				throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD + " has invalid value: " + valueStr);
			}
			
			return value;
		} catch (final NumberFormatException ex) {
			throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD + " has invalid value: " + valueStr);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private int getAverageResponseTimeThreshold(final Map<String,String> qosRequirements) {
		logger.debug("getAverageResponseTimeThreshold started...");
		
		final String valueStr = qosRequirements.get(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD);
		try {
			final int value = Integer.parseInt(valueStr);
			if (value <= 0) {
				throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD + " has invalid value: " + valueStr);
			}
			
			return value;
		} catch (final NumberFormatException ex) {
			throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD + " has invalid value: " + valueStr);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private int getJitterThreshold(final Map<String,String> qosRequirements) {
		logger.debug("getJitterThreshold started...");
		
		final String valueStr = qosRequirements.get(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD);
		try {
			final int value = Integer.parseInt(valueStr);
			if (value < 0) {
				throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD + " has invalid value: " + valueStr);
			}
			
			return value;
		} catch (final NumberFormatException ex) {
			throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD + " has invalid value: " + valueStr);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private double getRecentPacketLossThreshold(final Map<String,String> qosRequirements) {
		logger.debug("getRecentPacketLossThreshold started...");
		
		final String valueStr = qosRequirements.get(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS);
		try {
			final int value = Integer.parseInt(valueStr);
			if (value < 0) {
				throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS + " has invalid value: " + valueStr);
			}
			
			return value / 100.;
		} catch (final NumberFormatException ex) {
			throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS + " has invalid value: " + valueStr);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private double getPacketLossThreshold(final Map<String,String> qosRequirements) {
		logger.debug("getPacketLossThreshold started...");
		
		final String valueStr = qosRequirements.get(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS);
		try {
			final int value = Integer.parseInt(valueStr);
			if (value < 0) {
				throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS + " has invalid value: " + valueStr);
			}
			
			return value / 100.;
		} catch (final NumberFormatException ex) {
			throw new InvalidParameterException(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS + " has invalid value: " + valueStr);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementResponseDTO getIntraPingMeasurement(final long systemId) {
		logger.debug("getIntraPingMeasurement started...");
		
		final QoSIntraPingMeasurementResponseDTO measurement = intraPingMeasurementCache.get(systemId);
		
		if (measurement == null) {
			return getIntraPingMeasurementFromQoSMonitor(systemId);
		}
		
		final ZonedDateTime lastMeasurementAt = Utilities.parseUTCStringToLocalZonedDateTime(measurement.getMeasurement().getLastMeasurementAt());
		if (lastMeasurementAt.plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
			intraPingMeasurementCache.remove(systemId);
			return getIntraPingMeasurementFromQoSMonitor(systemId);
		}
		
		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementResponseDTO getIntraPingMeasurementFromQoSMonitor(final long systemId) {
		logger.debug("getPIntraingMeasurementFromQoSMonitor started...");
		
		final QoSIntraPingMeasurementResponseDTO measurement = orchestratorDriver.getIntraPingMeasurement(systemId);
		
		if (measurement.hasRecord() && measurement.isAvailable()) { // only caching when there are some data
			intraPingMeasurementCache.put(systemId, measurement);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurementResponseDTO getInterDirectPingMeasurement(final CloudSystemFormDTO request) {
		logger.debug("getInterDirectPingMeasurement started...");
		
		final QoSInterDirectPingMeasurementResponseDTO measurement = interDirectPingMeasurementCache.get(getCloudSystemCacheKey(request));
		
		if (measurement == null) {
			return getInterDirectPingMeasurementFromQoSMonitor(request);
		}
		
		if (Utilities.parseUTCStringToLocalZonedDateTime(measurement.getMeasurement().getLastMeasurementAt()).plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
			interDirectPingMeasurementCache.remove(getCloudSystemCacheKey(request));
			return getInterDirectPingMeasurementFromQoSMonitor(request);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurementResponseDTO getInterDirectPingMeasurementFromQoSMonitor(final CloudSystemFormDTO request) {
		logger.debug("getInterDirectPingMeasurementFromQoSMonitor started...");
		
		final QoSInterDirectPingMeasurementResponseDTO measurement = orchestratorDriver.getInterDirectPingMeasurement(request);
		
		if (measurement.hasRecord() && measurement.isAvailable()) { // only caching when there are some data
			interDirectPingMeasurementCache.put(getCloudSystemCacheKey(request), measurement);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurementListResponseDTO getInterRelayEchoMeasurement(final CloudRequestDTO request) {
		logger.debug("getInterRelayEchoMeasurement started...");
		
		final QoSInterRelayEchoMeasurementListResponseDTO measurement = interRelayEchoMeasurementCache.get(getCloudCacheKey(request));
		
		if (measurement == null) {
			return getInterRelayEchoMeasurementFromQoSMonitor(request);
		}
		
		for (final QoSInterRelayEchoMeasurementResponseDTO dto : measurement.getData()) {			
			if (Utilities.parseUTCStringToLocalZonedDateTime(dto.getMeasurement().getLastMeasurementAt()).plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
				interRelayEchoMeasurementCache.remove(getCloudCacheKey(request));
				return getInterRelayEchoMeasurementFromQoSMonitor(request);
			}
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurementListResponseDTO getInterRelayEchoMeasurementFromQoSMonitor(final CloudRequestDTO request) {
		logger.debug("getInterRelayEchoMeasurementFromQoSMonitor started...");
		
		final QoSInterRelayEchoMeasurementListResponseDTO measurement = orchestratorDriver.getInterRelayEchoMeasurement(request);
		
		if (measurement.getData() != null && !measurement.getData().isEmpty()) { // only caching when there are some data
			interRelayEchoMeasurementCache.put(getCloudCacheKey(request), measurement);
		}			
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void verifyAllPossibleRelays(final QoSVerificationParameters params) {
		final List<RelayResponseDTO> possibleRelays = orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays(params.getProviderCloud().getOperator(), params.getProviderCloud().getName())
																		.getGatewayRelays();
		params.getVerifiedRelays().addAll(possibleRelays);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getCloudSystemCacheKey(final CloudSystemFormDTO request) {
		return request.getSystem().getSystemName() + "." + request.getCloud().getName() + "." + request.getCloud().getOperator();
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getCloudCacheKey(final CloudRequestDTO request) {
		return request.getName() + "." + request.getOperator();
	}
}

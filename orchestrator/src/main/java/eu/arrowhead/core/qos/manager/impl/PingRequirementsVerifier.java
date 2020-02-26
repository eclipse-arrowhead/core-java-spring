package eu.arrowhead.core.qos.manager.impl;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;
import eu.arrowhead.core.qos.manager.QoSVerifier;

public class PingRequirementsVerifier implements QoSVerifier {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT)
	private boolean verifyNotMeasuredSystem; // result of verify if a system has no ping records
	
	@Value(CoreCommonConstants.$QOS_PING_MEASUREMENT_CACHE_THRESHOLD_WD)
	private int pingMeasurementCacheThreshold;
	
	@Autowired
	private OrchestratorDriver orchestrationDriver;
	
	private static final Logger logger = LogManager.getLogger(PingRequirementsVerifier.class);
	
	private Map<Long,PingMeasurementResponseDTO> pingMeasurementCache = new ConcurrentHashMap<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean verify(final OrchestrationResultDTO result, final Map<String,String> qosRequirements, final Map<String,String> commands) {
		logger.debug("verify started...");
		validateInput(result);
		
		if (qosRequirements == null || qosRequirements.isEmpty()) { // no need to verify anything
			return true;
		}
		
		final PingMeasurementResponseDTO measurement = getPingMeasurement(result.getProvider().getId());
		
		if (!measurement.hasRecord()) { // no record => use related constant to determine output 
			return verifyNotMeasuredSystem;
		}
		
		if (!measurement.isAvailable()) {
			return false;
		}
		
		if (hasMaximumResponseTimeThreshold(qosRequirements)) {
			final int threshold = getMaximumResponseTimeThreshold(qosRequirements);
			if (measurement.getMaxResponseTime().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of maximum response time threshold", result.getProvider().getSystemName(), result.getProvider().getId());
				return false;
			}
		}
		
		if (hasAverageResponseTimeThreshold(qosRequirements)) {
			final int threshold = getAverageResponseTimeThreshold(qosRequirements);
			if (measurement.getMeanResponseTimeWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of average response time threshold", result.getProvider().getSystemName(), result.getProvider().getId());
				return false;
			}
		}
		
		if (hasJitterThreshold(qosRequirements)) {
			final int threshold = getJitterThreshold(qosRequirements);
			if (measurement.getJitterWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of jitter threshold", result.getProvider().getSystemName(), result.getProvider().getId());
				return false;
			}
		}
		
		if (hasRecentPacketLossThreshold(qosRequirements)) {
			final double threshold = getRecentPacketLossThreshold(qosRequirements);
			final double recentPacketLoss = 1 - measurement.getReceived() / (double) measurement.getSent();
			if (recentPacketLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of recent packet loss threshold", result.getProvider().getSystemName(), result.getProvider().getId());
				return false;
			}
		}
		
		if (hasPacketLossThreshold(qosRequirements)) {
			final double threshold = getPacketLossThreshold(qosRequirements);
			final double packetLoss = 1 - measurement.getReceivedAll() / (double) measurement.getSentAll();
			if (packetLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of packet loss threshold", result.getProvider().getSystemName(), result.getProvider().getId());
				return false;
			}
		}
		
		return true;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateInput(final OrchestrationResultDTO result) {
		logger.debug("validateInput started...");
		
		Assert.notNull(result, "'result' is null");
		Assert.notNull(result.getProvider(), "Provider is null");
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
	private PingMeasurementResponseDTO getPingMeasurement(final long systemId) {
		logger.debug("getPingMeasurement started...");
		
		final PingMeasurementResponseDTO measurement = pingMeasurementCache.get(systemId);
		
		if (measurement == null) {
			return getPingMeasurementFromQoSMonitor(systemId);
		}
		
		if (measurement.getMeasurement().getLastMeasurementAt().plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
			pingMeasurementCache.remove(systemId);
			return getPingMeasurementFromQoSMonitor(systemId);
		}
		
		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementResponseDTO getPingMeasurementFromQoSMonitor(final long systemId) {
		logger.debug("getPingMeasurementFromQoSMonitor started...");
		
		final PingMeasurementResponseDTO measurement = orchestrationDriver.getPingMeasurement(systemId);
		
		if (measurement.hasRecord() && measurement.isAvailable()) { // only caching when there are some data
			pingMeasurementCache.put(systemId, measurement);
		}
		
		return measurement;
	}
}
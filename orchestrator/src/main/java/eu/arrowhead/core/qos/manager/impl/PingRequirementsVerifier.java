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
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
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
	
	private Map<Long,QoSIntraPingMeasurementResponseDTO> intraPingMeasurementCache = new ConcurrentHashMap<>();
	private Map<String,QoSInterDirectPingMeasurementResponseDTO> interDirectPingMeasurementCache = new ConcurrentHashMap<>();
	private Map<String,QoSInterRelayEchoMeasurementResponseDTO> interRelayEchoMeasurementCache = new ConcurrentHashMap<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean verify(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verify started...");
		validateInput(parameters);
		
		if (parameters.getQosRequirements() == null || parameters.getQosRequirements().isEmpty()) { // no need to verify anything
			return true;
		}
		
		if (!parameters.isInterCloud()) {			
			return verifyIntraCloudPingMeasurements(parameters, isPreVerification);
		} else if(!parameters.isGatewayIsMandatory()) {
			return verifyInterCloudDirectPingMeasurements(parameters, isPreVerification);
		} else {
			return verifyInterCloudRelayEchoAndPingMeasurements(parameters, isPreVerification);
		}		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateInput(final QoSVerificationParameters parameters) {
		logger.debug("validateInput started...");
		
		Assert.notNull(parameters, "'parameters' is null");
		Assert.notNull(parameters.getProviderSystem(), "Provider is null");
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyIntraCloudPingMeasurements(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verifyIntraCloudPingMeasuerements started...");
		
		final QoSIntraPingMeasurementResponseDTO measurement = getIntraPingMeasurement(parameters.getProviderSystem().getId());
		
		if (!measurement.hasRecord()) { // no record => use related constant to determine output 
			return verifyNotMeasuredSystem;
		}
		
		if (!measurement.isAvailable()) {
			return false;
		}
		
		if (hasMaximumResponseTimeThreshold(parameters.getQosRequirements())) {
			final int threshold = getMaximumResponseTimeThreshold(parameters.getQosRequirements());
			if (measurement.getMaxResponseTime().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of maximum response time threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasAverageResponseTimeThreshold(parameters.getQosRequirements())) {
			final int threshold = getAverageResponseTimeThreshold(parameters.getQosRequirements());
			if (measurement.getMeanResponseTimeWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of average response time threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasJitterThreshold(parameters.getQosRequirements())) {
			final int threshold = getJitterThreshold(parameters.getQosRequirements());
			if (measurement.getJitterWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of jitter threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasRecentPacketLossThreshold(parameters.getQosRequirements())) {
			final double threshold = getRecentPacketLossThreshold(parameters.getQosRequirements());
			final double recentPacketLoss = 1 - measurement.getReceived() / (double) measurement.getSent();
			if (recentPacketLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of recent packet loss threshold", parameters.getProviderSystem().getSystemName(),parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasPacketLossThreshold(parameters.getQosRequirements())) {
			final double threshold = getPacketLossThreshold(parameters.getQosRequirements());
			final double packetLoss = 1 - measurement.getReceivedAll() / (double) measurement.getSentAll();
			if (packetLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of packet loss threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyInterCloudDirectPingMeasurements(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verifyInterCloudDirectPingMeasurements started...");
		
		final QoSInterDirectPingMeasurementResponseDTO measurement = getInterDirectPingMeasurement(new CloudSystemFormDTO(parameters.getProviderCloud(), parameters.getProviderSystem()));
		
		if (!measurement.hasRecord()) { // no record => use related constant to determine output 
			return verifyNotMeasuredSystem;
		}
		
		if (!measurement.isAvailable()) {
			return false;
		}
		
		if (hasMaximumResponseTimeThreshold(parameters.getQosRequirements())) {
			final int threshold = getMaximumResponseTimeThreshold(parameters.getQosRequirements());
			if (measurement.getMaxResponseTime().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of maximum response time threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasAverageResponseTimeThreshold(parameters.getQosRequirements())) {
			final int threshold = getAverageResponseTimeThreshold(parameters.getQosRequirements());
			if (measurement.getMeanResponseTimeWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of average response time threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasJitterThreshold(parameters.getQosRequirements())) {
			final int threshold = getJitterThreshold(parameters.getQosRequirements());
			if (measurement.getJitterWithoutTimeout().intValue() > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of jitter threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasRecentPacketLossThreshold(parameters.getQosRequirements())) {
			final double threshold = getRecentPacketLossThreshold(parameters.getQosRequirements());
			final double recentPacketLoss = 1 - measurement.getReceived() / (double) measurement.getSent();
			if (recentPacketLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of recent packet loss threshold", parameters.getProviderSystem().getSystemName(),parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		if (hasPacketLossThreshold(parameters.getQosRequirements())) {
			final double threshold = getPacketLossThreshold(parameters.getQosRequirements());
			final double packetLoss = 1 - measurement.getReceivedAll() / (double) measurement.getSentAll();
			if (packetLoss > threshold) {
				logger.debug("Provider '{}' (id: {}) removed because of packet loss threshold", parameters.getProviderSystem().getSystemName(), parameters.getProviderSystem().getId());
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyInterCloudRelayEchoAndPingMeasurements(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verifyInterCloudRelayEchoAndPingMeasurements started...");
		
		final QoSInterRelayEchoMeasurementResponseDTO relayMeasurement = getInterRelayEchoMeasurement(new CloudSystemFormDTO(parameters.getProviderCloud(), parameters.getProviderSystem()));
		
		if (!relayMeasurement.hasRecord()) { // no record => use related constant to determine output 
			return verifyNotMeasuredSystem;
		}
		
		//TODO bordi
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
		
		if (measurement.getMeasurement().getLastMeasurementAt().plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
			intraPingMeasurementCache.remove(systemId);
			return getIntraPingMeasurementFromQoSMonitor(systemId);
		}
		
		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementResponseDTO getIntraPingMeasurementFromQoSMonitor(final long systemId) {
		logger.debug("getPIntraingMeasurementFromQoSMonitor started...");
		
		final QoSIntraPingMeasurementResponseDTO measurement = orchestrationDriver.getIntraPingMeasurement(systemId);
		
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
		
		if (measurement.getMeasurement().getLastMeasurementAt().plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
			interDirectPingMeasurementCache.remove(getCloudSystemCacheKey(request));
			return getInterDirectPingMeasurementFromQoSMonitor(request);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurementResponseDTO getInterDirectPingMeasurementFromQoSMonitor(final CloudSystemFormDTO request) {
		logger.debug("getInterDirectPingMeasurementFromQoSMonitor started...");
		
		final QoSInterDirectPingMeasurementResponseDTO measurement = orchestrationDriver.getInterDirectPingMeasurement(request);
		
		if (measurement.hasRecord() && measurement.isAvailable()) { // only caching when there are some data
			interDirectPingMeasurementCache.put(getCloudSystemCacheKey(request), measurement);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurementResponseDTO getInterRelayEchoMeasurement(final CloudSystemFormDTO request) {
		logger.debug("getInterRelayEchoMeasurement started...");
		
		QoSInterRelayEchoMeasurementResponseDTO measurement = interRelayEchoMeasurementCache.get(getCloudSystemCacheKey(request));
		
		if (measurement == null) {
			return getInterRelayEchoMeasurement(request);
		}
		
		if (measurement.getMeasurement().getLastMeasurementAt().plusSeconds(pingMeasurementCacheThreshold).isBefore(ZonedDateTime.now())) { // obsolete record
			interRelayEchoMeasurementCache.remove(getCloudSystemCacheKey(request));
			return getInterRelayEchoMeasurement(request);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurementResponseDTO getInterRelayEchoMeasurementFromQoSMonitor(final CloudSystemFormDTO request) {
		logger.debug("getInterRelayEchoMeasurementFromQoSMonitor started...");
		
		QoSInterRelayEchoMeasurementResponseDTO measurement = orchestrationDriver.getInterRelayEchoMeasurement(request);
		
		if (measurement.hasRecord()) { // only caching when there are some data
			interRelayEchoMeasurementCache.put(getCloudSystemCacheKey(request), measurement);
		}
		
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getCloudSystemCacheKey(final CloudSystemFormDTO request) {
		return request.getSystem().getSystemName() + "." + request.getCloud().getName() + "." + request.getCloud().getOperator();
	}
}
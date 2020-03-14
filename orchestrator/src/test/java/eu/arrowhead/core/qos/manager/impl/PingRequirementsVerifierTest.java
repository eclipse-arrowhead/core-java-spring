package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;

@RunWith(SpringRunner.class)
public class PingRequirementsVerifierTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private PingRequirementsVerifier verifier;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private Map<Long,QoSIntraPingMeasurementResponseDTO> pingMeasurementCache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(verifier, "pingMeasurementCacheThreshold", 120);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterNull() {
		verifier.verify(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterProviderNull() {
		verifier.verify(new QoSVerificationParameters(null, null, new HashMap<>(),  new HashMap<>(),  new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyParameterQosRequirementsNull() { // answer true
		final boolean verified = verifier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, new HashMap<>(),  null,  new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyParameterQosRequirementsEmpty() { // answer true
		final boolean verified = verifier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, new HashMap<>(),  new HashMap<>(),  new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//=================================================================================================
	// Intra-Cloud
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testVerifyParameterInvalidSystemId() {
		when(pingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenThrow(new BadPayloadException("test"));
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(-1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testVerifyObsoleteCache() {
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now().minusHours(2));
		response.setMeasurement(measurement);
		when(pingMeasurementCache.get(anyLong())).thenReturn(response);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenThrow(new ArrowheadException("just for finish the method execution"));
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultTrue() {
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		Assert.assertFalse(response.hasRecord());
		when(pingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultFalse() {
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", false);
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		Assert.assertFalse(response.hasRecord());
		when(pingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifySystemNotAvailable() {
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		response.setId(12L);
		response.setAvailable(false);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementInvalid() { // also tests that cache is stored the measurement
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(anyLong())).thenReturn(null);
		when(pingMeasurementCache.put(anyLong(), any())).thenReturn(response);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "invalid");
		
		try {
			verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		} catch (final InvalidParameterException ex) { // catch exception to test the caching
			verify(pingMeasurementCache, times(1)).put(anyLong(), any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementNotPositive() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "0");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementNotVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "30");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "300");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementInvalid() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementNotPositive() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "0");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementNotVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "30");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "300");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementInvalid() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementNegative() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "-2");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementNotVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "30");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "300");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementInvalid() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementNegative() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "-2");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementNotVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "10");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "85");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementInvalid() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementNegative() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "-2");
		
		verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementNotVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "10");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementVerified() { // measurement comes from cache
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now());
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(pingMeasurementCache.get(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "85");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()));
		Assert.assertTrue(verified);
	}
}
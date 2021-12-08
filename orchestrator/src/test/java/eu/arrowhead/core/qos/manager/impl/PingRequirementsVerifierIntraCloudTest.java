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

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;

@RunWith(SpringRunner.class)
public class PingRequirementsVerifierIntraCloudTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private PingRequirementsVerifier verifier;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private Map<Long,QoSIntraPingMeasurementResponseDTO> intraPingMeasurementCache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(verifier, "pingMeasurementCacheThreshold", 120);
		ReflectionTestUtils.setField(verifier, "intraPingMeasurementCache", intraPingMeasurementCache);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterNull() {
		verifier.verify(null, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterProviderNull() {
		verifier.verify(new QoSVerificationParameters(null, null, false, new HashMap<>(),  new HashMap<>(),  new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterQosRequirementsNull() {
		final boolean verified = verifier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), null, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyParameterQosRequirementsEmpty() { // answer true
		final boolean verified = verifier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), new HashMap<>(),  new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyParameterQosRequirementsIrrelevant() { // answer true
		final boolean verified = verifier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), Map.of("IRRELEVANT", "12"),  new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testVerifyParameterInvalidSystemId() {
		when(intraPingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getIntraPingMeasurement(anyLong())).thenThrow(new BadPayloadException("test"));
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(-1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testVerifyObsoleteCache() {
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().minusHours(2)));
		response.setMeasurement(measurement);
		when(intraPingMeasurementCache.get(anyLong())).thenReturn(response);
		when(orchestratorDriver.getIntraPingMeasurement(anyLong())).thenThrow(new ArrowheadException("just for finish the method execution"));
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultTrue() {
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		Assert.assertFalse(response.hasRecord());
		when(intraPingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getIntraPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultFalse() {
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", false);
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		Assert.assertFalse(response.hasRecord());
		when(intraPingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getIntraPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifySystemNotAvailable() {
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		response.setId(12L);
		response.setAvailable(false);
		Assert.assertTrue(response.hasRecord());
		when(intraPingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getIntraPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementInvalid() { // also tests that cache is stored the measurement
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(intraPingMeasurementCache.get(anyLong())).thenReturn(null);
		when(intraPingMeasurementCache.put(anyLong(), any())).thenReturn(response);
		when(orchestratorDriver.getIntraPingMeasurement(anyLong())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "invalid");
		
		try {
			verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		} catch (final InvalidParameterException ex) { // catch exception to test the caching
			verify(intraPingMeasurementCache, times(1)).put(any(), any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementNotPositive() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "0");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "30");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "300");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementInvalid() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementNotPositive() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "0");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "30");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "300");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementInvalid() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementNegative() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "-2");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "30");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "300");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementInvalid() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementNegative() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "-2");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementNotVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "10");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "85");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementInvalid() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "invalid");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementNegative() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "-2");
		
		verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementNotVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "10");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementVerified() { // measurement comes from qos monitor
		final QoSIntraPingMeasurementResponseDTO response = new QoSIntraPingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getIntraPingMeasurement(2L)).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "85");
		
		final boolean verified = verifier.verify(new QoSVerificationParameters(provider, null, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>()), false);
		Assert.assertTrue(verified);
	}
}
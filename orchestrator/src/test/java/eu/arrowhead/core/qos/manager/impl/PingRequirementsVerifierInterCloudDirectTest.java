package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.any;
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
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;

@RunWith(SpringRunner.class)
public class PingRequirementsVerifierInterCloudDirectTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private PingRequirementsVerifier verifier;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private Map<String,QoSInterDirectPingMeasurementResponseDTO> interDirectPingMeasurementCache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(verifier, "pingMeasurementCacheThreshold", 120);
		ReflectionTestUtils.setField(verifier, "interDirectPingMeasurementCache", interDirectPingMeasurementCache);
	}
	
	//verify input parameter tests are already done in PingRequirementsVerifierIntraCloudTest
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testVerifyInvalidCloudSystemForm() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(-2); //Invalid id
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenThrow(new BadPayloadException("test"));
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testVerifyObsoleteCahce() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().minusHours(2)));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setAvailable(true);
		when(interDirectPingMeasurementCache.get(any())).thenReturn(response);
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenThrow(new ArrowheadException("just for finish the method execution"));
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultTrue() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		Assert.assertFalse(response.hasRecord());
		when(interDirectPingMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultFalse() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", false);
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		Assert.assertFalse(response.hasRecord());
		when(interDirectPingMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyObsoleteProviderNotAvailable() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setAvailable(false);
		when(interDirectPingMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementInvalid() { // also tests that cache is stored the measurement
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(interDirectPingMeasurementCache.get(any())).thenReturn(null);
		when(interDirectPingMeasurementCache.put(any(), any())).thenReturn(response);
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		try {
			verifier.verify(parameters, false);
		} catch (final InvalidParameterException ex) { // catch exception to test the caching
			verify(interDirectPingMeasurementCache, times(1)).put(any(), any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementNotPositive() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "0");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "30");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMaxResponseTime(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "32");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementInvalid() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementNotPositive() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "0");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "30");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setMeanResponseTimeWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "32");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementInvalid() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementNegative() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "-2");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "30");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setJitterWithoutTimeout(32);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "33");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementInvalid() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementNegative() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "-2");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementNotVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "30");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceived(20);
		response.setSent(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "85");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementInvalid() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementNegative() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "-2");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementNotVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "30");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementVerified() { // measurement comes from qos monitor
		final QoSInterDirectPingMeasurementResponseDTO response = new QoSInterDirectPingMeasurementResponseDTO();
		final QoSInterDirectMeasurementResponseDTO measurement = new QoSInterDirectMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		response.setMeasurement(measurement);
		response.setId(12L);
		response.setAvailable(true);
		response.setReceivedAll(20);
		response.setSentAll(100);
		Assert.assertTrue(response.hasRecord());
		when(orchestratorDriver.getInterDirectPingMeasurement(any())).thenReturn(response);
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "85");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, false, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		
		verifier.verify(parameters, false);
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
}

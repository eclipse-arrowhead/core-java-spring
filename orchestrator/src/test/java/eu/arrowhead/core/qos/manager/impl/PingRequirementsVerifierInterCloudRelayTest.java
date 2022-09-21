package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;

@RunWith(SpringRunner.class)
public class PingRequirementsVerifierInterCloudRelayTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private PingRequirementsVerifier verifier;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private Map<String,QoSInterRelayEchoMeasurementListResponseDTO> interRelayEchoMeasurementCache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(verifier, "pingMeasurementCacheThreshold", 120);
		ReflectionTestUtils.setField(verifier, "interRelayEchoMeasurementCache", interRelayEchoMeasurementCache);
	}
	
	//verify input parameter tests are already done in PingRequirementsVerifierIntraCloudTest
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testVerifyInvalidCloudRequest() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setOperator("  "); //Invalid operator
		cloud.setName("  "); //Invalid name
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenThrow(new BadPayloadException("test"));
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyGatewayIsMandatorFlagAdjustedWhileHaveNoLocalReferenceMeasurement() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(null);
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());		
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyGatewayIsMandatorFlagAdjustedWhileHaveNoTargetCloudMeasurement() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(null);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testVerifyObsoleteCahce() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setLastMeasurementAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().minusHours(2)));
		response.setMeasurement(measurement);
		response.setId(1L);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1));
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenThrow(new ArrowheadException("just for finish the method execution"));
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoRequirementsDefined() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		final CloudWithRelaysResponseDTO cloudWithRelays = new CloudWithRelaysResponseDTO();
		cloudWithRelays.setId(cloud.getId());
		cloudWithRelays.setOperator(cloud.getOperator());
		cloudWithRelays.setName(cloud.getName());
		cloudWithRelays.setGatewayRelays(List.of(new RelayResponseDTO()));
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(), 0));
		when(orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays(any(), any())).thenReturn(cloudWithRelays);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
		Assert.assertEquals(cloudWithRelays.getGatewayRelays().size(), parameters.getVerifiedRelays().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoRequirementsDefinedAndHaveNoRelaysAtAll() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		final CloudWithRelaysResponseDTO cloudWithRelays = new CloudWithRelaysResponseDTO();
		cloudWithRelays.setId(cloud.getId());
		cloudWithRelays.setOperator(cloud.getOperator());
		cloudWithRelays.setName(cloud.getName());
		cloudWithRelays.setGatewayRelays(List.of());
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(), 0));
		when(orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays(any(), any())).thenReturn(cloudWithRelays);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
		Assert.assertTrue(parameters.getVerifiedRelays().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultTrue() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		final CloudWithRelaysResponseDTO cloudWithRelays = new CloudWithRelaysResponseDTO();
		cloudWithRelays.setId(cloud.getId());
		cloudWithRelays.setOperator(cloud.getOperator());
		cloudWithRelays.setName(cloud.getName());
		cloudWithRelays.setGatewayRelays(List.of(new RelayResponseDTO()));
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(), 0));
		when(orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays(any(), any())).thenReturn(cloudWithRelays);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultTrueButHaveNoRelaysAtAll() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		final CloudWithRelaysResponseDTO cloudWithRelays = new CloudWithRelaysResponseDTO();
		cloudWithRelays.setId(cloud.getId());
		cloudWithRelays.setOperator(cloud.getOperator());
		cloudWithRelays.setName(cloud.getName());
		cloudWithRelays.setGatewayRelays(List.of());
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(), 0));
		when(orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays(any(), any())).thenReturn(cloudWithRelays);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoMeasurementRecordUsingDefaultFalse() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", false);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(), 0));
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyObsoleteProviderNotAvailable() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(false);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		response.setMeasurement(measurement);
		response.setId(1L);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1));
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementInvalid() { // also tests that cache is stored the measurement
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "Invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(interRelayEchoMeasurementCache.put(any(), any())).thenReturn(responseList);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		try {
			verifier.verify(parameters, false);
		} catch (final InvalidParameterException ex) { // catch exception to test the caching
			verify(interRelayEchoMeasurementCache, times(1)).put(any(), any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyMaximumThresholdResponseRequirementNotPositive() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "0");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final int providerTestValue = 10;
		final int relayTestValue = 12;
		final int referenceTestValue = 10;
		final String requirementTestValue = "30";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setMaxResponseTime(referenceTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setMaxResponseTime(providerTestValue);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setMaxResponseTime(relayTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyMaximumThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final int providerTestValue = 10;
		final int relayTestValue = 12;
		final int referenceTestValue = 10;
		final String requirementTestValue = "32";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setMaxResponseTime(referenceTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setMaxResponseTime(providerTestValue);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setMaxResponseTime(relayTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementInvalid() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "Invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyAverageThresholdResponseRequirementNotPositive() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, "0");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final int providerTestValue = 10;
		final int relayTestValue = 12;
		final int referenceTestValue = 10;
		final String requirementTestValue = "30";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setMeanResponseTimeWithoutTimeout(referenceTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setMeanResponseTimeWithoutTimeout(providerTestValue);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setMeanResponseTimeWithoutTimeout(relayTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAverageThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final int providerTestValue = 10;
		final int relayTestValue = 12;
		final int referenceTestValue = 10;
		final String requirementTestValue = "32";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setMeanResponseTimeWithoutTimeout(referenceTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setMeanResponseTimeWithoutTimeout(providerTestValue);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setMeanResponseTimeWithoutTimeout(relayTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementInvalid() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "Invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyJitterThresholdResponseRequirementNegative() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, "-2");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementNotVerified() { // measurement comes from qos monitor
		final int providerTestValue = 10;
		final int relayTestValue = 12;
		final int referenceTestValue = 10;
		final String requirementTestValue = "30";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setJitterWithoutTimeout(referenceTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setJitterWithoutTimeout(providerTestValue);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setJitterWithoutTimeout(relayTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyJitterThresholdResponseRequirementVerified() { // measurement comes from qos monitor
		final int providerTestValue = 10;
		final int relayTestValue = 12;
		final int referenceTestValue = 10;
		final String requirementTestValue = "32";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_JITTER_THRESHOLD, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setJitterWithoutTimeout(referenceTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setJitterWithoutTimeout(providerTestValue);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setJitterWithoutTimeout(relayTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementInvalid() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "Invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyRecentPacketLossRequirementNegative() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, "-2");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementNotVerified() { // measurement comes from qos monitor
		final int providerReceivedTestValue = 5;
		final int providerSentTestValue = 30;
		final int relayReceivedTestValue = 10;
		final int relaySentTestValue = 40;
		final int referenceReceivedTestValue = 5;
		final int referenceSentTestValue = 30;
		final String requirementTestValue = "30";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setReceived(referenceReceivedTestValue);
		referenceMeasurement.setSent(referenceSentTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setReceived(providerReceivedTestValue);
		providerMeasurement.setSent(providerSentTestValue);		
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setReceived(relayReceivedTestValue);
		response.setSent(relaySentTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyRecentPacketLossRequirementVerified() { // measurement comes from qos monitor
		final int providerReceivedTestValue = 5;
		final int providerSentTestValue = 30;
		final int relayReceivedTestValue = 10;
		final int relaySentTestValue = 40;
		final int referenceReceivedTestValue = 5;
		final int referenceSentTestValue = 30;
		final String requirementTestValue = "85";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setReceived(referenceReceivedTestValue);
		referenceMeasurement.setSent(referenceSentTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setReceived(providerReceivedTestValue);
		providerMeasurement.setSent(providerSentTestValue);		
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setReceived(relayReceivedTestValue);
		response.setSent(relaySentTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementInvalid() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "Invalid");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testVerifyPacketLossRequirementNegative() { // measurement comes from qos monitor
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, "-2");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		verifier.verify(parameters, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementNotVerified() { // measurement comes from qos monitor
		final int providerReceivedTestValue = 5;
		final int providerSentTestValue = 30;
		final int relayReceivedTestValue = 10;
		final int relaySentTestValue = 40;
		final int referenceReceivedTestValue = 5;
		final int referenceSentTestValue = 30;
		final String requirementTestValue = "30";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setReceivedAll(referenceReceivedTestValue);
		referenceMeasurement.setSentAll(referenceSentTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setReceivedAll(providerReceivedTestValue);
		providerMeasurement.setSentAll(providerSentTestValue);		
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setReceivedAll(relayReceivedTestValue);
		response.setSentAll(relaySentTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyPacketLossRequirementVerified() { // measurement comes from qos monitor
		final int providerReceivedTestValue = 5;
		final int providerSentTestValue = 30;
		final int relayReceivedTestValue = 10;
		final int relaySentTestValue = 40;
		final int referenceReceivedTestValue = 5;
		final int referenceSentTestValue = 30;
		final String requirementTestValue = "85";
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		provider.setSystemName("test-sys");
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(1);
		cloud.setOperator("test-op");
		cloud.setName("test-n");
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS, requirementTestValue);
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		final QoSIntraPingMeasurementResponseDTO referenceMeasurement = new QoSIntraPingMeasurementResponseDTO();
		referenceMeasurement.setReceivedAll(referenceReceivedTestValue);
		referenceMeasurement.setSentAll(referenceSentTestValue);
		parameters.setLocalReferencePingMeasurement(referenceMeasurement);
		final QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
		providerMeasurement.setProviderAvailable(true);
		providerMeasurement.setReceivedAll(providerReceivedTestValue);
		providerMeasurement.setSentAll(providerSentTestValue);		
		parameters.setProviderTargetCloudMeasurement(providerMeasurement);
		
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setRelay(new RelayResponseDTO(5L, "10.10.10.10", 10000, null, true, false, RelayType.GENERAL_RELAY, null, null));
		response.setMeasurement(measurement);
		response.setId(1L);
		response.setReceivedAll(relayReceivedTestValue);
		response.setSentAll(relaySentTestValue);
		final QoSInterRelayEchoMeasurementListResponseDTO responseList = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(responseList);
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
	}
}

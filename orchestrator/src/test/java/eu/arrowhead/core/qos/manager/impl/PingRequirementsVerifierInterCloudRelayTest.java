package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.any;
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

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
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
	public void testVerifyInvalidCloudSystemForm() {
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		final CloudResponseDTO cloud = new CloudResponseDTO();
		cloud.setId(-2); //Invalid id
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
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		final QoSVerificationParameters parameters = new QoSVerificationParameters(provider, cloud, true, new HashMap<>(), qosRequirements, new HashMap<>(), new ArrayList<>());
		parameters.setLocalReferencePingMeasurement(new QoSIntraPingMeasurementResponseDTO());
		parameters.setProviderTargetCloudMeasurement(new QoSMeasurementAttributesFormDTO());
		
		final QoSInterRelayEchoMeasurementResponseDTO response = new QoSInterRelayEchoMeasurementResponseDTO();
		final QoSInterRelayMeasurementResponseDTO measurement = new QoSInterRelayMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now().minusHours(2));
		response.setMeasurement(measurement);
		response.setId(1L);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(response), 1));
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenThrow(new ArrowheadException("just for finish the method execution"));
		
		verifier.verify(parameters, false);
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
		
		ReflectionTestUtils.setField(verifier, "verifyNotMeasuredSystem", true);
		when(interRelayEchoMeasurementCache.get(any())).thenReturn(null);
		when(orchestratorDriver.getInterRelayEchoMeasurement(any())).thenReturn(new QoSInterRelayEchoMeasurementListResponseDTO(List.of(), 0));
		
		final boolean verified = verifier.verify(parameters, false);
		Assert.assertTrue(verified);
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
		QoSMeasurementAttributesFormDTO providerMeasurement = new QoSMeasurementAttributesFormDTO();
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
}

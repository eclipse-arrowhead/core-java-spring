package eu.arrowhead.core.qos.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;

@RunWith(SpringRunner.class)
public class ServiceTimeVerifierTest {
	
	//=================================================================================================
	// members
	
	private final ServiceTimeVerifier verfier = new ServiceTimeVerifier();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterNull() {
		verfier.verify(null, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterMetadataNull() {
		verfier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, false, null, new HashMap<>(), new HashMap<>(), new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterWarningsNull() {
		verfier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), new HashMap<>(), new HashMap<>(), null), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyParameterCommandsNull() {
		verfier.verify(new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), new HashMap<>(), null, new ArrayList<>()), false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoRecommendedTimeNoExclusivityNoChange() {
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), new HashMap<>(), new HashMap<>(),
																			  List.of(OrchestratorWarnings.TTL_UNKNOWN));
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals(OrchestratorWarnings.TTL_UNKNOWN, param.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIllFormedRecommendedTimeNoExclusivityNoChange() {
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, Map.of(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "not a number"),
																			  new HashMap<>(), new HashMap<>(), warnings);
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals(OrchestratorWarnings.TTL_UNKNOWN, param.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 15, warnings: TTL_EXPIRING
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, new HashMap<>(), new HashMap<>(),
																			  Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"), warnings);
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals("15", param.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(param.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, param.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIllFormedRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 15, warnings: TTL_EXPIRING
		final Map<String, String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "not a number");
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, metadata, new HashMap<>(),
																			  Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"), warnings);
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals("15", param.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(param.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, param.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyTooShortRecommendedTimeWithExclusivityNotVerified() { // answer false
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, Map.of(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "5"),
				  															  new HashMap<>(), Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"), warnings);
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyEnoughRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 15, warnings: TTL_EXPIRING
		final Map<String, String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "14"); // extra seconds does not count
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, metadata, new HashMap<>(),
																			  Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"), warnings);
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals("15", param.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(param.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, param.getWarnings().get(0));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAlmostEnoughRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 16, warnings: TTL_EXPIRING
		final Map<String, String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "10"); // extra seconds does not count
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, metadata, new HashMap<>(),
																			  Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "11"), warnings);
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals("16", param.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(param.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, param.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyEnoughRecommendedTimeWithExclusivityVerifiedNoWarning() { // answer true, calculated time: 126, warnings: none
		final Map<String, String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "240");
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.TTL_UNKNOWN);
		final QoSVerificationParameters param = new QoSVerificationParameters(new SystemResponseDTO(), null, false, metadata, new HashMap<>(),
																			  Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "121"), warnings);
		
		final boolean verified = verfier.verify(param, false);
		Assert.assertTrue(verified);
		Assert.assertEquals("126", param.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(param.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(0, param.getWarnings().size());
	}
}
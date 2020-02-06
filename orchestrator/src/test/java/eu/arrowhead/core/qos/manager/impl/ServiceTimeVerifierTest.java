package eu.arrowhead.core.qos.manager.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
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
	public void testVerifyResultNull() {
		verfier.verify(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyResultMetadataNull() {
		verfier.verify(new OrchestrationResultDTO(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyResultWarningsNull() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setMetadata(Map.of());
		result.setWarnings(null);
		verfier.verify(result, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyResultCommandsNull() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setMetadata(Map.of());
		verfier.verify(result, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoRecommendedTimeNoExclusivityNoChange() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setMetadata(Map.of());
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of());
		Assert.assertTrue(verified);
		Assert.assertEquals(OrchestratorWarnings.TTL_UNKNOWN, result.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIllFormedRecommendedTimeNoExclusivityNoChange() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setMetadata(Map.of(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "not a number"));
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of());
		Assert.assertTrue(verified);
		Assert.assertEquals(OrchestratorWarnings.TTL_UNKNOWN, result.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyNoRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 15, warnings: TTL_EXPIRING
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final Map<String,String> metadata = new HashMap<>();
		result.setMetadata(metadata);
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"));
		Assert.assertTrue(verified);
		Assert.assertEquals("15", result.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(result.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, result.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIllFormedRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 15, warnings: TTL_EXPIRING
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final Map<String,String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "not a number");
		result.setMetadata(metadata);
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"));
		Assert.assertTrue(verified);
		Assert.assertEquals("15", result.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(result.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, result.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyTooShortRecommendedTimeWithExclusivityNotVerified() { // answer false
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final Map<String,String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "5");
		result.setMetadata(metadata);
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"));
		Assert.assertFalse(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyEnoughRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 15, warnings: TTL_EXPIRING
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final Map<String,String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "14"); // extra seconds does not count
		result.setMetadata(metadata);
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10"));
		Assert.assertTrue(verified);
		Assert.assertEquals("15", result.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(result.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, result.getWarnings().get(0));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyAlmostEnoughRecommendedTimeWithExclusivityVerified() { // answer true, calculated time: 16, warnings: TTL_EXPIRING
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final Map<String,String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "10"); // extra seconds does not count
		result.setMetadata(metadata);
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "11"));
		Assert.assertTrue(verified);
		Assert.assertEquals("16", result.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(result.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(OrchestratorWarnings.TTL_EXPIRING, result.getWarnings().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyEnoughRecommendedTimeWithExclusivityVerifiedNoWarning() { // answer true, calculated time: 126, warnings: none
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final Map<String,String> metadata = new HashMap<>();
		metadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "240"); 
		result.setMetadata(metadata);
		result.getWarnings().add(OrchestratorWarnings.TTL_UNKNOWN);
		
		final boolean verified = verfier.verify(result, null, Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "121"));
		Assert.assertTrue(verified);
		Assert.assertEquals("126", result.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
		Assert.assertTrue(result.getWarnings().indexOf(OrchestratorWarnings.TTL_UNKNOWN) < 0);
		Assert.assertEquals(0, result.getWarnings().size());
	}
}
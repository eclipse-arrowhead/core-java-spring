package eu.arrowhead.core.orchestrator.service;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;

@RunWith(SpringRunner.class)
public class OrchestratorUtilsTest {
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateServiceTimeOkWithRecommendedTime() {
		final Map<String, String> serviceMetadata = new HashMap<>();
		serviceMetadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "40");
		final Map<String, String> orchestrationCommands = new HashMap<>();
		orchestrationCommands.put(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "25");
		
		int result = OrchestratorUtils.calculateServiceTime(serviceMetadata, orchestrationCommands);
		assertEquals(orchestrationCommands.get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY), String.valueOf(result));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateServiceTimeOkWithoutRecommendedTime() {
		final Map<String, String> serviceMetadata = new HashMap<>();
		final Map<String, String> orchestrationCommands = new HashMap<>();
		orchestrationCommands.put(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "25");
		
		int result = OrchestratorUtils.calculateServiceTime(serviceMetadata, orchestrationCommands);
		assertEquals(orchestrationCommands.get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY), String.valueOf(result));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateServiceTimeInvalidRecommendedTime() {
		final Map<String, String> serviceMetadata = new HashMap<>();
		serviceMetadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "invalid");
		final Map<String, String> orchestrationCommands = new HashMap<>();
		orchestrationCommands.put(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "25");
		
		int result = OrchestratorUtils.calculateServiceTime(serviceMetadata, orchestrationCommands);
		assertEquals(orchestrationCommands.get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY), String.valueOf(result));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateServiceTimeInvalidRecommendedTimeLessThanRequested() {
		final Map<String, String> serviceMetadata = new HashMap<>();
		serviceMetadata.put(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME, "10");
		final Map<String, String> orchestrationCommands = new HashMap<>();
		orchestrationCommands.put(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "25");
		
		int result = OrchestratorUtils.calculateServiceTime(serviceMetadata, orchestrationCommands);
		assertEquals(0, result);
	}
}

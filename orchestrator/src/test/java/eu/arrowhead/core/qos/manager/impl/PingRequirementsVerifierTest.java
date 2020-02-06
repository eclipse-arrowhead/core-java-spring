package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
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
	private Map<Long,PingMeasurementResponseDTO> pingMeasurementCache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyResultNull() {
		verifier.verify(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyResultProviderNull() {
		verifier.verify(new OrchestrationResultDTO(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyQosRequirementsNull() { // answer true
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setProvider(new SystemResponseDTO());
		
		final boolean verified = verifier.verify(result, null, null);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyQosRequirementsEmpty() { // answer true
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setProvider(new SystemResponseDTO());
		
		final boolean verified = verifier.verify(result, Map.of(), null);
		Assert.assertTrue(verified);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testVerifyInvalidSystemId() {
		when(pingMeasurementCache.get(anyLong())).thenReturn(null);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenThrow(new BadPayloadException("test"));
		
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(-1);
		result.setProvider(provider);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		verifier.verify(result, qosRequirements, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testVerifyObsoleteCache() {
		final PingMeasurementResponseDTO response = new PingMeasurementResponseDTO();
		final QoSIntraMeasurementResponseDTO measurement = new QoSIntraMeasurementResponseDTO();
		measurement.setLastMeasurementAt(ZonedDateTime.now().minusHours(2));
		response.setMeasurement(measurement);
		when(pingMeasurementCache.get(anyLong())).thenReturn(response);
		when(orchestratorDriver.getPingMeasurement(anyLong())).thenThrow(new ArrowheadException("just for finish the method execution"));
		
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(2);
		result.setProvider(provider);
		final Map<String,String> qosRequirements = new HashMap<>();
		qosRequirements.put(OrchestrationFormRequestDTO.QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD, "500");
		
		verifier.verify(result, qosRequirements, null);
	}
}
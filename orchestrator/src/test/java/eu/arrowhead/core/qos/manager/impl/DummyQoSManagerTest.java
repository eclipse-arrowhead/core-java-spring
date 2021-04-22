package eu.arrowhead.core.qos.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@RunWith(SpringRunner.class)
public class DummyQoSManagerTest {

	//=================================================================================================
	// members
	
	private final DummyQoSManager qosManager = new DummyQoSManager();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterReservedProvidersDoNothing() {
		final List<OrchestrationResultDTO> filtered = qosManager.filterReservedProviders(getTestOrchestrationResults(), null);
		Assert.assertEquals(3, filtered.size());
		for (int i = 0; i < 3; ++i) {
			Assert.assertEquals(i, filtered.get(i).getProvider().getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReserveProvidersTemporarilyDoNothing() {
		final List<OrchestrationResultDTO> reserved = qosManager.reserveProvidersTemporarily(getTestOrchestrationResults(), null);
		Assert.assertEquals(3, reserved.size());
		for (int i = 0; i < 3; ++i) {
			Assert.assertEquals(i, reserved.get(i).getProvider().getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIntraCloudServicesDoNothing() {
		final List<OrchestrationResultDTO> verified = qosManager.verifyIntraCloudServices(getTestOrchestrationResults(), null);
		Assert.assertEquals(3, verified.size());
		for (int i = 0; i < 3; ++i) {
			Assert.assertEquals(i, verified.get(i).getProvider().getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPreVerifyInterCloudServicesDoNothing() {
		final List<GSDPollResponseDTO> verified = qosManager.preVerifyInterCloudServices(getTestGSDResponses(), null);
		Assert.assertEquals(3, verified.size());
		for (int i = 0; i < 3; ++i) {
			Assert.assertEquals(i, verified.get(i).getProviderCloud().getId());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyInterCloudServicesDoNothing() {
		final List<OrchestrationResultDTO> verified = qosManager.verifyInterCloudServices(null, getTestOrchestrationResults(), null, null);
		Assert.assertEquals(3, verified.size());
		for (int i = 0; i < 3; ++i) {
			Assert.assertEquals(i, verified.get(i).getProvider().getId());
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestrationResultDTO> getTestOrchestrationResults() {
		final List<OrchestrationResultDTO> result = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			final OrchestrationResultDTO dto = new OrchestrationResultDTO();
			final SystemResponseDTO system = new SystemResponseDTO(i, "System" + i, "localhost", 3000 + i, null, null, null, null);
			dto.setProvider(system);
			result.add(dto);
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<GSDPollResponseDTO> getTestGSDResponses() {
		final List<GSDPollResponseDTO> response = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			GSDPollResponseDTO dto = new GSDPollResponseDTO();
			dto.setProviderCloud(new CloudResponseDTO(i, "test-op", "test-n", true, true, false, "sdfhbs", null, null));
			response.add(dto);
		}
		return response;
	}

}
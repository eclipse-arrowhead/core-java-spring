package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@RunWith(SpringRunner.class)
public class RandomIntraCloudProviderMatchmakerTest {

	//=================================================================================================
	// members
	
	private IntraCloudProviderMatchmakingAlgorithm algorithm = new RandomIntraCloudProviderMatchmaker();
	private Random rng = new Random();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void doMatchmakingSRListNull() {
		algorithm.doMatchmaking(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void doMatchmakingSRListEmpty() {
		algorithm.doMatchmaking(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void doMatchmakingParamsNull() {
		algorithm.doMatchmaking(List.of(new ServiceRegistryResponseDTO()), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void doMatchmakingNoPreferred() {
		final ServiceRegistryResponseDTO dto1 = new ServiceRegistryResponseDTO();
		dto1.setId(1);
		final ServiceRegistryResponseDTO dto2 = new ServiceRegistryResponseDTO();
		dto2.setId(2);
		
		final long seed = System.currentTimeMillis();
		IntraCloudProviderMatchmakingParameters params = new IntraCloudProviderMatchmakingParameters(List.of());
		params.setRandomSeed(seed);
		rng.setSeed(seed);
		algorithm = new RandomIntraCloudProviderMatchmaker(); // to make sure the same seed is set
		
		final List<ServiceRegistryResponseDTO> srList = List.of(dto1, dto2);
		final ServiceRegistryResponseDTO selected = algorithm.doMatchmaking(srList, params);
		
		Assert.assertEquals(srList.get(rng.nextInt(2)).getId(), selected.getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void doMatchmakingNoPreferredMatch() {
		final SystemResponseDTO system1 = new SystemResponseDTO(1, "system1", "localhost", 1234, null, null, null);
		final SystemResponseDTO system2 = new SystemResponseDTO(1, "system2", "localhost", 4567, null, null, null);
		final ServiceRegistryResponseDTO dto1 = new ServiceRegistryResponseDTO();
		dto1.setId(1);
		dto1.setProvider(system1);
		final ServiceRegistryResponseDTO dto2 = new ServiceRegistryResponseDTO();
		dto2.setId(2);
		dto2.setProvider(system2);
		
		final SystemRequestDTO reqSystem = new SystemRequestDTO();
		reqSystem.setSystemName("other");
		reqSystem.setAddress("127.0.0.1");
		reqSystem.setPort(1111);
		final PreferredProviderDataDTO ppDTO = new PreferredProviderDataDTO();
		ppDTO.setProviderSystem(reqSystem);
		
		final long seed = System.currentTimeMillis();
		IntraCloudProviderMatchmakingParameters params = new IntraCloudProviderMatchmakingParameters(List.of());
		params.setRandomSeed(seed);
		rng.setSeed(seed);
		algorithm = new RandomIntraCloudProviderMatchmaker(); // to make sure the same seed is set
		
		final List<ServiceRegistryResponseDTO> srList = List.of(dto1, dto2);
		final ServiceRegistryResponseDTO selected = algorithm.doMatchmaking(srList, params);
		
		Assert.assertEquals(srList.get(rng.nextInt(2)).getId(), selected.getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void doMatchmakingPreferredMatch() {
		final SystemResponseDTO system1 = new SystemResponseDTO(1, "system1", "localhost", 1234, null, null, null);
		final SystemResponseDTO system2 = new SystemResponseDTO(1, "system2", "localhost", 4567, null, null, null);
		final ServiceRegistryResponseDTO dto1 = new ServiceRegistryResponseDTO();
		dto1.setId(1);
		dto1.setProvider(system1);
		final ServiceRegistryResponseDTO dto2 = new ServiceRegistryResponseDTO();
		dto2.setId(2);
		dto2.setProvider(system2);
		
		final SystemRequestDTO reqSystem = new SystemRequestDTO();
		reqSystem.setSystemName("System2");
		reqSystem.setAddress("localhost   ");
		reqSystem.setPort(4567);
		final PreferredProviderDataDTO ppDTO = new PreferredProviderDataDTO();
		ppDTO.setProviderSystem(reqSystem);
		
		final ServiceRegistryResponseDTO selected = algorithm.doMatchmaking(List.of(dto1, dto2), new IntraCloudProviderMatchmakingParameters(List.of(ppDTO)));
		
		Assert.assertEquals(2, selected.getId());
	}
}
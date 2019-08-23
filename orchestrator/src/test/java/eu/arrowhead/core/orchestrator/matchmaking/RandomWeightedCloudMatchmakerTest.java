package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;

@RunWith(SpringRunner.class)
public class RandomWeightedCloudMatchmakerTest {

	//=================================================================================================
	// members
	
	private final CloudMatchmakingAlgorithm algorithm = new RandomWeightedCloudMatchmaker();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingMoreWeightResultsMoreMatching() {		
		
		final boolean onlyPreferred = false;
		final List<CloudRequestDTO> preferredClouds = List.of();

		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(
				1L, 
				"operator1", 
				"cloudname1", 
				false, 
				true, 
				false, 
				null, 
				null, 
				null);
		final int numOfProviders10 = 10;		
		final GSDPollResponseDTO gsdPollResponseDTO1 = new GSDPollResponseDTO(
				providerCloud1,
				"requiredServiceDefinition",
				null,
				numOfProviders10,
				null);
		
		final CloudResponseDTO providerCloud2 = new CloudResponseDTO(
				1L, 
				"operator2", 
				"cloudname2", 
				false, 
				true, 
				false, 
				null, 
				null, 
				null);
		final int numOfProviders1 = 1;		
		final GSDPollResponseDTO gsdPollResponseDTO2 = new GSDPollResponseDTO(
				providerCloud2,
				"requiredServiceDefinition",
				null,
				numOfProviders1,
				null);
		final List<GSDPollResponseDTO> gsdPollResponseDTOList = List.of(gsdPollResponseDTO1, gsdPollResponseDTO2);
		final int unsuccessfulRequests = 0;
		final GSDQueryResultDTO gsdQueryResultDTO = new GSDQueryResultDTO(
				gsdPollResponseDTOList, 
				unsuccessfulRequests);
		
		final CloudMatchmakingParameters params = new CloudMatchmakingParameters(
				gsdQueryResultDTO, 
				preferredClouds, 
				onlyPreferred);	
		
		int provider1 = 0;
		int provider2 = 0;
		for (int i = 0; i < 100; i++) {
			
			final CloudResponseDTO cloudResponseDTO = algorithm.doMatchmaking(params);
			
			if (cloudResponseDTO.getName().equalsIgnoreCase(providerCloud1.getName())) {
				
				++provider1;
			}else {
				++provider2;
			}
		}
		
		Assert.assertTrue( provider1 > provider2 );

	
	}

}
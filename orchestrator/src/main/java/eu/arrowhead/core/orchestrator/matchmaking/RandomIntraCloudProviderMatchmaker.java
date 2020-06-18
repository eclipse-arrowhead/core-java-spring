package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;

public class RandomIntraCloudProviderMatchmaker implements IntraCloudProviderMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(RandomIntraCloudProviderMatchmaker.class);

	private Random rng;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first (preferred) provider.
	 */
	@Override
	public OrchestrationResultDTO doMatchmaking(final List<OrchestrationResultDTO> orList, final IntraCloudProviderMatchmakingParameters params) {
		logger.debug("RandomIntraCloudProviderMatchmaker.doMatchmaking started...");
		
		Assert.isTrue(orList != null && !orList.isEmpty(), "orList is null or empty.");
		Assert.notNull(params, "params is null");
		
		if (rng == null) {
			rng = new Random(params.getRandomSeed());
		}
		
		if (params.getPreferredLocalProviders().isEmpty()) {
			logger.debug("No preferred provider is specified, a random one in the list is selected.");
			return orList.get(rng.nextInt(orList.size()));
		}
		
		for (final OrchestrationResultDTO orResult : orList) {
			for (final PreferredProviderDataDTO provider : params.getPreferredLocalProviders()) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(orResult.getProvider(), provider.getProviderSystem())) {
					logger.debug("The first preferred provider found in the list is selected.");
					return orResult;
				}
			}
		}
		
		logger.debug("no match was found between preferred providers, a random one is selected.");
		
		return orList.get(rng.nextInt(orList.size()));
	}
}
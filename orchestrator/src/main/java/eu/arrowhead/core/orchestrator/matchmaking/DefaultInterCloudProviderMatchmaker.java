package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.DTOUtilities;
import eu.arrowhead.common.dto.ICNResultDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;

public class DefaultInterCloudProviderMatchmaker implements InterCloudProviderMatchmakingAlgorithm {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DefaultInterCloudProviderMatchmaker.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Override
	public OrchestrationResponseDTO doMatchmaking(final InterCloudProviderMatchmakingParameters params) {
		logger.debug("DefaultInterCloudProviderMatchmaker.doMatchmaking started...");
		
		Assert.notNull(params, "params is null");
		
		final ICNResultDTO icnResultDTO = params.getIcnResultDTO();
		final List<PreferredProviderDataDTO> preferredProviderDataDTOList = params.getPreferredGlobalProviders();
		final boolean storeOrchestration = params.isStoreOrchestration();
		
		if (icnResultDTO == null || icnResultDTO.getResponse().isEmpty()) {
			// returning empty response
			return new OrchestrationResponseDTO();
		}
		
		for (final OrchestrationResultDTO orchestratorResultDTO : icnResultDTO.getResponse()) {
			for (final PreferredProviderDataDTO provider :  preferredProviderDataDTOList) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(orchestratorResultDTO.getProvider(), provider.getProviderSystem())) {
					logger.debug("The first preferred provider found in icnResponse is selected.");
					return new OrchestrationResponseDTO(List.of(orchestratorResultDTO));
				}
			}
		}
		
		if (storeOrchestration) {
			// returning empty response
			return new OrchestrationResponseDTO();
		}
		
		logger.debug("no match was found between preferred providers, the first one is selected.");
		return  new OrchestrationResponseDTO(List.of(icnResultDTO.getResponse().get(0)));
	}
}
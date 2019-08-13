package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.DTOUtilities;
import eu.arrowhead.common.dto.ICNResponseDTO;
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
	public OrchestrationResponseDTO doMatchmaking(InterCloudProviderMatchmakingParameters params) {
		logger.debug("DefaultInterCloudProviderMatchmaker.doMatchmaking started...");
		
		final ICNResponseDTO icnResponseDTO = params.getIcnResponseDTO();
		final List<PreferredProviderDataDTO> preferredProviderDataDTOList = params.getPreferredGlobalProviders();
		final boolean storeOrchestration = params.isStoreOrchestration();
		
		Assert.isTrue(icnResponseDTO != null && !icnResponseDTO.getResponse().isEmpty(), "icnResponseDTO is null or empty.");
		Assert.notNull(params, "params is null");
		
		for (final OrchestrationResultDTO orchestratorResultDTO : icnResponseDTO.getResponse()) {
			for (PreferredProviderDataDTO provider :  preferredProviderDataDTOList) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(orchestratorResultDTO.getProvider(), provider.getProviderSystem())) {
					logger.debug("The first preferred provider found in icnResponse is selected.");
					return new OrchestrationResponseDTO(List.of(orchestratorResultDTO));
				}
			}
		}
		
		if (storeOrchestration) {
			
			//returning empty response
			return new OrchestrationResponseDTO();
		}
		
		logger.debug("no match was found between preferred providers, the first one is selected.");
		
		return  new OrchestrationResponseDTO(List.of(icnResponseDTO.getResponse().get(0)));
	}

}

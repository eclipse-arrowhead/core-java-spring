package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

public class DefaultIntraCloudProviderMatchmaker implements IntraCloudProviderMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DefaultIntraCloudProviderMatchmaker.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first (preferred) provider.
	 */
	@Override
	public ServiceRegistryResponseDTO doMatchmaking(final List<ServiceRegistryResponseDTO> srList, final IntraCloudProviderMatchmakingParameters params) {
		logger.debug("DefaultIntraCloudProviderMatchmaker.doMatchmaking started...");
		
		Assert.isTrue(srList != null && !srList.isEmpty(), "srList is null or empty.");
		Assert.notNull(params, "params is null");
		
		if (params.getPreferredLocalProviders().isEmpty()) {
			logger.debug("No preferred provider is specified, the first one in the SR list is selected.");
			return srList.get(0);
		}
		
		for (final ServiceRegistryResponseDTO srResult : srList) {
			for (final PreferredProviderDataDTO provider : params.getPreferredLocalProviders()) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(srResult.getProvider(), provider.getProviderSystem())) {
					logger.debug("The first preferred provider found in SR is selected.");
					return srResult;
				}
			}
		}
		
		logger.debug("no match was found between preferred providers, the first one is selected.");
		
		return srList.get(0);
	}
}
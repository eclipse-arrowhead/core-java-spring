package eu.arrowhead.core.gatekeeper;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeper;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@Component
public class GatekeeperApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private CommonDBService commonDBservicre;
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;

	//=================================================================================================
	// assistant methods
		
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");
		
		final Cloud ownCloud = commonDBservicre.getOwnCloud(sslProperties.isSslEnabled());
		final String authorizationInfo = sslProperties.isSslEnabled() ? Base64.getEncoder().encodeToString(publicKey.getEncoded()) : null;
		final CloudGatekeeper gatekeeper = checkIfGatekeeperRegistered(ownCloud);
		if (gatekeeper == null) {
			gatekeeperDBService.registerGatekeeper(ownCloud, coreSystemRegistrationProperties.getCoreSystemAddress()
														   , coreSystemRegistrationProperties.getCoreSystemPort()
														   , CommonConstants.GATEKEEPER_URI
														   , authorizationInfo);
			logger.info("Gatekeeper of own cloud has been registered.");
		} else if (!checkIfRegisteredGatekeeperHasSameProperties(gatekeeper)) {
			gatekeeperDBService.updateGatekeeper(gatekeeper, coreSystemRegistrationProperties.getCoreSystemAddress()
														   , coreSystemRegistrationProperties.getCoreSystemPort()
														   , CommonConstants.GATEKEEPER_URI
														   , authorizationInfo);
			logger.info("Gatekeeper of own cloud has been updated.");
		} else {
			logger.info("Gatekeeper of own cloud was already registered.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudGatekeeper checkIfGatekeeperRegistered(final Cloud ownCloud) {
		try {
			return gatekeeperDBService.getGatekeeperByCloud(ownCloud);			
		} catch (final InvalidParameterException ex) {
			return null;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean checkIfRegisteredGatekeeperHasSameProperties(final CloudGatekeeper gatekeeper) {
		if (publicKey == null) {
			
			return (gatekeeper.getAddress().equalsIgnoreCase(coreSystemRegistrationProperties.getCoreSystemAddress().trim()) 
					&& gatekeeper.getPort() == coreSystemRegistrationProperties.getCoreSystemPort()
					&& gatekeeper.getServiceUri().equalsIgnoreCase(CommonConstants.GATEKEEPER_URI.trim())
					&& gatekeeper.getAuthenticationInfo() == null);
			
		} else if (gatekeeper.getAuthenticationInfo() == null) {
			
			return (gatekeeper.getAddress().equalsIgnoreCase(coreSystemRegistrationProperties.getCoreSystemAddress().trim()) 
					&& gatekeeper.getPort() == coreSystemRegistrationProperties.getCoreSystemPort()
					&& gatekeeper.getServiceUri().equalsIgnoreCase(CommonConstants.GATEKEEPER_URI.trim())
					&& publicKey == null);
			
		} else {
			
			return (gatekeeper.getAddress().equalsIgnoreCase(coreSystemRegistrationProperties.getCoreSystemAddress().trim()) 
					&& gatekeeper.getPort() == coreSystemRegistrationProperties.getCoreSystemPort()
					&& gatekeeper.getServiceUri().equalsIgnoreCase(CommonConstants.GATEKEEPER_URI.trim())
					&& gatekeeper.getAuthenticationInfo().equals(Base64.getEncoder().encodeToString(publicKey.getEncoded())));
		}
	}
}
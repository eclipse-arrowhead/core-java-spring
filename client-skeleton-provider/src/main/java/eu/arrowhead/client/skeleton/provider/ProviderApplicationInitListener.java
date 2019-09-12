package eu.arrowhead.client.skeleton.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import eu.arrowhead.client.skeleton.common.ApplicationInitListener;
import eu.arrowhead.client.skeleton.common.ArrowheadService;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.UnavailableServerException;

@Component
public class ProviderApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	protected final Logger logger = LogManager.getLogger(ProviderApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {

		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICE_REGISTRY);
		checkCoreSystemReachability(CoreSystem.AUTHORIZATION);
		
		//TODO: implement here any custom behavior on application start up
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void destroy() throws InterruptedException {
		//TODO: implement here any custom behavior on application shout down
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkCoreSystemReachability(final CoreSystem coreSystem) {
		try {			
			final ResponseEntity<String> response = arrowheadService.echoCoreSystem(coreSystem);
			
			if (response != null && response.getStatusCode() == HttpStatus.OK) {
				logger.info("'{}' core system is reachable.", coreSystem.name());
			} else {
				logger.info("'{}' core system is NOT reachable.", coreSystem.name());
			}
		} catch (final  UnavailableServerException | AuthException ex) {
			logger.info("'{}' core system is NOT reachable.", coreSystem.name());
		}
	}
}

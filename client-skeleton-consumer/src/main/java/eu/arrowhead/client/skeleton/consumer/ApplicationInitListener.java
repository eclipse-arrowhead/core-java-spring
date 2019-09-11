package eu.arrowhead.client.skeleton.consumer;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import eu.arrowhead.client.skeleton.common.ArrowheadService;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.UnavailableServerException;

@Component
public class ApplicationInitListener {

	//=================================================================================================
	// members
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	private final Logger logger = LogManager.getLogger(ApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	@Order(10)
	public void onApplicationEvent(final ContextRefreshedEvent event) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InterruptedException {
		
		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICE_REGISTRY);
		checkCoreSystemReachability(CoreSystem.ORCHESTRATOR);
		checkCoreSystemReachability(CoreSystem.AUTHORIZATION);
		
		
		//Update and store core service URLs in CoreServiceProperties component
		arrowheadService.updateCoreServiceProperties(CoreSystem.ORCHESTRATOR);
		arrowheadService.updateCoreServiceProperties(CoreSystem.AUTHORIZATION);

		//TODO: implement here any custom behavior on application start up
	}
	
	//-------------------------------------------------------------------------------------------------
	@PreDestroy
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

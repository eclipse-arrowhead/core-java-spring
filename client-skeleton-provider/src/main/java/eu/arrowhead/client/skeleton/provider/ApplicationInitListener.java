package eu.arrowhead.client.skeleton.provider;

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
import eu.arrowhead.common.exception.UnavailableServerException;

@Component
public class ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	protected final Logger logger = LogManager.getLogger(ApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	@Order(10)
	public void onApplicationEvent(final ContextRefreshedEvent event) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InterruptedException {
		
		//Checking the availability of Service Registry Core System
		if (checkServiceRegistryIsAlive()) {
			logger.info("Service Registry is available.");
		} else {
			logger.info("Service Registry is NOT available.");
		}

		//TODO: implement here any custom behavior on application start up
	}
	
	//-------------------------------------------------------------------------------------------------
	@PreDestroy
	public void destroy() throws InterruptedException {
		//TODO: implement here any custom behavior on application shout down
	}

	//=================================================================================================
	// assistant methods
	
	private boolean checkServiceRegistryIsAlive() {
		try {
			final ResponseEntity<String> response = arrowheadService.echoServiceRegistry();
			return response.getStatusCode() == HttpStatus.OK;			
		} catch (final UnavailableServerException ex) {
			return false;
		}
	}
}

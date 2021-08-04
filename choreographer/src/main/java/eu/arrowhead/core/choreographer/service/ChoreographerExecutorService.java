package eu.arrowhead.core.choreographer.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@Service
public class ChoreographerExecutorService {
	
	//=================================================================================================
    // members
	
	@Autowired
	private ChoreographerDriver driver;
	private final Logger logger = LogManager.getLogger(ChoreographerExecutorService.class);
	
	//=================================================================================================
    // methods

	//-------------------------------------------------------------------------------------------------	
	public SystemResponseDTO registerExecutorSystem(final SystemRequestDTO systemRequest) { //TODO junit
		logger.debug("registerExecutorSystem started...");
		Assert.notNull(systemRequest, "SystemRequestDTO is null");
		
		return driver.registerSystem(systemRequest);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public void unregisterExecutorSystem(final String systemName, final String address, final int port) {
		logger.debug("unregisterExecutorSystem started...");
		
		//TODO
	}
}

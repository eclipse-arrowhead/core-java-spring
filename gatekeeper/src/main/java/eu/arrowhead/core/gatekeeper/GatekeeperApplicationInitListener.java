package eu.arrowhead.core.gatekeeper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.database.service.CommonDBService;
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

		//TODO
	}
}
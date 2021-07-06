package eu.arrowhead.core.translator;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.translator.services.fiware.FiwareService;
import eu.arrowhead.core.translator.services.translator.TranslatorService;

@Component
public class TranslatorApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	private TranslatorService translatorService;
        private FiwareService fiwareService;
	
	@Autowired
	private ApplicationContext applicationContext;
        
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	
	//=================================================================================================
	// assistant methods
    //-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");
                translatorService = applicationContext.getBean(TranslatorService.class);
                translatorService.start();
                fiwareService = applicationContext.getBean(FiwareService.class);
                fiwareService.start();

	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {
		logger.debug("customDestroy started...");
                fiwareService.unregisterAll();
	}
    
}
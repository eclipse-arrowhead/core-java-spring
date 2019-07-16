package eu.arrowhead.core.serviceregistry;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;

@Component
public class ServiceRegistryApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		System.out.println("Custom init called.");
	}

}
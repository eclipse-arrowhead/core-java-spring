package eu.arrowhead.core.eventhandler;

import java.util.List;

import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;

@Component
public class EventHandlerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		
		return List.of(CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE);
	}
	
}
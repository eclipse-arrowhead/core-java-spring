package eu.arrowhead.core.authorization;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;

@Component
public class AuthorizationApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$AUTHORIZATION_IS_EVENTHANDLER_PRESENT_WD)
	private boolean eventhandlerIsPresent;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		
		if ( eventhandlerIsPresent ) {
			
			return List.of(CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE);
		
		} else {
			
			return List.of();
		}
		
	}
}
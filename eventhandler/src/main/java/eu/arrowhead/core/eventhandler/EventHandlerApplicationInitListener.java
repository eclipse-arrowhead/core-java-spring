package eu.arrowhead.core.eventhandler;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.eventhandler.metadatafiltering.DefaultMetadataFilter;
import eu.arrowhead.core.eventhandler.metadatafiltering.MetadataFilteringAlgorithm;
import eu.arrowhead.common.CommonConstants;

@Component
public class EventHandlerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	//-------------------------------------------------------------------------------------------------

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CommonConstants.EVENT_METADATA_FILTER)
	public MetadataFilteringAlgorithm getMetadataFilter() {
		return new DefaultMetadataFilter();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		
		return List.of(CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE);
	}
	
}
package eu.arrowhead.core.eventhandler.metadatafiltering;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

public class DefaultMetadataFilter implements MetadataFilteringAlgorithm {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DefaultMetadataFilter.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Override
	public boolean doFiltering( final MetadataFilteringParameters params ) {
		logger.debug("DefaultMetadataFilter.doFiltering started...");
		
		Assert.notNull(params, "params is null");
		Assert.notNull(params.getMetaDataFilterMap(), "params.MetaDataFilterMap is null");
		Assert.notNull(params.getEventMetadata(), "params.EventMetadata is null");
		
		final Map<String, String> metaDataFilterMap = params.getMetaDataFilterMap();
		final Map<String, String> eventMetadata = params.getEventMetadata();
		
		if ( metaDataFilterMap.keySet().containsAll( eventMetadata.keySet() ) ) {
			
			if (metaDataFilterMap.values().containsAll( eventMetadata.values())) {
				
				return true;
			}
			
		}
		
		return false;
	}

}

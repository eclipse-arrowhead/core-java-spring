package eu.arrowhead.core.eventhandler.metadatafiltering;

import java.util.Map;

public class MetadataFilteringParameters {

	//=================================================================================================
	// members
	
	protected Map<String, String> metaDataFilterMap;
	protected Map<String, String> eventMetadata;

	// additional parameter can be add here to provide information to the various filtering algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public MetadataFilteringParameters( final Map<String, String> metaDataFilterMap, final Map<String, String> eventMetadata ) {
		super();
		this.metaDataFilterMap = metaDataFilterMap;
		this.eventMetadata = eventMetadata;
	}

	//-------------------------------------------------------------------------------------------------
	public Map<String, String> getMetaDataFilterMap() {	return metaDataFilterMap; }
	public Map<String, String> getEventMetadata() {	return eventMetadata; }

	//-------------------------------------------------------------------------------------------------
	public void setMetaDataFilterMap(final Map<String, String> metaDataFilterMap) { this.metaDataFilterMap = metaDataFilterMap; }
	public void setEventMetadata(final Map<String, String> eventMetadata) { this.eventMetadata = eventMetadata; }
	
}

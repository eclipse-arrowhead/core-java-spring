/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.eventhandler.metadatafiltering;

import java.util.Map;

public class MetadataFilteringParameters {

	//=================================================================================================
	// members
	
	protected Map<String,String> metaDataFilterMap;
	protected Map<String,String> eventMetadata;

	// additional parameter can be add here to provide information to the various filtering algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public MetadataFilteringParameters(final Map<String,String> metaDataFilterMap, final Map<String,String> eventMetadata) {
		super();
		this.metaDataFilterMap = metaDataFilterMap;
		this.eventMetadata = eventMetadata;
	}

	//-------------------------------------------------------------------------------------------------
	public Map<String,String> getMetaDataFilterMap() {	return metaDataFilterMap; }
	public Map<String,String> getEventMetadata() {	return eventMetadata; }

	//-------------------------------------------------------------------------------------------------
	public void setMetaDataFilterMap(final Map<String,String> metaDataFilterMap) { this.metaDataFilterMap = metaDataFilterMap; }
	public void setEventMetadata(final Map<String,String> eventMetadata) { this.eventMetadata = eventMetadata; }
}
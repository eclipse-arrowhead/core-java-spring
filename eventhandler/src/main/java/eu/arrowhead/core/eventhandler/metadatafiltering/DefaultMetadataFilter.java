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
	public boolean doFiltering(final MetadataFilteringParameters params) {
		logger.debug("DefaultMetadataFilter.doFiltering started...");
		
		Assert.notNull(params, "params is null");
		Assert.notNull(params.getMetaDataFilterMap(), "params.MetaDataFilterMap is null");
		Assert.notNull(params.getEventMetadata(), "params.EventMetadata is null");
		
		final Map<String, String> metaDataFilterMap = params.getMetaDataFilterMap();
		final Map<String, String> eventMetadata = params.getEventMetadata();
		
		for (final String filterKey : metaDataFilterMap.keySet()) {
			if (!metaDataFilterMap.get(filterKey).equalsIgnoreCase(eventMetadata.get(filterKey))) {
				return false;
			}			
		}
		
		return true;
	}
}
/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.choreographer.executor;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

public class WeightedExecutorMeasurementStrategy implements ExecutorMeasurementStrategy {
	
	//=================================================================================================
	// members
	
	private static final int DEPENDENCY_WEIGHT = 1;
	private static final int NON_LOCAL_DEPENDENCY_WEIGHT = 20;
	private static final int MAX_VALUE_FOR_SERVICE_RARITY = 100;
	private static final int WORST_VALUE = Integer.MAX_VALUE;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public int getMeasurement(final Map<Integer,List<String>> dependencyServiceData) {
		Assert.notNull(dependencyServiceData, "input is null.");
		
		int result = DEPENDENCY_WEIGHT * dependencyServiceData.size();
		
		int minCloudListSize = Integer.MAX_VALUE;
		for (final List<String> cloudList : dependencyServiceData.values()) {
			if (Utilities.isEmpty(cloudList)) { // means a dependency service has no provider at all
				return WORST_VALUE; 
			}
			
			if (isNonLocal(cloudList)) {
				result += NON_LOCAL_DEPENDENCY_WEIGHT;
				
				if (cloudList.size() < minCloudListSize) {
					minCloudListSize = cloudList.size();
				}
			}
		}
		
		if (minCloudListSize < Integer.MAX_VALUE) {
			result += Math.max(0, MAX_VALUE_FOR_SERVICE_RARITY - minCloudListSize);
		}
		
		return result;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private boolean isNonLocal(final List<String> cloudList) {
		return !ChoreographerDriver.OWN_CLOUD_MARKER.equals(cloudList.get(0));
	}
}
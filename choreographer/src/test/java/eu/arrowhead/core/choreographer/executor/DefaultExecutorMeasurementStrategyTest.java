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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class DefaultExecutorMeasurementStrategyTest {
	
	//=================================================================================================
	// members
	
	private DefaultExecutorMeasurementStrategy strategy = new DefaultExecutorMeasurementStrategy();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetMeasurementNullInput() {
		try {
			strategy.getMeasurement(null);
		} catch (final Exception ex) {
			Assert.assertEquals("input is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurementEmptyMap() {
		final int value = strategy.getMeasurement(Map.of());
		
		Assert.assertEquals(0, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement1() {
		final int value = strategy.getMeasurement(Map.of(1, List.of(), 2, List.of()));
		
		Assert.assertEquals(2, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement2() {
		final int value = strategy.getMeasurement(Map.of(1, List.of(), 2, List.of(), 3, List.of(), 4, List.of(), 5, List.of()));
		
		Assert.assertEquals(5, value);
	}
}
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class WeightedExecutorMeasurementStrategyTest {
	
	//=================================================================================================
	// members
	
	private WeightedExecutorMeasurementStrategy strategy = new WeightedExecutorMeasurementStrategy();
	
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
	public void testGetMeasurementEmptyCloudList() {
		final int value = strategy.getMeasurement(Map.of(1, List.of()));
		
		Assert.assertEquals(Integer.MAX_VALUE, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurementLocalOnly() {
		final int value = strategy.getMeasurement(Map.of(1, List.of("#OWN_CLOUD#"), 2, List.of("#OWN_CLOUD#")));
		
		Assert.assertEquals(2, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement1() {
		final int value = strategy.getMeasurement(Map.of(1, List.of("#OWN_CLOUD#"), 2, List.of("operator/name1", "operator/name2")));
		
		Assert.assertEquals(120, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement2() {
		final int value = strategy.getMeasurement(Map.of(1, List.of("operator/name1"), 2, List.of("operator/name1", "operator/name2")));
		
		Assert.assertEquals(141, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement3() {
		final int value = strategy.getMeasurement(Map.of(1, List.of("operator/name1", "operator/name2"), 2, List.of("operator/name1")));
		
		Assert.assertEquals(141, value);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement4() {
		final List<String> longList = Collections.nCopies(150, "cloudid");
		final int value = strategy.getMeasurement(Map.of(1, longList));
		
		Assert.assertEquals(21, value);
	}
}
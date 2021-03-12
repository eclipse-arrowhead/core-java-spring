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

package eu.arrowhead.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.exception.BadPayloadException;

@RunWith(SpringRunner.class)
public class CoreUtilitiesTest {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCalculateDirectionDirectionNull() {
		CoreUtilities.calculateDirection(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCalculateDirectionDirectionEmpty() {
		CoreUtilities.calculateDirection(" ", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCalculateDirectionDirectionInvalid() {
		CoreUtilities.calculateDirection("invalid", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateDirectionDirectionAsc() {
		final Direction direction = CoreUtilities.calculateDirection(" ASC ", null);
		Assert.assertEquals(Direction.ASC, direction);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateDirectionDirectionDesc() {
		final Direction direction = CoreUtilities.calculateDirection("desc", null);
		Assert.assertEquals(Direction.DESC, direction);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidatePageParametersPageAndSizeNull() {
		final ValidatedPageParams vpp = CoreUtilities.validatePageParameters(null, null, "ASC", "origin");
		Assert.assertEquals(0, vpp.getValidatedPage());
		Assert.assertEquals(Integer.MAX_VALUE, vpp.getValidatedSize());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testValidatePageParametersPageNullAndSizeNotNull() {
		CoreUtilities.validatePageParameters(null, 10, "ASC", "origin");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testValidatePageParametersPageNotNullAndSizeNull() {
		CoreUtilities.validatePageParameters(0, null, "ASC", "origin");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidatePageParametersPageAndSizeNotNull() {
		final ValidatedPageParams vpp = CoreUtilities.validatePageParameters(1, 15, "ASC", "origin");
		Assert.assertEquals(1, vpp.getValidatedPage());
		Assert.assertEquals(15, vpp.getValidatedSize());
	}
}
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

package eu.arrowhead.common.verifier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CommonNamePartVerifierTest {

	//=================================================================================================
	// members

	private CommonNamePartVerifier verifier = new CommonNamePartVerifier();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValid() {
		final boolean valid = verifier.isValid("valid-label");
		Assert.assertTrue(valid);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInvalid1() {
		final boolean valid = verifier.isValid("invalid_label");
		Assert.assertFalse(valid);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInvalid2() {
		final boolean valid = verifier.isValid("invalid-label-1-");
		Assert.assertFalse(valid);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInvalid3() {
		final boolean valid = verifier.isValid("invalid.label-2");
		Assert.assertFalse(valid);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInvalid4() {
		final boolean valid = verifier.isValid("1invalid-label");
		Assert.assertFalse(valid);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInvalid5() {
		final boolean valid = verifier.isValid("invalid-length-fgkfgnfdg65465fgfhghnkfdg61541fdgfdshhsdhsabgkjhk"); //64 character long instead of the maximum 63
		Assert.assertFalse(valid);
	}
}
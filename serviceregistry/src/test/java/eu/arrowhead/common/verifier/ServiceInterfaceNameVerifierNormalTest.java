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

package eu.arrowhead.common.verifier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.serviceregistry.ServiceRegistryMain;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration
public class ServiceInterfaceNameVerifierNormalTest {

	//=================================================================================================
	// members
	
	@Autowired
	private ServiceInterfaceNameVerifier verifier;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(verifier, ServiceInterfaceNameVerifier.FIELD_STRICT_MODE, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidNull() {
		Assert.assertFalse(verifier.isValid(null));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidEmpty() {
		Assert.assertFalse(verifier.isValid("   "));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidNoMatch() {
		Assert.assertFalse(verifier.isValid("json"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidNoWrongSeparator() {
		Assert.assertFalse(verifier.isValid("http_secure_json"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidGood() {
		Assert.assertTrue(verifier.isValid("unique_protocol-insecure-unique_format"));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidGood2() {
		Assert.assertTrue(verifier.isValid("unique_protocol-SECURE-unique_format"));
	}
}
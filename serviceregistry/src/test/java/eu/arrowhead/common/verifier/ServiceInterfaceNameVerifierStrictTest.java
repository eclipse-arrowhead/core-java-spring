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
public class ServiceInterfaceNameVerifierStrictTest {

	//=================================================================================================
	// members
	
	@Autowired
	private ServiceInterfaceNameVerifier verifier;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(verifier, ServiceInterfaceNameVerifier.FIELD_STRICT_MODE, true);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidUnknownProtocol() {
		Assert.assertFalse(verifier.isValid("unique_protocol-SECURE-JSON"));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidUnknownFormat() {
		Assert.assertFalse(verifier.isValid("HTTP-SECURE-unique-format"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsValidGood() {
		Assert.assertTrue(verifier.isValid("HTTP-SECURE-XML"));
	}
}
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

package eu.arrowhead.relay.gatekeeper.activemq;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.relay.RelayCryptographer;

public class ActiveMQGatekeeperRelayClientTest {

	//=================================================================================================
	// members

	private ActiveMQGatekeeperRelayClient testingObject;
	
	private PublicKey publicKey;
	private SSLProperties sslProps;
	private RelayCryptographer cryptographer;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		publicKey = Mockito.mock(PublicKey.class);
		sslProps = Mockito.mock(SSLProperties.class);
		cryptographer = Mockito.mock(RelayCryptographer.class);
		
		testingObject = new ActiveMQGatekeeperRelayClient("serverCN", publicKey, getTestPrivateKey(), sslProps, 60000);
		ReflectionTestUtils.setField(testingObject, "cryptographer", cryptographer);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		try {
			new ActiveMQGatekeeperRelayClient(null, null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		try {
			new ActiveMQGatekeeperRelayClient("", null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		try {
			new ActiveMQGatekeeperRelayClient("serverCN", null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Public key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		try {
			new ActiveMQGatekeeperRelayClient("serverCN", getTestPublicKey(), null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Private key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSSLPropertiesNull() {
		try {
			new ActiveMQGatekeeperRelayClient("serverCN", getTestPublicKey(), getTestPrivateKey(), null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("SSL properties object is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostNull() throws Exception {
		try {
			testingObject.createConnection(null, 0, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostEmpty() throws Exception {
		try {
			testingObject.createConnection("", 0, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooLow() throws Exception {
		try {
			testingObject.createConnection("localhost", -1, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooHigh() throws Exception {
		try {
			testingObject.createConnection("localhost", 100000, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PublicKey getTestPublicKey() {
		return new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PrivateKey getTestPrivateKey() {
		return new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		};
	}
}
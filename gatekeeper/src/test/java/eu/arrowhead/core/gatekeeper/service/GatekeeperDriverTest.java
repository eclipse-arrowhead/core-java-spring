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

package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class GatekeeperDriverTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperDriver testingObject;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private HttpService httpService;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoCommonName() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(false);
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventCommonNameWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn(new Object());
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoPublicKey() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gatekeeper.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(false);
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventPublicKeyWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gatekeeper.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn("not a public key");
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoPrivateKey() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gatekeeper.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(false);
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventPrivateKeyWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gatekeeper.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn("not a private key");
		
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test
	public void testOnApplicationEventEverythingOK() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gatekeeper.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		});
		ReflectionTestUtils.setField(testingObject, "sslProps", new SSLProperties());
		
		testingObject.onApplicationEvent(null);
		
		final Object relayClient = ReflectionTestUtils.getField(testingObject, "relayClient");
		Assert.assertNotNull(relayClient);
	}
}
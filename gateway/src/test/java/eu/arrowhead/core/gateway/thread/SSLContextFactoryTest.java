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

package eu.arrowhead.core.gateway.thread;

import java.util.ServiceConfigurationError;

import javax.net.ssl.SSLContext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.SSLProperties;

@RunWith(SpringRunner.class)
public class SSLContextFactoryTest {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextSSLPropsNull() {
		SSLContextFactory.createGatewaySSLContext(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextSSLNotEnabled() {
		final SSLProperties sslProps = new SSLProperties();

		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextKeystoreTypeNull() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextKeystoreTypeEmpty() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "");
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextKeystoreNull() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextKeystoreNotExist() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new FileSystemResource("invalid.txt");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextKeystorePasswordNull() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextTrustStoreNull() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextTrustStoreNotExist() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new FileSystemResource("invalid.txt");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGatewaySSLContextTrustStorePasswordNull() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new ClassPathResource("certificates/truststore.p12");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ServiceConfigurationError.class)
	public void testCreateGatewaySSLContextWhenInternalExceptionThrown() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "invalid");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new ClassPathResource("certificates/truststore.p12");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		ReflectionTestUtils.setField(sslProps, "trustStorePassword", "123456");
		
		SSLContextFactory.createGatewaySSLContext(sslProps);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateGatewaySSLContextEverythingOk() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new ClassPathResource("certificates/truststore.p12");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		ReflectionTestUtils.setField(sslProps, "trustStorePassword", "123456");
		
		final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(sslProps);
		
		Assert.assertNotNull(sslContext);;
	}
}
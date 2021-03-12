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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ServiceConfigurationError;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;

public class SSLContextFactory {
	
	//=================================================================================================
	// members
	
	private static final String TLS = "TLS";
	private static final String SSL_KEY_MANAGER_FACTORY_ALGORITHM = "ssl.KeyManagerFactory.algorithm";
	private static final String SSL_TRUST_MANAGER_FACTORY_ALGORITHM = "ssl.TrustManagerFactory.algorithm";
	
	private static final Logger logger = LogManager.getLogger(SSLContextFactory.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static SSLContext createGatewaySSLContext(final SSLProperties sslProps) {
		logger.debug("createProviderSideSSLContext started...");
		Assert.notNull(sslProps, "sslProps is null.");
		
		validateSSLProperties(sslProps);
		
		try {
			final KeyStore keyStore = KeyStore.getInstance(sslProps.getKeyStoreType());
			keyStore.load(sslProps.getKeyStore().getInputStream(), sslProps.getKeyStorePassword().toCharArray());
			final String kmfAlgorithm = System.getProperty(SSL_KEY_MANAGER_FACTORY_ALGORITHM, KeyManagerFactory.getDefaultAlgorithm());
		    final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(kmfAlgorithm);
		    keyManagerFactory.init(keyStore, sslProps.getKeyStorePassword().toCharArray());
			
		    final KeyStore trustStore = KeyStore.getInstance(sslProps.getKeyStoreType());
		    trustStore.load(sslProps.getTrustStore().getInputStream(), sslProps.getTrustStorePassword().toCharArray());
		    final String tmfAlgorithm = System.getProperty(SSL_TRUST_MANAGER_FACTORY_ALGORITHM, TrustManagerFactory.getDefaultAlgorithm());
		    final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
		    trustManagerFactory.init(trustStore);
		    
		    final SSLContext sslContext = SSLContext.getInstance(TLS);
		    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

			return sslContext;
		} catch (final KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException | KeyManagementException ex) {
			logger.error("SSL context creation failed: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			
			throw new ServiceConfigurationError("SSL context creation failed.", ex);
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SSLContextFactory() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void validateSSLProperties(final SSLProperties sslProps) {
		Assert.isTrue(sslProps.isSslEnabled(), "SSL is not enabled.");
		final String messageNotDefined = " is not defined.";
		Assert.isTrue(!Utilities.isEmpty(sslProps.getKeyStoreType()), CommonConstants.KEYSTORE_TYPE + messageNotDefined);
		Assert.notNull(sslProps.getKeyStore(), CommonConstants.KEYSTORE_PATH + messageNotDefined);
		Assert.isTrue(sslProps.getKeyStore().exists(), CommonConstants.KEYSTORE_PATH + " is not found.");
		Assert.notNull(sslProps.getKeyStorePassword(), CommonConstants.KEYSTORE_PASSWORD + messageNotDefined);
		Assert.notNull(sslProps.getTrustStore(), CommonConstants.TRUSTSTORE_PATH + messageNotDefined);
		Assert.isTrue(sslProps.getTrustStore().exists(), CommonConstants.TRUSTSTORE_PATH + " is not found.");
		Assert.notNull(sslProps.getTrustStorePassword(), CommonConstants.TRUSTSTORE_PASSWORD + messageNotDefined);
	}
}
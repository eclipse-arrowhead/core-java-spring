/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.config;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.hbconfmgr.SSLProperties;

/**
 * This class provides the trust store of the configuration system in different formats via beans managed by the Spring
 * container. The trust store contains the public keys that are trusted.
 */
@Configuration
public class TrustStoreConfig {

	@Autowired
	private SSLProperties sslProps;
	
	private KeyStore truststore;

    @Bean
    public TrustManagerFactory trustManagerFactory() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
		final KeyStore trustStore = loadTrustStore();
		final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		
		return trustManagerFactory;
    }

    private KeyStore loadTrustStore() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    	if (sslProps.isSslEnabled()) {
    		if (truststore == null) {
    	        truststore = KeyStore.getInstance(sslProps.getKeyStoreType());
    	        truststore.load(sslProps.getTrustStore().getInputStream(), sslProps.getTrustStorePassword().toCharArray());
    		}
    		
    		return truststore;
    	}
    	
    	return null;
    }
}
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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.hbconfmgr.SSLProperties;

/**
 * This class provides the key store of the configuration system in different formats via beans managed by the Spring
 * container. The key store contains the private and the public key of the configuration system itself.
 */
@Configuration
public class KeyStoreConfig {

	@Autowired
	private SSLProperties sslProps;
	
	private KeyStore keystore;

    @Bean
    public KeyManagerFactory hawkbitConfigurationSystemKeyManagerFactory() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
		final KeyStore keyStore = loadKeyStore();
		final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, sslProps.getKeyStorePassword().toCharArray());
		
		return keyManagerFactory;
    }

    @Bean
    public PrivateKey hawkbitConfigurationSystemPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
		final KeyStore keyStore = loadKeyStore();
		
		return (PrivateKey) keyStore.getKey(sslProps.getKeyAlias(), sslProps.getKeyPassword().toCharArray());
    }

    @Bean
    public PublicKey hawkbitConfigurationSystemPublicKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
   		final KeyStore keyStore = loadKeyStore();
   		
   		return keyStore.getCertificate(sslProps.getKeyAlias()).getPublicKey();
    }

    private KeyStore loadKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    	if (sslProps.isSslEnabled()) {
    		if (keystore == null) {
    	        keystore = KeyStore.getInstance(sslProps.getKeyStoreType());
    	        keystore.load(sslProps.getKeyStore().getInputStream(), sslProps.getKeyStorePassword().toCharArray());
    		}
    		
    		return keystore;
    	}
    	
    	return null;
    }
}
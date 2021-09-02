/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class provides the key store of the HawkBit Configuration Manager in different formats via beans managed by the Spring
 * container. The key store contains the private and the public key of the HawkBit Configuration Manager itself.
 */
@Configuration
public class KeyStoreConfig {

    private static final String KEY_STORE_TYPE = "pkcs12";

    @Value("${keyStore.path}")
    private String keyStorePath;

    @Value("${keyStore.password}")
    private String keyStorePassword;

    @Value("${keyStore.alias}")
    private String keyStoreAlias;

    @Bean
    public KeyManagerFactory configurationSystemKeyManagerFactory() throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException, IOException, CertificateException {
        KeyStore keyStore = loadKeyStore();
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        return keyManagerFactory;
    }

    @Bean
    public PrivateKey configurationSystemPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateException {
        KeyStore keyStore = loadKeyStore();
        return (PrivateKey) keyStore.getKey(keyStoreAlias, keyStorePassword.toCharArray());
    }

    @Bean
    public PublicKey configurationSystemPublicKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = loadKeyStore();
        return keyStore.getCertificate(keyStoreAlias).getPublicKey();
    }

    private KeyStore loadKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        keyStore.load(new FileInputStream(new File(keyStorePath)), keyStorePassword.toCharArray());
        return keyStore;
    }

}

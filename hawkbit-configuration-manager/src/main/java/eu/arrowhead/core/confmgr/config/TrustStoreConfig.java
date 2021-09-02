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
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class provides the trust store of the HawkBit Configuration Manager in different formats via beans managed by the Spring
 * container. The trust store contains the public keys that are trusted.
 */
@Configuration
public class TrustStoreConfig {

    private static final String KEY_STORE_TYPE = "pkcs12";

    @Value("${trustStore.path}")
    private String trustStorePath;

    @Value("${trustStore.password}")
    private String trustStorePassword;

    @Bean
    public TrustManagerFactory trustManagerFactory() throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException {
        KeyStore trustStore = loadTrustStore();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory;
    }

    private KeyStore loadTrustStore() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE);
        trustStore.load(new FileInputStream(new File(trustStorePath)), trustStorePassword.toCharArray());
        return trustStore;
    }

}

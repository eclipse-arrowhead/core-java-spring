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

import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Configuration
@Profile("test")
public class TestConfig {
    /**
     * This Bean returns a mocked TrustManager implementation to test
     * whether the correct certificate is used during TLS handshake
     * @return mocked TrustManager object
     */
    @Bean
    public TrustManager[] getTestTrustManager() {
        TrustManager[] tm = {
            new X509ExtendedTrustManager(){
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
                }
        
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
                    log.info("Server authentication with the following certificate:");
                    log.error(chain[0]);
                }
        
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                }
        
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                    log.info("Server authentication with the following certificate:");
                    log.error(chain[0]);
                }
        
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
        
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
        
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    log.info("Server authentication with the following certificate:");
                    log.error(certs[0]);
                }
            }
        };

        return tm;
    }
    
}

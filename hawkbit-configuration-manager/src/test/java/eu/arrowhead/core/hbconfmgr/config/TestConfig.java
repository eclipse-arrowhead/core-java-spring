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
        final TrustManager[] tm = {
            new X509ExtendedTrustManager(){
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket) {
                }
        
                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket) {
                    log.info("Server authentication with the following certificate:");
                    log.error(chain[0]);
                }
        
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) {
                }
        
                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) {
                    log.info("Server authentication with the following certificate:");
                    log.error(chain[0]);
                }
        
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
        
                @Override
                public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                }
        
                @Override
                public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                    log.info("Server authentication with the following certificate:");
                    log.error(certs[0]);
                }
            }
        };

        return tm;
    }
}
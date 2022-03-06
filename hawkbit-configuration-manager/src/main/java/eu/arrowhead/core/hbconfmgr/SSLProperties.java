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

package eu.arrowhead.core.hbconfmgr;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@DependsOn(Constants.GUARD_BEAN)
public class SSLProperties {
	
	//=================================================================================================
	// members
	
	@Value(Constants.SSL_ENABLED)
	private boolean sslEnabled;
	
	@Value(Constants.KEYSTORE_TYPE)
	private String keyStoreType;
	
	@Value(Constants.KEYSTORE_PATH)
	private String keyStorePath;	
	private Resource keyStore;
	
	@Value(Constants.KEYSTORE_PASSWORD)
	private String keyStorePassword;
	
	@Value(Constants.KEY_ALIAS)
	private String keyAlias;
	
	@Value(Constants.KEY_PASSWORD)
	private String keyPassword;
	
	@Value(Constants.TRUSTSTORE_PATH)
	private String trustStorePath;
	private Resource trustStore;
	
	@Value(Constants.TRUSTSTORE_PASSWORD)
	private String trustStorePassword;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public boolean isSslEnabled() { return sslEnabled; }
	public String getKeyStoreType() { return keyStoreType; }
	public Resource getKeyStore() { return keyStore; }
	public String getKeyStorePassword() { return keyStorePassword; }
	public String getKeyAlias() { return keyAlias; }
	public String getKeyPassword() { return keyPassword; }
	public Resource getTrustStore() { return trustStore; }
	public String getTrustStorePassword() { return trustStorePassword; }
	
	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validate() {
		if (sslEnabled) {
			if (isEmpty(keyStoreType)) {
				throw new RuntimeException("keyStoreType is missing");
			}
			
			if (isEmpty(keyStorePath)) {
				throw new RuntimeException("keyStorePath is missing");
			} else {
				keyStore = resourceLoader.getResource(keyStorePath);
			}
			
			if (isEmpty(keyStorePassword)) {
				throw new RuntimeException("keyStorePassword is missing");
			}
			
			if (isEmpty(keyAlias)) {
				throw new RuntimeException("keyAlias is missing");
			}
			
			if (isEmpty(keyPassword)) {
				throw new RuntimeException("keyPassword is missing");
			}
			
			if (isEmpty(trustStorePath)) {
				throw new RuntimeException("trustStorePath is missing");
			} else {
				trustStore = resourceLoader.getResource(trustStorePath);
			}
			
			if (isEmpty(trustStorePassword)) {
				throw new RuntimeException("trustStorePassword is missing");
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isEmpty(final String str) {
		return str == null || str.trim().isEmpty();
	}
}
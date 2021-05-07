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

package eu.arrowhead.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.exception.InvalidParameterException;

@Component
public class SSLProperties {
	
	//=================================================================================================
	// members
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Value(CommonConstants.$KEYSTORE_TYPE_WD)
	private String keyStoreType;
	
	@Value(CommonConstants.$KEYSTORE_PATH_WD)
	private String keyStorePath;	
	private Resource keyStore;
	
	@Value(CommonConstants.$KEYSTORE_PASSWORD_WD)
	private String keyStorePassword;
	
	@Value(CommonConstants.$KEY_PASSWORD_WD)
	private String keyPassword;
	
	@Value(CommonConstants.$TRUSTSTORE_PATH_WD)
	private String trustStorePath;
	private Resource trustStore;
	
	@Value(CommonConstants.$TRUSTSTORE_PASSWORD_WD)
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
	public String getKeyPassword() { return keyPassword; }
	public Resource getTrustStore() { return trustStore; }
	public String getTrustStorePassword() { return trustStorePassword; }
	
	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validate() {
		if (sslEnabled) {
			if (Utilities.isEmpty(keyStoreType)) {
				throw new InvalidParameterException("keyStoreType is missing");
			}
			
			if (Utilities.isEmpty(keyStorePath)) {
				throw new InvalidParameterException("keyStorePath is missing");
			} else {
				keyStore = resourceLoader.getResource(keyStorePath);
			}
			
			if (Utilities.isEmpty(keyStorePassword)) {
				throw new InvalidParameterException("keyStorePassword is missing");
			}
			
			if (Utilities.isEmpty(keyPassword)) {
				throw new InvalidParameterException("keyPassword is missing");
			}
			
			if (Utilities.isEmpty(trustStorePath)) {
				throw new InvalidParameterException("trustStorePath is missing");
			} else {
				trustStore = resourceLoader.getResource(trustStorePath);
			}
			
			if (Utilities.isEmpty(trustStorePassword)) {
				throw new InvalidParameterException("trustStorePassword is missing");
			}
		}
	}
}
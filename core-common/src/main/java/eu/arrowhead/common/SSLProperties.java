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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SSLProperties {
	
	//=================================================================================================
	// members
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Value(CommonConstants.$KEYSTORE_TYPE)
	private String keyStoreType;
	
	@Value(CommonConstants.$KEYSTORE_PATH)
	private Resource keyStore;
	
	@Value(CommonConstants.$KEYSTORE_PASSWORD)
	private String keyStorePassword;
	
	@Value(CommonConstants.$KEY_PASSWORD)
	private String keyPassword;
	
	@Value(CommonConstants.$TRUSTSTORE_PATH)
	private Resource trustStore;
	
	@Value(CommonConstants.$TRUSTSTORE_PASSWORD)
	private String trustStorePassword;
	
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
}
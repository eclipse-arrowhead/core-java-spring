/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr;

public class Constants {
	
	public static final String GUARD_BEAN = "guardBean";
	
	public static final String HTTPS = "https";
	
	public static final String APPLICATION_JSON = "application/json";
	
	public static final String SSL_ENABLED = "${server.ssl.enabled:true}";
	public static final String KEYSTORE_TYPE = "${server.ssl.key-store-type:PKCS12}";
	public static final String KEYSTORE_PATH  ="${server.ssl.key-store:}";
	public static final String KEYSTORE_PASSWORD = "${server.ssl.key-store-password:}";
	public static final String KEY_PASSWORD = "${server.ssl.key-password:}";
	public static final String KEY_ALIAS = "${server.ssl.key-alias:}";
	public static final String TRUSTSTORE_PATH = "${server.ssl.trust-store:}";
	public static final String TRUSTSTORE_PASSWORD = "${server.ssl.trust-store-password:}";
	
	public static final String SERVICE_REGISTRY_ADDRESS = "${sr_address:localhost}";
	public static final String SERVICE_REGISTRY_PORT = "${sr_port:8443}";
	
	public static final String HAWKBIT_HOST = "${hawkbit.host:localhost}";
	public static final String HAWKBIT_PORT = "${hawkbit.port:8080}";
	public static final String HAWKBIT_USER = "${hawkbit.username:}";
	public static final String HAWKBIT_PASSWORD = "${hawkbit.password:}";
	public static final String HAWKBIT_TENANT = "${hawkbit.tenant:DEFAULT}";
	
	public static final String CORE_SERVICE_AUTH_PUBLIC_KEY = "auth-public-key";

	private Constants() {
		throw new UnsupportedOperationException();
	}
}
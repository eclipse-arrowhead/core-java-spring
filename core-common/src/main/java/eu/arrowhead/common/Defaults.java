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

public class Defaults {

	//=================================================================================================
	// members
	
	public static final int DEFAULT_VERSION = 1;
	
	public static final boolean DEFAULT_SSL_SERVER_ENABLED = true;
	public static final boolean DEFAULT_WEBSOCKETS_ENABLED = false;
	public static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";
	public static final String DEFAULT_KEYSTORE_PATH = "";
	public static final String DEFAULT_KEYSTORE_PASSWORD = "";
	public static final String DEFAULT_KEY_PASSWORD = "";
	public static final String DEFAULT_TRUSTSTORE_PATH = "";
	public static final String DEFAULT_TRUSTSTORE_PASSWORD ="";
	public static final boolean DEFAULT_DISABLE_HOSTNAME_VERIFIER = false;
	public static final boolean DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE = false;
	public static final int DEFAULT_URI_CRAWLER_INTERVAL = 30;
	public static final int DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES = -1; // never expires

    public static final String DEFAULT_SERVICEREGISTRY_ADDRESS = CommonConstants.LOCALHOST;
    public static final int DEFAULT_ONBOARDING_PORT = 8435;
    public static final int DEFAULT_SYSTEMREGISTRY_PORT = 8437;
	public static final int DEFAULT_DEVICEREGISTRY_PORT = 8439;
	public static final int DEFAULT_ORCHESTRATOR_PORT = 8441;
	public static final int DEFAULT_SERVICEREGISTRY_PORT = 8443;
	public static final int DEFAULT_AUTHORIZATION_PORT = 8445;
    public static final int DEFAULT_GATEKEEPER_PORT = 8449;
	public static final int DEFAULT_QOSMONITOR_PORT = 8451;
	public static final int DEFAULT_GATEWAY_PORT = 8453;
	public static final int DEFAULT_EVENTHANDLER_PORT = 8455;
	public static final int DEFAULT_DATAMANAGER_PORT = 8461;
	public static final int DEFAULT_TIMEMANAGER_PORT = 8463;
	public static final int DEFAULT_CHOREOGRAPHER_PORT = 8457;
	public static final int DEFAULT_CERTIFICATEAUTHORITY_PORT = 8448;
    public static final int DEFAULT_CONFIGURATION_PORT = 8459;
	public static final int DEFAULT_CERTIFICATE_AUTHORITY_PORT = 8448;
    public static final int DEFAULT_GENERIC_AUTONOMIC_MANAGEMENT_PORT = 8502;
	public static final int DEFAULT_TRANSLATOR_PORT = 8462;
    public static final int DEFAULT_MSCV_PORT = 8501;
	public static final int DEFAULT_PLANT_DESCRIPTION_ENGINE_PORT = 8432;
	public static final int DEFAULT_HAWKBIT_CONFIGURATION_MANAGER_PORT = 8447;

    // HTTP client defaults
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT = 10000;

    // CORS defaults
    public static final long CORS_MAX_AGE = 600;
    public static final String CORS_ALLOW_CREDENTIALS = "true";

    // CA defaults
	public static final long DEFAULT_CA_CERT_VALIDITY_NEG_OFFSET_MINUTES = 1;
	public static final long DEFAULT_CA_CERT_VALIDITY_POS_OFFSET_MINUTES = 60 * 24 * 365;

    //-------------------------------------------------------------------------------------------------
    private Defaults() {
        throw new UnsupportedOperationException();
    }
}

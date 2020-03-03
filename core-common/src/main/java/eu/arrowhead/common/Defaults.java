package eu.arrowhead.common;

public class Defaults {

	//=================================================================================================
	// members
	
	public static final boolean DEFAULT_SSL_SERVER_ENABLED = true;
	public static final boolean DEFAULT_DISABLE_HOSTNAME_VERIFIER = false;
	public static final boolean DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE = false;
	public static final int DEFAULT_URI_CRAWLER_INTERVAL = 30;
	public static final int DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES = -1; // never expires

	public static final String DEFAULT_SERVICE_REGISTRY_ADDRESS = CommonConstants.LOCALHOST;	
	public static final int DEFAULT_SERVICE_REGISTRY_PORT = 8443;
	public static final int DEFAULT_AUTHORIZATION_PORT = 8445;
	public static final int DEFAULT_ORCHESTRATOR_PORT = 8441;
	public static final int DEFAULT_GATEKEEPER_PORT = 8449;
	public static final int DEFAULT_GATEWAY_PORT = 8453;
	public static final int DEFAULT_EVENT_HANDLER_PORT = 8455;
	public static final int DEFAULT_CHOREOGRAPHER_PORT = 8457;
	public static final int DEFAULT_CERTIFICATE_AUTHORITY_PORT = 8459;
	public static final int DEFAULT_QOS_MONITOR_PORT = 8451;
	
	// HTTP client defaults
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT = 10000;
	
	// CORS defaults
	public static final long CORS_MAX_AGE = 600;
	public static final String CORS_ALLOW_CREDENTIALS = "true";
	
	//-------------------------------------------------------------------------------------------------
	private Defaults() {
		throw new UnsupportedOperationException();
	}
}
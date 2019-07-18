package eu.arrowhead.common;

public class Defaults {

	//=================================================================================================
	// members

	public static final int VARCHAR_LOG = 100;
	public static final int VARCHAR_BASIC = 255;
	public static final int VARCHAR_EXTENDED = 2047;
	
	public static final boolean DEFAULT_SSL_SERVER_ENABLED = true;
	public static final boolean DEFAULT_DISABLE_HOSTNAME_VERIFIER = false;
	public static final boolean DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE = false;
	public static final String DEFAULT_SERVICE_REGISTRY_ADDRESS = CommonConstants.LOCALHOST;
	
	public static final int DEFAULT_SERVICE_REGISTRY_PORT = 8443;
	public static final int DEFAULT_AUTHORIZATION_PORT = 8445;
	public static final int DEFAULT_ORCHESTRATOR_PORT = 8441;
	public static final int DEFAULT_GATEKEEPER_PORT = 8449;
	public static final int DEFAULT_GATEWAY_PORT = 8453;
	public static final int DEFAULT_EVENT_HANDLER_PORT = 8455;
	public static final int DEFAULT_CERTIFICATE_AUTHORITY_PORT = 8459;

	public static final boolean DEFAULT_SERVICE_REGISTRY_PING_SCHEDULED = false;
	public static final int DEFAULT_SERVICE_REGISTRY_PING_INTERVAL_MINUTES = 60;
	public static final int DEFAULT_SERVICE_REGISTRY_PING_TIMEOUT_MILISECONDS = 5000;
	public static final boolean DEFAULT_SERVICE_REGISTRY_TTL_SCHEDULED = false;
	public static final int DEFAULT_SERVICE_REGISTRY_TTL_INTERVAL_MINUTES = 13;
	public static final boolean DEFAULT_SERVICE_REGISTRY_USE_STRICT_SERVICE_INTF_NAME_VERIFIER = true;
	
	// HTTP client defaults
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT = 10000;
	
	// CORS defaults
	public static final long CORS_MAX_AGE = 600;
	public static final String CORS_ALLOW_CREDENTIALS = "true";
	
	public static final boolean DEFAULT_IS_GATEKEEPER_PRESENT = false;
	
	public static final String DEFAULT_OWN_CLOUD_OPERATOR = "default_operator";
	public static final String DEFAULT_OWN_CLOUD_NAME = "default_insecure_cloud";
	
	public static final String DEFAULT_REQUEST_PARAM_DIRECTION_VALUE = CommonConstants.SORT_ORDER_ASCENDING;
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Defaults() {
		throw new UnsupportedOperationException();
	}
}
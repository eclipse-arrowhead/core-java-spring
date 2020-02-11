package eu.arrowhead.common;

public class CoreDefaults {

	//=================================================================================================
	// members

	public static final int VARCHAR_LOG = 100;
	public static final int VARCHAR_BASIC = 255;
	public static final int VARCHAR_EXTENDED = 2047;
	
	public static final boolean DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE = false;
	public static final boolean DEFAULT_USE_STRICT_SERVICE_INTF_NAME_VERIFIER = false;
	public static final int DEFAULT_URI_CRAWLER_INTERVAL = 30;
	public static final int DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES = -1; // never expires
	
	public static final boolean DEFAULT_SERVICE_REGISTRY_PING_SCHEDULED = false;
	public static final int DEFAULT_SERVICE_REGISTRY_PING_INTERVAL_MINUTES = 60;
	public static final int DEFAULT_SERVICE_REGISTRY_PING_TIMEOUT_MILISECONDS = 5000;
	public static final boolean DEFAULT_SERVICE_REGISTRY_TTL_SCHEDULED = false;
	public static final int DEFAULT_SERVICE_REGISTRY_TTL_INTERVAL_MINUTES = 13;
	
	public static final String DEFAULT_OWN_CLOUD_OPERATOR = "default_operator";
	public static final String DEFAULT_OWN_CLOUD_NAME = "default_insecure_cloud";
	
	public static final String DEFAULT_REQUEST_PARAM_DIRECTION_VALUE = CoreCommonConstants.SORT_ORDER_ASCENDING;
	
	public static final boolean DEFAULT_AUTHORIZATION_IS_EVENTHANDLER_PRESENT = false;
	
	public static final boolean DEFAULT_ORCHESTRATOR_IS_GATEKEEPER_PRESENT = false;
	
	public static final int DEFAULT_RELAY_CHECK_INTERVAL = 9;
	public static final int DEFAULT_NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS = 50;
	
	public static final boolean DEFAULT_GATEKEEPER_IS_GATEWAY_PRESENT = false;
	public static final boolean DEFAULT_GATEKEEPER_IS_GATEWAY_MANDATORY = false;
	
	public static final int DEFAULT_GATEWAY_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_GATEWAY_MIN_PORT = 8000;
	public static final int DEFAULT_GATEWAY_MAX_PORT = 8100;
	
	public static final boolean DEFAULT_EVENT_HANDLER_TTL_SCHEDULED = false;
	public static final int DEFAULT_EVENT_HANDLER_TTL_INTERVAL_MINUTES = 17;
	
	public static final long DEFAULT_TIME_STAMP_TOLERANCE_SECONDS = 120;
	public static final int DEFAULT_EVENT_HANDLER_MAX_EXPRESS_SUBSCRIBERS = 10;
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private CoreDefaults() {
		throw new UnsupportedOperationException();
	}
}
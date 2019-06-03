package eu.arrowhead.common;

public class Defaults {

	public static final int VARCHAR_LOG = 100;
	public static final int VARCHAR_BASIC = 255;
	public static final int VARCHAR_EXTENDED = 2047;
	
	public static final boolean DEFAULT_SSL_SERVER_ENABLED = true;
	public static final String DEFAULT_SERVICE_REGISTRY_ADDRESS = CommonConstants.LOCALHOST;
	public static final int DEFAULT_SERVICE_REGISTRY_PORT = 8443;
	public static final boolean DEFAULT_SERVICE_REGISTRY_PING_SCHEDULED = false;
	public static final int DEFAULT_SERVICE_REGISTRY_PING_INTERVAL_MINUTES = 60;
	public static final int DEFAULT_SERVICE_REGISTRY_PING_TIMEOUT_MILISECONDS = 5000;
	public static final boolean DEFAULT_SERVICE_REGISTRY_TTL_SCHEDULED = false;
	public static final int DEFAULT_SERVICE_REGISTRY_TTL_INTERVAL_MINUTES = 13;
	public static final boolean DEFAULT_DISABLE_HOSTNAME_VERIFIER = false;
	
	// HTTP client defaults
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT = 10000;
	
	private Defaults() {
		throw new UnsupportedOperationException();
	}
}

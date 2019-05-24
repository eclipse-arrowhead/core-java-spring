package eu.arrowhead.common;

public class Defaults {
	
	public static final boolean DEFAULT_SSL_SERVER_ENABLED = true;
	public static final String DEFAULT_SERVICE_REGISTRY_ADDRESS = "127.0.0.1";
	public static final int DEFAULT_SERVICE_REGISTRY_PORT = 8443;
	public static final boolean DEFAULT_DISABLE_HOSTNAME_VERIFIER = false;
	
	// HTTP client defaults
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT = 10000;
	
	private Defaults() {
		throw new UnsupportedOperationException();
	}
}

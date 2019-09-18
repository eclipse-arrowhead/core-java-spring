package eu.arrowhead.common;

public class Defaults {

	//=================================================================================================
	// members
	
	//=================================================================================================
	// assistant methods
	
	public static final String DEFAULT_SERVICE_REGISTRY_ADDRESS = CoreCommonConstants.LOCALHOST;
	public static final int DEFAULT_SERVICE_REGISTRY_PORT = 8443;
	
	// HTTP client defaults
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_CONNECTION_MANAGER_TIMEOUT = 10000;
	
	// CORS defaults
	public static final long CORS_MAX_AGE = 600;
	public static final String CORS_ALLOW_CREDENTIALS = "true";
	
	public static final boolean DEFAULT_SSL_SERVER_ENABLED = true;
	public static final boolean DEFAULT_DISABLE_HOSTNAME_VERIFIER = false;
	
	//-------------------------------------------------------------------------------------------------
	private Defaults() {
		throw new UnsupportedOperationException();
	}
}

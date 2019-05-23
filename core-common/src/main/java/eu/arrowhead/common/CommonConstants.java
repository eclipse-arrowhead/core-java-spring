package eu.arrowhead.common;

public class CommonConstants {
	
	public static final String APPLICATION_PROPERTIES = "application.properties";
	
	public static final String DATABASE_URL = "spring.datasource.url";
	public static final String DATABASE_USER = "spring.datasource.username";
	public static final String DATABASE_PASSWORD = "spring.datasource.password";
	public static final String DATABASE_DRIVER_CLASS = "spring.datasource.driver-class-name"; 
	
	public static final String SERVER_SSL_ENABLED = "server.ssl.enabled";
	public static final String $SERVER_SSL_ENABLED = "${" + SERVER_SSL_ENABLED + "}";
	public static final String KEYSTORE_TYPE = "server.ssl.key-store-type";
	public static final String $KEYSTORE_TYPE = "${" + KEYSTORE_TYPE + "}";
	public static final String KEYSTORE_PATH = "server.ssl.key-store";
	public static final String $KEYSTORE_PATH = "${" + KEYSTORE_PATH + "}";
	public static final String KEYSTORE_PASSWORD = "server.ssl.key-store-password";
	public static final String $KEYSTORE_PASSWORD = "${" + KEYSTORE_PASSWORD + "}";
	public static final String KEY_PASSWORD = "server.ssl.key-password";
	public static final String $KEY_PASSWORD = "${" + KEY_PASSWORD + "}";
	public static final String TRUSTSTORE_PATH = "server.ssl.trust-store";
	public static final String $TRUSTSTORE_PATH = "${" + TRUSTSTORE_PATH + "}";
	public static final String TRUSTSTORE_PASSWORD = "server.ssl.trust-store-password";
	public static final String $TRUSTSTORE_PASSWORD = "${" + TRUSTSTORE_PASSWORD + "}";
	
	public static final String SERVICE_REGISTRY_ADDRESS = "sr_address";
	public static final String $SERVICE_REGISTRY_ADDRESS = "${" + SERVICE_REGISTRY_ADDRESS + "}";
	public static final String SERVICE_REGISTRY_PORT = "sr_port";
	public static final String $SERVICE_REGISTRY_PORT = "${" + SERVICE_REGISTRY_PORT + "}";

	private CommonConstants() {
		throw new UnsupportedOperationException();
	}
}

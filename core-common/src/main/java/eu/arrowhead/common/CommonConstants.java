package eu.arrowhead.common;

public class CommonConstants {
	
	public static final String APPLICATION_PROPERTIES = "application.properties";
	
	public static final String BASE_PACKAGE = "eu.arrowhead";
	
	public static final String DATABASE_URL = "spring.datasource.url";
	public static final String DATABASE_USER = "spring.datasource.username";
	public static final String DATABASE_PASSWORD = "spring.datasource.password"; //NOSONAR it is not a password 
	public static final String DATABASE_DRIVER_CLASS = "spring.datasource.driver-class-name"; 
	public static final String DATABASE_ENTITY_PACKAGE = "eu.arrowhead.common.database.entity";
	public static final String DATABASE_REPOSITORY_PACKAGE = "eu.arrowhead.common.database.repository";
	
	public static final String CORE_SYSTEM_AUTHORIZATION = "Authorization";
	public static final String CORE_SYSTEM_EVENT_HANDLER = "Event Handler";
	public static final String CORE_SYSTEM_GATEKEEPER = "Gatekeeper";
	public static final String CORE_SYSTEM_GATEWAY = "Gateway";
	public static final String CORE_SYSTEM_ORCHESTRATOR = "Orchestrator";
	public static final String CORE_SYSTEM_SERVICE_REGISTRY = "Service Registry";
	
	public static final String COMMON_FIELD_NAME_ID = "id";
	
	public static final String ARROWHEAD_CONTEXT = "arrowheadContext";
	public static final String SERVER_COMMON_NAME = "server.common.name";
	
	public static final String UNKNOWN_ORIGIN = "<unknown>";
	public static final String ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";
	public static final String COMMON_NAME_FIELD_NAME = "CN";
	
	public static final String HTTPS = "https";
	public static final String HTTP = "http";
	public static final String LOCALHOST = "127.0.0.1";
	public static final int HTTP_PORT = 80;
	
	public static final String SERVER_ERROR_URI = "/error";
	public static final String SERVICEREGISTRY_URI = "/serviceregistry";

	public static final String SWAGGER_COMMON_PACKAGE = "eu.arrowhead.common.swagger";
	public static final String SWAGGER_UI_URI = "/swagger-ui.html";
	public static final String SWAGGER_HTTP_200_MESSAGE = "Core service is available";
	public static final String SWAGGER_HTTP_401_MESSAGE = "You are not authorized";
	public static final String SWAGGER_HTTP_500_MESSAGE = "Core service is not available";
	
	public static final String SERVER_SSL_ENABLED = "server.ssl.enabled";
	public static final String $SERVER_SSL_ENABLED_WD = "${" + SERVER_SSL_ENABLED + ":" + Defaults.DEFAULT_SSL_SERVER_ENABLED + "}";
	public static final String KEYSTORE_TYPE = "server.ssl.key-store-type";
	public static final String $KEYSTORE_TYPE = "${" + KEYSTORE_TYPE + "}";
	public static final String KEYSTORE_PATH = "server.ssl.key-store";
	public static final String $KEYSTORE_PATH = "${" + KEYSTORE_PATH + "}";
	public static final String KEYSTORE_PASSWORD = "server.ssl.key-store-password"; //NOSONAR it is not a password
	public static final String $KEYSTORE_PASSWORD = "${" + KEYSTORE_PASSWORD + "}"; //NOSONAR it is not a password
	public static final String KEY_PASSWORD = "server.ssl.key-password"; //NOSONAR it is not a password
	public static final String $KEY_PASSWORD = "${" + KEY_PASSWORD + "}"; //NOSONAR it is not a password
	public static final String TRUSTSTORE_PATH = "server.ssl.trust-store";
	public static final String $TRUSTSTORE_PATH = "${" + TRUSTSTORE_PATH + "}";
	public static final String TRUSTSTORE_PASSWORD = "server.ssl.trust-store-password"; //NOSONAR it is not a password
	public static final String $TRUSTSTORE_PASSWORD = "${" + TRUSTSTORE_PASSWORD + "}"; //NOSONAR it is not a password
	public static final String DISABLE_HOSTNAME_VERIFIER = "disable.hostname.verifier";
	public static final String $DISABLE_HOSTNAME_VERIFIER_WD = "${" + DISABLE_HOSTNAME_VERIFIER + ":" + Defaults.DEFAULT_DISABLE_HOSTNAME_VERIFIER + "}";
	
	public static final String HTTP_CLIENT_CONNECTION_TIMEOUT = "http.client.connection.timeout";
	public static final String $HTTP_CLIENT_CONNECTION_TIMEOUT_WD = "${" + HTTP_CLIENT_CONNECTION_TIMEOUT + ":" + Defaults.DEFAULT_CONNECTION_TIMEOUT + "}";
	public static final String HTTP_CLIENT_SOCKET_TIMEOUT = "http.client.socket.timeout";
	public static final String $HTTP_CLIENT_SOCKET_TIMEOUT_WD = "${" + HTTP_CLIENT_SOCKET_TIMEOUT + ":" + Defaults.DEFAULT_SOCKET_TIMEOUT + "}";
	public static final String HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT = "http.client.connection.manager.timeout";
	public static final String $HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT_WD = "${" + HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT + ":" + Defaults.DEFAULT_CONNECTION_MANAGER_TIMEOUT + "}";
	
	public static final String SERVICE_REGISTRY_ADDRESS = "sr_address";
	public static final String $SERVICE_REGISTRY_ADDRESS_WD = "${" + SERVICE_REGISTRY_ADDRESS + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_ADDRESS + "}";
	public static final String SERVICE_REGISTRY_PORT = "sr_port";
	public static final String $SERVICE_REGISTRY_PORT_WD = "${" + SERVICE_REGISTRY_PORT + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_PORT + "}";
	public static final String SERVICE_REGISTRY_PING_SCHEDULED = "ping_scheduled";
	public static final String $SERVICE_REGISTRY_PING_SCHEDULED_WD = "${" + SERVICE_REGISTRY_PING_SCHEDULED + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_PING_SCHEDULED + "}";
	public static final String SERVICE_REGISTRY_PING_INTERVAL = "ping_interval";
	public static final String $SERVICE_REGISTRY_PING_INTERVAL_WD = "${" + SERVICE_REGISTRY_PING_INTERVAL + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_PING_INTERVAL_MINUTES + "}";
	public static final String SERVICE_REGISTRY_PING_TIMEOUT = "ping_timeout";
	public static final String $SERVICE_REGISTRY_PING_TIMEOUT_WD = "${" + SERVICE_REGISTRY_PING_TIMEOUT + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_PING_TIMEOUT_MILISECONDS + "}";
	public static final String SERVICE_REGISTRY_TTL_SCHEDULED = "ttl_scheduled";
	public static final String $SERVICE_REGISTRY_TTL_SCHEDULED_WD = "${" + SERVICE_REGISTRY_TTL_SCHEDULED + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_TTL_SCHEDULED + "}";
	public static final String SERVICE_REGISTRY_TTL_INTERVAL = "ttl_interval";
	public static final String $SERVICE_REGISTRY_TTL_INTERVAL_WD = "${" + SERVICE_REGISTRY_TTL_INTERVAL + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_TTL_INTERVAL_MINUTES + "}";
	
	public static final int CONVERSION_MILLISECOND_TO_MINUTES = 60000;
	
	public static final String LOG_ALL_REQUEST_AND_RESPONSE = "log_all_request_and_response";
	public static final String $LOG_ALL_REQUEST_AND_RESPONSE_WD = "${" + LOG_ALL_REQUEST_AND_RESPONSE + ":" + Defaults.DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE + "}";

	private CommonConstants() {
		throw new UnsupportedOperationException();
	}
}

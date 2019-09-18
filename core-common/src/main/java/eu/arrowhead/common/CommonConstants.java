package eu.arrowhead.common;

import java.util.List;

import eu.arrowhead.common.core.CoreSystemService;

public class CommonConstants {

	//=================================================================================================
	// members
	
	public static final String BASE_PACKAGE = "eu.arrowhead";
	
	public static final String ARROWHEAD_CONTEXT = "arrowheadContext";
	public static final String SERVER_COMMON_NAME = "server.common.name";
	public static final String SERVER_PUBLIC_KEY = "server.public.key";
	public static final String SERVER_PRIVATE_KEY = "server.private.key";
	
	public static final String HTTPS = "https";
	public static final String HTTP = "http";
	public static final String JSON = "JSON";
	public static final String XML = "XML";
	public static final String HTTP_SECURE_JSON = HTTP + "-SECURE-" + JSON; 
	public static final String HTTP_INSECURE_JSON = HTTP + "-INSECURE-" + JSON;
	
	public static final String SERVICE_REGISTRY_ADDRESS = "sr_address";
	public static final String $SERVICE_REGISTRY_ADDRESS_WD = "${" + SERVICE_REGISTRY_ADDRESS + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_ADDRESS + "}";
	public static final String SERVICE_REGISTRY_PORT = "sr_port";
	public static final String $SERVICE_REGISTRY_PORT_WD = "${" + SERVICE_REGISTRY_PORT + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_PORT + "}";
	
	public static final String SERVICE_REGISTRY_URI = "/serviceregistry";
	public static final String OP_SERVICE_REGISTRY_REGISTER_URI = "/register";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_URI = "/unregister";
	public static final String OP_SERVICE_REGISTRY_QUERY_URI = "/query";	
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME = "system_name";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS = "address";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT = "port";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";
	
	public static final String ORCHESTRATON_FLAG_MATCHMAKING = "matchmaking";
	public static final String ORCHESTRATON_FLAG_METADATA_SEARCH = "metadataSearch";
	public static final String ORCHESTRATON_FLAG_ONLY_PREFERRED = "onlyPreferred";
	public static final String ORCHESTRATON_FLAG_PING_PROVIDERS = "pingProviders";
	public static final String ORCHESTRATON_FLAG_OVERRIDE_STORE = "overrideStore";
	public static final String ORCHESTRATON_FLAG_TRIGGER_INTER_CLOUD = "triggerInterCloud";
	public static final String ORCHESTRATON_FLAG_EXTERNAL_SERVICE_REQUEST = "externalServiceRequest";
	public static final String ORCHESTRATON_FLAG_ENABLE_INTER_CLOUD = "enableInterCloud";
	public static final String ORCHESTRATON_FLAG_ENABLE_QOS = "enableQoS";
	
	public static final String REQUEST_PARAM_TOKEN = "token";
	
	public static final String ECHO_URI = "/echo";
	
	public static final List<CoreSystemService> PUBLIC_CORE_SYSTEM_SERVICES = List.of(CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
			 															   			  CoreSystemService.EVENT_PUBLISH, CoreSystemService.EVENT_SUBSCRIBE);
	
	public static final String HTTP_CLIENT_CONNECTION_TIMEOUT = "http.client.connection.timeout";
	public static final String $HTTP_CLIENT_CONNECTION_TIMEOUT_WD = "${" + HTTP_CLIENT_CONNECTION_TIMEOUT + ":" + Defaults.DEFAULT_CONNECTION_TIMEOUT + "}";
	public static final String HTTP_CLIENT_SOCKET_TIMEOUT = "http.client.socket.timeout";
	public static final String $HTTP_CLIENT_SOCKET_TIMEOUT_WD = "${" + HTTP_CLIENT_SOCKET_TIMEOUT + ":" + Defaults.DEFAULT_SOCKET_TIMEOUT + "}";
	public static final String HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT = "http.client.connection.manager.timeout";
	public static final String $HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT_WD = "${" + HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT + ":" + Defaults.DEFAULT_CONNECTION_MANAGER_TIMEOUT + "}";
	
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
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CommonConstants() {
		throw new UnsupportedOperationException();
	}
}

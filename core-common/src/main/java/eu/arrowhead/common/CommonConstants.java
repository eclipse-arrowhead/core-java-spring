package eu.arrowhead.common;

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;

public class CommonConstants {
	
	//=================================================================================================
	// members
	
	public static final String APPLICATION_PROPERTIES = "application.properties";
	
	public static final String BASE_PACKAGE = "eu.arrowhead";
	
	public static final String DATABASE_URL = "spring.datasource.url";
	public static final String DATABASE_USER = "spring.datasource.username";
	public static final String DATABASE_PASSWORD = "spring.datasource.password"; //NOSONAR it is not a password 
	public static final String DATABASE_DRIVER_CLASS = "spring.datasource.driver-class-name"; 
	public static final String DATABASE_ENTITY_PACKAGE = "eu.arrowhead.common.database.entity";
	public static final String DATABASE_REPOSITORY_PACKAGE = "eu.arrowhead.common.database.repository";
	public static final String DATABASE_OPERATION_EXCEPTION_MSG = "Database operation exception";
	
	public static final String CORE_SYSTEM_AUTHORIZATION = "Authorization";
	public static final String CORE_SYSTEM_EVENT_HANDLER = "Event Handler";
	public static final String CORE_SYSTEM_GATEKEEPER = "Gatekeeper";
	public static final String CORE_SYSTEM_GATEWAY = "Gateway";
	public static final String CORE_SYSTEM_ORCHESTRATOR = "Orchestrator";
	public static final String CORE_SYSTEM_SERVICE_REGISTRY = "Service Registry";
	
	public static final String CORE_SERVICE_AUTH_TOKEN_GENERATION = "token-generation";
	public static final String CORE_SERVICE_AUTH_PUBLIC_KEY = "public-key";
	public static final String CORE_SERVICE_AUTH_CONTROL_INTRA = "authorization-control-intra";
	public static final String CORE_SERVICE_AUTH_CONTROL_INTER = "authorization-control-inter";
	public static final String CORE_SERVICE_ORCH_PROCESS = "orchestration-service";
	public static final String CORE_SERVICE_GATEKEEPER_GSD = "global-service-discovery";
	public static final String CORE_SERVICE_GATEKEEPER_ICN = "inter-cloud-negotiations";
	public static final String CORE_SERVICE_EVENT_HANDLER_PUBLISH = "event-publish";
	public static final String CORE_SERVICE_EVENT_HANDLER_SUBSCRIBE = "event-subscribe";
	
	public static final String COMMON_FIELD_NAME_ID = "id";
	
	public static final String ARROWHEAD_CONTEXT = "arrowheadContext";
	public static final String SERVER_COMMON_NAME = "server.common.name";
	public static final String SERVER_PUBLIC_KEY = "server.public.key";
	public static final String SERVER_PRIVATE_KEY = "server.private.key";
	public static final String SERVER_STANDALONE_MODE = "server.standalone.mode";
	public static final String SR_QUERY_URI = "service.registry.query.uri";
	public static final String SR_QUERY_BY_SYSTEM_ID_URI = "service.registry.query.by.system.id.uri";
	public static final String SR_QUERY_BY_SYSTEM_DTO_URI = "service.registry.query.by.system.dto.uri";
	public static final String REQUIRED_URI_LIST = "required.uri.list";
	public static final String URI_SUFFIX = "-uri";
	
	public static final String JWT_CLAIM_CONSUMER_ID = "cid";
	public static final String JWT_CLAIM_SERVICE_ID = "sid";
	public static final String JWT_CLAIM_INTERFACE_ID = "iid";
	public static final String JWT_CLAIM_MESSAGE_TYPE = "mst";
	public static final String JWT_CLAIM_SESSION_ID = "sid"; // can be the same as service id because we don't use service id and session id at the same time
	public static final String JWT_CLAIM_PAYLOAD = "pld";  
	public static final String JWE_KEY_MANAGEMENT_ALG = KeyManagementAlgorithmIdentifiers.RSA_OAEP_256;
	public static final String JWE_ENCRYPTION_ALG = ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512;
	public static final String JWS_SIGN_ALG = AlgorithmIdentifiers.RSA_USING_SHA512;
	
	public static final String RELAY_MESSAGE_TYPE_RAW = "raw";
	public static final String RELAY_MESSAGE_TYPE_ACK = "ack";
	public static final String RELAY_MESSAGE_TYPE_GSD_POLL = "gsd_poll";
	public static final String RELAY_MESSAGE_TYPE_ICN_PROPOSAL = "icn_proposal";
	
	public static final String UNKNOWN_ORIGIN = "<unknown>";
	public static final String ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";
	public static final String COMMON_NAME_FIELD_NAME = "CN";
	public static final String LOCAL_SYSTEM_OPERATOR_NAME = "sysop";
	
	public static final String HTTPS = "https";
	public static final String HTTP = "http";
	public static final String JSON = "JSON";
	public static final String XML = "XML";
	public static final String HTTP_SECURE_JSON = HTTP + "-SECURE-" + JSON; 
	public static final String HTTP_INSECURE_JSON = HTTP + "-INSECURE-" + JSON; 
	
	public static final String LOCALHOST = "localhost";
	public static final int HTTP_PORT = 8080;
	
	public static final String SERVER_ERROR_URI = "/error";
	public static final String ECHO_URI = "/echo";
	public static final String MGMT_URI = "/mgmt";
	
	public static final String SERVICE_REGISTRY_URI = "/serviceregistry";
	public static final String OP_SERVICE_REGISTRY_REGISTER_URI = "/register";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_URI = "/unregister";
	public static final String OP_SERVICE_REGISTRY_QUERY_URI = "/query";
	public static final String OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI = "/query/system/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI = "/query/system";
	
	public static final String AUTHORIZATION_URI = "/authorization";
	public static final String OP_AUTH_TOKEN_URI = "/token";
	public static final String OP_AUTH_KEY_URI = "/publickey";
	public static final String OP_AUTH_INTRA_CHECK_URI = "/intracloud/check";
	public static final String OP_AUTH_INTER_CHECK_URI = "/intercloud/check";
	
	public static final String ORCHESTRATOR_URI = "/orchestrator";
	public static final String ORCHESTRATOR_STORE_MGMT_URI = "/mgmt/store";
	public static final String OP_ORCH_PROCESS = "/orchestration";
	
	public static final String GATEKEEPER_URI = "/gatekeeper";
	public static final String OP_GATEKEEPER_GSD_SERVICE = "/init_gsd";
	public static final String OP_GATEKEEPER_ICN_SERVICE = "/init_icn";
	
	public static final String EVENT_HANDLER_URI = "/event_handler";
	public static final String OP_EVENT_HANDLER_PUBLISH = "/publish";
	public static final String OP_EVENT_HANDLER_SUBSCRIBE = "/subscribe";

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
	
	public static final String SERVER_ADDRESS = "server.address";
	public static final String $SERVER_ADDRESS = "${" + SERVER_ADDRESS + "}";
	public static final String SERVER_PORT = "server.port";
	public static final String $SERVER_PORT = "${" + SERVER_PORT + "}";
	public static final String CORE_SYSTEM_NAME = "core_system_name";
	public static final String $CORE_SYSTEM_NAME = "${" + CORE_SYSTEM_NAME + "}";
	public static final String LOG_ALL_REQUEST_AND_RESPONSE = "log_all_request_and_response";
	public static final String $LOG_ALL_REQUEST_AND_RESPONSE_WD = "${" + LOG_ALL_REQUEST_AND_RESPONSE + ":" + Defaults.DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE + "}";
	public static final String USE_STRICT_SERVICE_INTF_NAME_VERIFIER = "use_strict_service_intf_name_verifier";
	public static final String $USE_STRICT_SERVICE_INTF_NAME_VERIFIER_WD = "${" + USE_STRICT_SERVICE_INTF_NAME_VERIFIER + ":" + Defaults.DEFAULT_USE_STRICT_SERVICE_INTF_NAME_VERIFIER + "}";
	public static final String URI_CRAWLER_INTERVAL = "uri_crawler_interval"; // in seconds
	public static final String $URI_CRAWLER_INTERVAL_WD = "${" + URI_CRAWLER_INTERVAL + ":" + Defaults.DEFAULT_URI_CRAWLER_INTERVAL + "}";
	public static final String AUTH_TOKEN_TTL_IN_MINUTES = "auth_token_ttl_in_minutes";
	public static final String $AUTH_TOKEN_TTL_IN_MINUTES_WD = "${" + AUTH_TOKEN_TTL_IN_MINUTES + ":" + Defaults.DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES + "}";

	public static final String REQUEST_PARAM_PAGE = "page";
	public static final String REQUEST_PARAM_ITEM_PER_PAGE = "item_per_page";
	public static final String REQUEST_PARAM_DIRECTION = "direction";
	public static final String REQUEST_PARAM_SORT_FIELD = "sort_field";
	public static final String REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";
	public static final String REQUEST_PARAM_TOKEN = "token";
	
	public static final int SYSTEM_PORT_RANGE_MIN = 0;
	public static final int SYSTEM_PORT_RANGE_MAX = 65535;
	
	public static final long CONVERSION_MILLISECOND_TO_SECOND = 1000;
	public static final long CONVERSION_MILLISECOND_TO_MINUTE = 60000;

	public static final String SORT_ORDER_ASCENDING = "ASC";
	public static final String SORT_ORDER_DESCENDING = "DESC";
	
	public static final String SORT_FIELD_PRIORITY = "priority";

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
	
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME = "system_name";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS = "address";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT = "port";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";
	
	public static final String ORCHESTRATOR_IS_GATEKEEPER_PRESENT = "gatekeeper_is_present";
	public static final String $ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD = "${" + ORCHESTRATOR_IS_GATEKEEPER_PRESENT + ":" + Defaults.DEFAULT_ORCHESTRATOR_IS_GATEKEEPER_PRESENT + "}";
	
	public static final String ORCHESTRATON_FLAG_MATCHMAKING = "matchmaking";
	public static final String ORCHESTRATON_FLAG_METADATA_SEARCH = "metadataSearch";
	public static final String ORCHESTRATON_FLAG_ONLY_PREFERRED = "onlyPreferred";
	public static final String ORCHESTRATON_FLAG_PING_PROVIDERS = "pingProviders";
	public static final String ORCHESTRATON_FLAG_OVERRIDE_STORE = "overrideStore";
	public static final String ORCHESTRATON_FLAG_TRIGGER_INTER_CLOUD = "triggerInterCloud";
	public static final String ORCHESTRATON_FLAG_EXTERNAL_SERVICE_REQUEST = "externalServiceRequest";
	public static final String ORCHESTRATON_FLAG_ENABLE_INTER_CLOUD = "enableInterCloud";
	public static final String ORCHESTRATON_FLAG_ENABLE_QOS = "enableQoS";

	public static final String RELAY_CHECK_INTERVAL = "relay_check_interval"; // in seconds
	public static final String $RELAY_CHECK_INTERVAL_WD = "${" + RELAY_CHECK_INTERVAL + ":" + Defaults.DEFAULT_RELAY_CHECK_INTERVAL + "}";
	public static final String GATEKEEPER_IS_GATEWAY_PRESENT = "gateway_is_present";
	public static final String $GATEKEEPER_IS_GATEWAY_PRESENT_WD = "${" + GATEKEEPER_IS_GATEWAY_PRESENT + ":" + Defaults.DEFAULT_GATEKEEPER_IS_GATEWAY_PRESENT + "}";
	public static final String GATEKEEPER_IS_GATEWAY_MANDATORY = "gateway_is_mandatory";
	public static final String $GATEKEEPER_IS_GATEWAY_MANDATORY_WD = "${" + GATEKEEPER_IS_GATEWAY_MANDATORY + ":" + Defaults.DEFAULT_GATEKEEPER_IS_GATEWAY_MANDATORY + "}";
	
	public static final String INTRA_CLOUD_PROVIDER_MATCHMAKER = "intraCloudProviderMatchmaker";
	public static final String INTER_CLOUD_PROVIDER_MATCHMAKER = "interCloudProviderMatchmaker";
	public static final String GATEKEEPER_MATCHMAKER = "gatekeeperMatchmaker";
	public static final String CLOUD_MATCHMAKER = "cloudMatchmaker";
	
	public static final int TOP_PRIORITY = 1;
	
	public static final String NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS = "no_gatekeeper_relay_request_handler_workers";
	public static final String $NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS_WD = "${" + NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS + ":" + 
																				 Defaults.DEFAULT_NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS + "}";

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CommonConstants() {
		throw new UnsupportedOperationException();
	}
}

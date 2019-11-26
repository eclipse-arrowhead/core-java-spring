package eu.arrowhead.common;

public class CoreCommonConstants {
	
	//=================================================================================================
	// members
	
	public static final String APPLICATION_PROPERTIES = "application.properties";
	
	public static final String DATABASE_URL = "spring.datasource.url";
	public static final String DATABASE_USER = "spring.datasource.username";
	public static final String DATABASE_PASSWORD = "spring.datasource.password"; //NOSONAR it is not a password 
	public static final String DATABASE_DRIVER_CLASS = "spring.datasource.driver-class-name"; 
	public static final String DATABASE_ENTITY_PACKAGE = "eu.arrowhead.common.database.entity";
	public static final String DATABASE_REPOSITORY_PACKAGE = "eu.arrowhead.common.database.repository";
	public static final String DATABASE_OPERATION_EXCEPTION_MSG = "Database operation exception";
		
	public static final String COMMON_FIELD_NAME_ID = "id";
	
	public static final String SERVER_STANDALONE_MODE = "server.standalone.mode";
	public static final String SR_QUERY_URI = "service.registry.query.uri";
	public static final String SR_QUERY_BY_SYSTEM_ID_URI = "service.registry.query.by.system.id.uri";
	public static final String SR_QUERY_BY_SYSTEM_DTO_URI = "service.registry.query.by.system.dto.uri";
	public static final String REQUIRED_URI_LIST = "required.uri.list";
	public static final String URI_SUFFIX = "-uri";
	
	public static final String RELAY_MESSAGE_TYPE_RAW = "raw";
	public static final String RELAY_MESSAGE_TYPE_ACK = "ack";
	public static final String RELAY_MESSAGE_TYPE_GSD_POLL = "gsd_poll";
	public static final String RELAY_MESSAGE_TYPE_ICN_PROPOSAL = "icn_proposal";
	
	public static final String LOCAL_SYSTEM_OPERATOR_NAME = "sysop"; 
	
	public static final String SERVER_ERROR_URI = "/error";
	public static final String MGMT_URI = "/mgmt";
	
	public static final String OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI = "/query/system/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI = "/query/system";
		
	public static final String ORCHESTRATOR_STORE_MGMT_URI = "/mgmt/store";
	
	public static final String SWAGGER_COMMON_PACKAGE = "eu.arrowhead.common.swagger";
	public static final String SWAGGER_UI_URI = "/swagger-ui.html";
	public static final String SWAGGER_HTTP_200_MESSAGE = "Core service is available";
	public static final String SWAGGER_HTTP_401_MESSAGE = "You are not authorized";
	public static final String SWAGGER_HTTP_500_MESSAGE = "Core service is not available";
	
	public static final String SWAGGER_TAG_MGMT = "Management";
	public static final String SWAGGER_TAG_CLIENT = "Client";
	public static final String SWAGGER_TAG_PRIVATE = "Private";
	public static final String SWAGGER_TAG_ALL = "All";
	
	public static final String SERVER_ADDRESS = "server.address";
	public static final String $SERVER_ADDRESS = "${" + SERVER_ADDRESS + ":}";
	public static final String SERVER_PORT = "server.port";
	public static final String $SERVER_PORT = "${" + SERVER_PORT + ":0}";
	public static final String DOMAIN_NAME = "domain.name";
	public static final String $DOMAIN_NAME = "${" + DOMAIN_NAME + ":}";
	public static final String DOMAIN_PORT = "domain.port";
	public static final String $DOMAIN_PORT = "${" + DOMAIN_PORT + ":0}";
	public static final String CORE_SYSTEM_NAME = "core_system_name";
	public static final String $CORE_SYSTEM_NAME = "${" + CORE_SYSTEM_NAME + "}";
	public static final String LOG_ALL_REQUEST_AND_RESPONSE = "log_all_request_and_response";
	public static final String $LOG_ALL_REQUEST_AND_RESPONSE_WD = "${" + LOG_ALL_REQUEST_AND_RESPONSE + ":" + CoreDefaults.DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE + "}";
	public static final String USE_STRICT_SERVICE_INTF_NAME_VERIFIER = "use_strict_service_intf_name_verifier";
	public static final String $USE_STRICT_SERVICE_INTF_NAME_VERIFIER_WD = "${" + USE_STRICT_SERVICE_INTF_NAME_VERIFIER + ":" + CoreDefaults.DEFAULT_USE_STRICT_SERVICE_INTF_NAME_VERIFIER + "}";
	public static final String URI_CRAWLER_INTERVAL = "uri_crawler_interval"; // in seconds
	public static final String $URI_CRAWLER_INTERVAL_WD = "${" + URI_CRAWLER_INTERVAL + ":" + CoreDefaults.DEFAULT_URI_CRAWLER_INTERVAL + "}";
	public static final String AUTH_TOKEN_TTL_IN_MINUTES = "auth_token_ttl_in_minutes";
	public static final String $AUTH_TOKEN_TTL_IN_MINUTES_WD = "${" + AUTH_TOKEN_TTL_IN_MINUTES + ":" + CoreDefaults.DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES + "}";

	public static final String REQUEST_PARAM_PAGE = "page";
	public static final String REQUEST_PARAM_ITEM_PER_PAGE = "item_per_page";
	public static final String REQUEST_PARAM_DIRECTION = "direction";
	public static final String REQUEST_PARAM_SORT_FIELD = "sort_field";
	public static final String REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";
	
	public static final long CONVERSION_MILLISECOND_TO_SECOND = 1000;
	public static final long CONVERSION_MILLISECOND_TO_MINUTE = 60000;

	public static final String SORT_ORDER_ASCENDING = "ASC";
	public static final String SORT_ORDER_DESCENDING = "DESC";
	
	public static final String SORT_FIELD_PRIORITY = "priority";

	public static final String SERVICE_REGISTRY_PING_SCHEDULED = "ping_scheduled";
	public static final String $SERVICE_REGISTRY_PING_SCHEDULED_WD = "${" + SERVICE_REGISTRY_PING_SCHEDULED + ":" + CoreDefaults.DEFAULT_SERVICE_REGISTRY_PING_SCHEDULED + "}";
	public static final String SERVICE_REGISTRY_PING_INTERVAL = "ping_interval";
	public static final String $SERVICE_REGISTRY_PING_INTERVAL_WD = "${" + SERVICE_REGISTRY_PING_INTERVAL + ":" + CoreDefaults.DEFAULT_SERVICE_REGISTRY_PING_INTERVAL_MINUTES + "}";
	public static final String SERVICE_REGISTRY_PING_TIMEOUT = "ping_timeout";
	public static final String $SERVICE_REGISTRY_PING_TIMEOUT_WD = "${" + SERVICE_REGISTRY_PING_TIMEOUT + ":" + CoreDefaults.DEFAULT_SERVICE_REGISTRY_PING_TIMEOUT_MILISECONDS + "}";
	public static final String SERVICE_REGISTRY_TTL_SCHEDULED = "ttl_scheduled";
	public static final String $SERVICE_REGISTRY_TTL_SCHEDULED_WD = "${" + SERVICE_REGISTRY_TTL_SCHEDULED + ":" + CoreDefaults.DEFAULT_SERVICE_REGISTRY_TTL_SCHEDULED + "}";
	public static final String SERVICE_REGISTRY_TTL_INTERVAL = "ttl_interval";
	public static final String $SERVICE_REGISTRY_TTL_INTERVAL_WD = "${" + SERVICE_REGISTRY_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_SERVICE_REGISTRY_TTL_INTERVAL_MINUTES + "}";
	
    public static final String AUTHORIZATION_IS_EVENTHANDLER_PRESENT = "eventhandler_is_present";
    public static final String $AUTHORIZATION_IS_EVENTHANDLER_PRESENT_WD = "${" + AUTHORIZATION_IS_EVENTHANDLER_PRESENT + ":" +CoreDefaults.DEFAULT_AUTHORIZATION_IS_EVENTHANDLER_PRESENT + "}";
	
	public static final String ORCHESTRATOR_IS_GATEKEEPER_PRESENT = "gatekeeper_is_present";
	public static final String $ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD = "${" + ORCHESTRATOR_IS_GATEKEEPER_PRESENT + ":" + CoreDefaults.DEFAULT_ORCHESTRATOR_IS_GATEKEEPER_PRESENT + "}";

	public static final String RELAY_CHECK_INTERVAL = "relay_check_interval"; // in seconds
	public static final String $RELAY_CHECK_INTERVAL_WD = "${" + RELAY_CHECK_INTERVAL + ":" + CoreDefaults.DEFAULT_RELAY_CHECK_INTERVAL + "}";
	public static final String GATEKEEPER_IS_GATEWAY_PRESENT = "gateway_is_present";
	public static final String $GATEKEEPER_IS_GATEWAY_PRESENT_WD = "${" + GATEKEEPER_IS_GATEWAY_PRESENT + ":" + CoreDefaults.DEFAULT_GATEKEEPER_IS_GATEWAY_PRESENT + "}";
	public static final String GATEKEEPER_IS_GATEWAY_MANDATORY = "gateway_is_mandatory";
	public static final String $GATEKEEPER_IS_GATEWAY_MANDATORY_WD = "${" + GATEKEEPER_IS_GATEWAY_MANDATORY + ":" + CoreDefaults.DEFAULT_GATEKEEPER_IS_GATEWAY_MANDATORY + "}";
	
	public static final String INTRA_CLOUD_PROVIDER_MATCHMAKER = "intraCloudProviderMatchmaker";
	public static final String INTER_CLOUD_PROVIDER_MATCHMAKER = "interCloudProviderMatchmaker";
	public static final String ICN_PROVIDER_MATCHMAKER = "icnProviderMatchmaker";
	public static final String GATEKEEPER_MATCHMAKER = "gatekeeperMatchmaker";
	public static final String GATEWAY_MATCHMAKER = "gatewayMatchmaker";
	public static final String CLOUD_MATCHMAKER = "cloudMatchmaker";
	
	public static final int TOP_PRIORITY = 1;
	
	public static final String NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS = "no_gatekeeper_relay_request_handler_workers";
	public static final String $NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS_WD = "${" + NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS + ":" + 
																				 CoreDefaults.DEFAULT_NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS + "}";
	
	public static final String GATEWAY_SOCKET_TIMEOUT = "gateway_socket_timeout";
	public static final String $GATEWAY_SOCKET_TIMEOUT_WD = "${" + GATEWAY_SOCKET_TIMEOUT + ":" + CoreDefaults.DEFAULT_GATEWAY_SOCKET_TIMEOUT + "}";
	public static final String GATEWAY_MIN_PORT = "min_port";
	public static final String $GATEWAY_MIN_PORT_WD = "${" + GATEWAY_MIN_PORT + ":" + CoreDefaults.DEFAULT_GATEWAY_MIN_PORT + "}";
	public static final String GATEWAY_MAX_PORT = "max_port";
	public static final String $GATEWAY_MAX_PORT_WD = "${" + GATEWAY_MAX_PORT + ":" + CoreDefaults.DEFAULT_GATEWAY_MAX_PORT + "}";
	
	public static final String GATEWAY_ACTIVE_SESSION_MAP = "activeSessions";
	public static final String GATEWAY_AVAILABLE_PORTS_QUEUE = "availableQueue";
	
	public static final String EVENT_HANDLER_TTL_SCHEDULED = "event_handler_ttl_scheduled";
	public static final String $EVENT_HANDLER_TTL_SCHEDULED_WD = "${" + EVENT_HANDLER_TTL_SCHEDULED + ":" + CoreDefaults.DEFAULT_EVENT_HANDLER_TTL_SCHEDULED + "}";
	public static final String EVENT_HANDLER_TTL_INTERVAL = "event_handler_ttl_interval";
	public static final String $EVENT_HANDLER_TTL_INTERVAL_WD = "${" + EVENT_HANDLER_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_EVENT_HANDLER_TTL_INTERVAL_MINUTES + "}";

	public static final String TIME_STAMP_TOLERANCE_SECONDS = "time_stamp_tolerance_seconds";
	public static final String $TIME_STAMP_TOLERANCE_SECONDS_WD = "${" + TIME_STAMP_TOLERANCE_SECONDS + ":" + CoreDefaults.DEFAULT_TIME_STAMP_TOLERANCE_SECONDS + "}";

	public static final String EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE = "SUBSCRIBER_AUTH_UPDATE";	
	public static final String EVENT_PUBLISHING_QUEUE = "eventPublishingQueue";
	public static final String EVENT_PUBLISHING_QUEUE_WATCHER_TASK = "eventPublishingQueueWatcherTask";
	public static final String EVENT_HANDLER_MAX_EXPRESS_SUBSCRIBERS = "event_handler_max_express_subscribers";
	public static final String $EVENT_HANDLER_MAX_EXPRESS_SUBSCRIBERS_WD = "${" + EVENT_HANDLER_MAX_EXPRESS_SUBSCRIBERS + ":" + CoreDefaults.DEFAULT_EVENT_HANDLER_MAX_EXPRESS_SUBSCRIBERS + "}";
	public static final String EVENT_PUBLISHING_EXPRESS_EXECUTOR = "eventPublishingExpressExecutor";

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreCommonConstants() {
		throw new UnsupportedOperationException();
	}
}
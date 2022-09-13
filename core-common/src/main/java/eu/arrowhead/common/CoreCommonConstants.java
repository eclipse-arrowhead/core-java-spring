/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common;

public class CoreCommonConstants {
	
	//=================================================================================================
	// members
	
	public static final String APPLICATION_PROPERTIES = "application.properties";
	public static final String QUARTZ_THREAD_PROPERTY = "org.quartz.threadPool.threadCount";
	
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
	public static final String SR_MULTI_QUERY_URI = "service.registry.multi.query.uri";
	public static final String SR_QUERY_BY_SYSTEM_ID_URI = "service.registry.query.by.system.id.uri";
	public static final String SR_QUERY_BY_SYSTEM_DTO_URI = "service.registry.query.by.system.dto.uri";
	public static final String SR_QUERY_ALL = "service.registry.query.all.uri";
	public static final String SR_QUERY_BY_SERVICE_DEFINITION_LIST_URI = "service.registry.query.services.by.service.definition.list.uri";
	public static final String SR_REGISTER_SYSTEM_URI = "service.registry.register.system.uri";
	public static final String SR_UNREGISTER_SYSTEM_URI = "service.registry.unregister.system.uri";
	public static final String SR_PULL_CONFIG_URI = "service.registry.pull.config.uri";
	public static final String REQUIRED_URI_LIST = "required.uri.list";
	public static final String URI_SUFFIX = "-uri";
	
	public static final String RELAY_MESSAGE_TYPE_RAW = "raw";
	public static final String RELAY_MESSAGE_TYPE_ACK = "ack";
	public static final String RELAY_MESSAGE_TYPE_GSD_POLL = "gsd_poll";
	public static final String RELAY_MESSAGE_TYPE_MULTI_GSD_POLL = "multi_gsd_poll";
	public static final String RELAY_MESSAGE_TYPE_ICN_PROPOSAL = "icn_proposal";
	public static final String RELAY_MESSAGE_TYPE_ACCESS_TYPE = "access_type";
	public static final String RELAY_MESSAGE_TYPE_SYSTEM_ADDRESS_LIST = "system_address_list";
	public static final String RELAY_MESSAGE_TYPE_QOS_RELAY_TEST = "qos_relay_test";
	
	public static final String LOCAL_SYSTEM_OPERATOR_NAME = "sysop"; 
	
	public static final String SERVER_ERROR_URI = "/error";
	public static final String MGMT_URI = "/mgmt";

	public static final String OP_QUERY_LOG_ENTRIES = MGMT_URI + "/logs";
	public static final String QUERY_LOG_ENTRIES_HTTP_200_MESSAGE = "Log entries returned.";
	public static final String QUERY_LOG_ENTRIES_HTTP_400_MESSAGE = "Invalid parameters.";
	public static final String REQUEST_PARAM_LOG_LEVEL = "level";
	public static final String REQUEST_PARAM_FROM = "from";
	public static final String REQUEST_PARAM_TO = "to";
	public static final String REQUEST_PARAM_LOGGER = "logger";

	public static final String OP_DEVICEREGISTRY_QUERY_BY_DEVICE_ID_URI = "/query/device/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_URI = "/query/device";

	public static final String OP_SYSTEMREGISTRY_QUERY_BY_SYSTEM_ID_URI = "/query/system/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_SYSTEMREGISTRY_QUERY_BY_SYSTEM_DTO_URI = "/query/system";

	public static final String OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_URI = "/query/system/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI = "/query/system";
	public static final String OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI = "/query/all";

	public static final String OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SYSTEM_ID_URI = "/query/provider/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_SERVICEREGISTRY_MULTI_QUERY_URI = CommonConstants.OP_SERVICEREGISTRY_QUERY_URI + "/multi";	
	public static final String OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SERVICE_DEFINITION_LIST_URI = "/query/definition";
	public static final String OP_SERVICEREGISTRY_PULL_CONFIG_URI = "/pull-config";
		
	public static final String ORCHESTRATOR_STORE_MGMT_URI = "/mgmt/store";
	public static final String ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI = "/mgmt/store/flexible";
	public static final String ORCHESTRATOR_STORE_FLEXIBLE_BY_ID_MGMT_URI = ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI + "/{" + COMMON_FIELD_NAME_ID + "}";

	public static final String SWAGGER_COMMON_PACKAGE = "eu.arrowhead.common.swagger";
	public static final String SWAGGER_UI_URI = "/swagger-ui.html";
	public static final String SWAGGER_HTTP_200_MESSAGE = "Core service is available";
	public static final String SWAGGER_HTTP_201_MESSAGE = "Created";
	public static final String SWAGGER_HTTP_400_MESSAGE = "Bad request";
	public static final String SWAGGER_HTTP_401_MESSAGE = "You are not authorized";
	public static final String SWAGGER_HTTP_404_MESSAGE = "Not found";
	public static final String SWAGGER_HTTP_409_MESSAGE = "Request caused a conflict";
	public static final String SWAGGER_HTTP_415_MESSAGE = "MediaType not supported";
	public static final String SWAGGER_HTTP_500_MESSAGE = "Core service is not available";
	
	public static final String SWAGGER_TAG_MGMT = "Management";
	public static final String SWAGGER_TAG_CLIENT = "Client";
	public static final String SWAGGER_TAG_PRIVATE = "Private";
	public static final String SWAGGER_TAG_ONBOARDING = "Onboarding";
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
	public static final String USE_STRICT_SERVICE_DEFINITION_VERIFIER = "use_strict_service_definition_verifier";
	public static final String $USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD = "${" + USE_STRICT_SERVICE_DEFINITION_VERIFIER + ":" + CoreDefaults.DEFAULT_USE_STRICT_SERVICE_DEFINITION_VERIFIER + "}";
	public static final String USE_NETWORK_ADDRRESS_DETECTOR = "use_network_address_detector";
	public static final String $USE_NETWORK_ADDRESS_DETECTOR_WD = "${" + USE_NETWORK_ADDRRESS_DETECTOR + ":" + CoreDefaults.DEFAULT_USE_NETWORK_ADDRESS_DETECTOR + "}";
	public static final String FILTER_PROXY_ADDRESSES = "filter_proxy_addresses";
	public static final String $FILTER_PROXY_ADDRESSES_WD = "${" + "filter_proxy_addresses" + ":" + CoreDefaults.DEFAULT_FILTER_PROXY_ADDRESSES + "}";
	public static final String ALLOW_SELF_ADDRESSING = "allow_self_addressing";
	public static final String $ALLOW_SELF_ADDRESSING_WD = "${" + ALLOW_SELF_ADDRESSING + ":" + CoreDefaults.DEFAULT_ALLOW_SELF_ADDRESSING + "}";
	public static final String ALLOW_NON_ROUTABLE_ADDRESSING = "allow_non_routable_addressing";
	public static final String $ALLOW_NON_ROUTABLE_ADDRESSING_WD = "${" + ALLOW_NON_ROUTABLE_ADDRESSING + ":" + CoreDefaults.DEFAULT_ALLOW_NON_ROUTABLE_ADDRESSING + "}";
	public static final String URI_CRAWLER_INTERVAL = "uri_crawler_interval"; // in seconds
	public static final String $URI_CRAWLER_INTERVAL_WD = "${" + URI_CRAWLER_INTERVAL + ":" + CoreDefaults.DEFAULT_URI_CRAWLER_INTERVAL + "}";
	public static final String AUTH_TOKEN_TTL_IN_MINUTES = "auth_token_ttl_in_minutes";
	public static final String $AUTH_TOKEN_TTL_IN_MINUTES_WD = "${" + AUTH_TOKEN_TTL_IN_MINUTES + ":" + CoreDefaults.DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES + "}";

	public static final String PARAM_VERIFICATION_INTERVAL = "mscv.defaultVerificationInterval"; // in seconds
	public static final String $VERIFICATION_INTERVAL_WD = "${" + PARAM_VERIFICATION_INTERVAL + ":" + CoreDefaults.DEFAULT_VERIFICATION_INTERVAL + "}";

    public static final String REQUEST_PARAM_PAGE = "page";
	public static final String REQUEST_PARAM_ITEM_PER_PAGE = "item_per_page";
	public static final String REQUEST_PARAM_DIRECTION = "direction";
	public static final String REQUEST_PARAM_SORT_FIELD = "sort_field";
	public static final String REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";

	public static final String COAP_SERVER_ADDRESS_ENABLED = "coap.server.enabled";
	public static final String $COAP_SERVER_ADDRESS_ENABLED = "${" + COAP_SERVER_ADDRESS_ENABLED + ":false}";
	public static final String COAP_SERVER_ADDRESS = "coap.server.address";
	public static final String $COAP_SERVER_ADDRESS = "${" + COAP_SERVER_ADDRESS + ":0.0.0.0}";
	public static final String COAP_SERVER_PORT = "coap.server.port";
	public static final String $COAP_SERVER_PORT = "${" + COAP_SERVER_PORT + ":0}";
	
	public static final long CONVERSION_MILLISECOND_TO_SECOND = 1000;
	public static final long CONVERSION_MILLISECOND_TO_MINUTE = 60000;

	public static final String SORT_ORDER_ASCENDING = "ASC";
	public static final String SORT_ORDER_DESCENDING = "DESC";
	
	public static final String SORT_FIELD_PRIORITY = "priority";

	public static final String SERVICEREGISTRY_PING_SCHEDULED = "ping_scheduled";
	public static final String $SERVICEREGISTRY_PING_SCHEDULED_WD = "${" + SERVICEREGISTRY_PING_SCHEDULED + ":" + CoreDefaults.DEFAULT_SERVICEREGISTRY_PING_SCHEDULED + "}";
	public static final String SERVICEREGISTRY_PING_INTERVAL = "ping_interval";
	public static final String $SERVICEREGISTRY_PING_INTERVAL_WD = "${" + SERVICEREGISTRY_PING_INTERVAL + ":" + CoreDefaults.DEFAULT_SERVICEREGISTRY_PING_INTERVAL_MINUTES + "}";
	public static final String SERVICEREGISTRY_PING_TIMEOUT = "ping_timeout";
	public static final String $SERVICEREGISTRY_PING_TIMEOUT_WD = "${" + SERVICEREGISTRY_PING_TIMEOUT + ":" + CoreDefaults.DEFAULT_SERVICEREGISTRY_PING_TIMEOUT_MILLISECONDS + "}";
	public static final String SERVICEREGISTRY_TTL_SCHEDULED = "ttl_scheduled";
	public static final String $SERVICEREGISTRY_TTL_SCHEDULED_WD = "${" + SERVICEREGISTRY_TTL_SCHEDULED + ":" + CoreDefaults.DEFAULT_SERVICEREGISTRY_TTL_SCHEDULED + "}";
	public static final String SERVICEREGISTRY_TTL_INTERVAL = "ttl_interval";
	public static final String $SERVICEREGISTRY_TTL_INTERVAL_WD = "${" + SERVICEREGISTRY_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_SERVICEREGISTRY_TTL_INTERVAL_MINUTES + "}";

	public static final String SYSTEMREGISTRY_PING_TIMEOUT = "ping_timeout";
	public static final String $SYSTEMREGISTRY_PING_TIMEOUT_WD =
			"${" + SYSTEMREGISTRY_PING_TIMEOUT + ":" + CoreDefaults.DEFAULT_SYSTEMREGISTRY_PING_TIMEOUT_MILLISECONDS + "}";

	public static final String AUTHORIZATION_IS_EVENTHANDLER_PRESENT = "eventhandler_is_present";
	public static final String $AUTHORIZATION_IS_EVENTHANDLER_PRESENT_WD = "${" + AUTHORIZATION_IS_EVENTHANDLER_PRESENT + ":" + CoreDefaults.DEFAULT_AUTHORIZATION_IS_EVENTHANDLER_PRESENT + "}";
	
    public static final String ORCHESTRATOR_USE_FLEXIBLE_STORE = "use_flexible_store";
    public static final String $ORCHESTRATOR_USE_FLEXIBLE_STORE_WD = "${" + ORCHESTRATOR_USE_FLEXIBLE_STORE + ":" + CoreDefaults.DEFAULT_ORCHESTRATOR_USE_FLEXIBLE_STORE + "}";
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
	
	public static final String PING_MONITOR = "pingMonitor";

	public static final int TOP_PRIORITY = 1;
	
	public static final String QOSMANAGER = "qosManager";
	public static final String QOS_ENABLED = "enable_qos";
	public static final String $QOS_ENABLED_WD = "${" + QOS_ENABLED + ":" + CoreDefaults.DEFAULT_QOS_ENABLED + "}";
	public static final String QOS_RESERVATION_CHECK_INTERVAL = "qos_reservation_check_interval"; // in seconds
	public static final String $QOS_RESERVATION_CHECK_INTERVAL_WD = "${" + QOS_RESERVATION_CHECK_INTERVAL + ":" + CoreDefaults.DEFAULT_QOS_RESERVATION_CHECK_INTERVAL + "}";
	public static final String QOS_RESERVATION_TEMP_LOCK_DURATION = "qos_reservation_temp_lock_duration"; // in seconds
	public static final String $QOS_RESERVATION_TEMP_LOCK_DURATION_WD = "${" + QOS_RESERVATION_TEMP_LOCK_DURATION + ":" + CoreDefaults.DEFAULT_QOS_TEMPORARY_LOCK_DURATION + "}";
	public static final String QOS_MAX_RESERVATION_DURATION = "qos_maximum_reservation_duration"; // in seconds
	public static final String $QOS_MAX_RESERVATION_DURATION_WD = "${" + QOS_MAX_RESERVATION_DURATION + ":" + CoreDefaults.DEFAULT_QOS_MAX_RESERVATION_DURATION + "}";
	public static final String QOS_PING_MEASUREMENT_CACHE_THRESHOLD = "qos_ping_measurement_cache_threshold"; // in seconds
	public static final String $QOS_PING_MEASUREMENT_CACHE_THRESHOLD_WD = "${" + QOS_PING_MEASUREMENT_CACHE_THRESHOLD + ":" + CoreDefaults.DEFAULT_QOS_PING_MEASUREMENT_CACHE_THRESHOLD + "}";
	public static final String QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT = "qos_not_measured_system_verify_result"; // result of verify if a system has no measurement records
	public static final String $QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT = "${" + QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT + ":" + CoreDefaults.DEFAULT_QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT + "}";
	public static final String $QOS_DEFAULT_REFERENCE_MIN_RESPONSE_TIME_WD = "${default.min_response_time:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_MIN_RESPONSE_TIME + "}";
	public static final String $QOS_DEFAULT_REFERENCE_MAX_RESPONSE_TIME_WD = "${default.max_response_time:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_MAX_RESPONSE_TIME + "}";
	public static final String $QOS_DEFAULT_REFERENCE_MEAN_RESPONSE_TIME_WITH_TIMEOUT_WD = "${default.mean_response_time_with_timeout:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT + "}";
	public static final String $QOS_DEFAULT_REFERENCE_MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT_WD = "${default.mean_response_time_without_timeout:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT + "}";
	public static final String $QOS_DEFAULT_REFERENCE_JITTER_WITH_TIMEOUT_WD = "${default.jitter_with_timeout:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_JITTER_WITHOUT_TIMEOUT + "}";
	public static final String $QOS_DEFAULT_REFERENCE_JITTER_WITHOUT_TIMEOUT_WD = "${default.jitter_without_timeout:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_JITTER_WITHOUT_TIMEOUT + "}";
	public static final String $QOS_DEFAULT_REFERENCE_LOST_PET_MEASUREMENT_PERCENT_WD = "${default.lost_per_measurement_percent:" + CoreDefaults.DEFAULT_QOS_DEFAULT_REFERENCE_LOST_PER_MEASUREMENT_PERCENT + "}";
	public static final String QOS_ENABLED_RELAY_TASK = "enable_qos_relay_task";
	public static final String $QOS_ENABLED_RELAY_TASK_WD =  "${" + QOS_ENABLED_RELAY_TASK + ":" + CoreDefaults.DEFAULT_QOS_ENABLED_RELAY_TASK + "}";
	public static final String QOS_IS_GATEKEEPER_PRESENT = "gatekeeper_is_present";
	public static final String $QOS_IS_GATEKEEPER_PRESENT_WD = "${" + QOS_IS_GATEKEEPER_PRESENT + ":" + CoreDefaults.DEFAULT_QOS_IS_GATEKEEPER_PRESENT + "}";
	public static final String QOS_MONITOR_PROVIDER_TYPE = "monitor_provider_type";
	public static final String $QOS_MONITOR_PROVIDER_TYPE_WD = "${" + QOS_MONITOR_PROVIDER_TYPE + ":" + CoreDefaults.DEFAULT_QOS_MONITOR_PROVIDER_TYPE + "}";
	public static final String QOS_MONITOR_PROVIDER_NAME = "monitor_provider_name";
	public static final String $QOS_MONITOR_PROVIDER_NAME_WD = "${" + QOS_MONITOR_PROVIDER_NAME + ":" + CoreDefaults.DEFAULT_QOS_MONITOR_PROVIDER_NAME + "}";
	public static final String QOS_MONITOR_PROVIDER_ADDRESS = "monitor_provider_address";
	public static final String $QOS_MONITOR_PROVIDER_ADDRESS_WD = "${" + QOS_MONITOR_PROVIDER_ADDRESS + ":" + CoreDefaults.DEFAULT_QOS_MONITOR_PROVIDER_ADDRESS + "}";
	public static final String QOS_MONITOR_PROVIDER_PORT = "monitor_provider_port";
	public static final String $QOS_MONITOR_PROVIDER_PORT_WD = "${" + QOS_MONITOR_PROVIDER_PORT + ":" + CoreDefaults.DEFAULT_QOS_MONITOR_PROVIDER_PORT + "}";
	public static final String QOS_MONITOR_PROVIDER_PATH = "monitor_provider_path";
	public static final String $QOS_MONITOR_PROVIDER_PATH_WD = "${" + QOS_MONITOR_PROVIDER_PATH + ":" + CoreDefaults.DEFAULT_QOS_MONITOR_PROVIDER_PATH + "}";
	public static final String QOS_MONITOR_PROVIDER_SECURE = "monitor_provider_secure";
	public static final String $QOS_MONITOR_PROVIDER_SECURE_WD = "${" + QOS_MONITOR_PROVIDER_SECURE + ":" + CoreDefaults.DEFAULT_QOS_MONITOR_PROVIDER_SECURE + "}";

	public static final String NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS = "no_gatekeeper_relay_request_handler_workers";
	public static final String $NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS_WD = "${" + NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS + ":" + 
																				 CoreDefaults.DEFAULT_NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS + "}";
	
	public static final String GATEWAY_INACTIVE_BRIDGE_TIMEOUT = "inactive_gateway_bridge_timeout";
	public static final String $GATEWAY_INACTIVE_BRIDGE_TIMEOUT_WD = "${" + GATEWAY_INACTIVE_BRIDGE_TIMEOUT + ":" + CoreDefaults.DEFAULT_GATEWAY_INACTIVE_BRIDGE_TIMEOUT + "}";
	public static final String GATEWAY_SOCKET_TIMEOUT = "gateway_socket_timeout";
	public static final String $GATEWAY_SOCKET_TIMEOUT_WD = "${" + GATEWAY_SOCKET_TIMEOUT + ":" + CoreDefaults.DEFAULT_GATEWAY_SOCKET_TIMEOUT + "}";
	public static final String GATEWAY_MIN_PORT = "min_port";
	public static final String $GATEWAY_MIN_PORT_WD = "${" + GATEWAY_MIN_PORT + ":" + CoreDefaults.DEFAULT_GATEWAY_MIN_PORT + "}";
	public static final String GATEWAY_MAX_PORT = "max_port";
	public static final String $GATEWAY_MAX_PORT_WD = "${" + GATEWAY_MAX_PORT + ":" + CoreDefaults.DEFAULT_GATEWAY_MAX_PORT + "}";
	public static final String GATEWAY_PROVIDER_SIDE_MAX_REQUEST_PER_SOCKET = "provider_side_max_request_per_socket";
	public static final String $GATEWAY_PROVIDER_SIDE_MAX_REQUEST_PER_SOCKET = "${" + GATEWAY_PROVIDER_SIDE_MAX_REQUEST_PER_SOCKET + ":" + CoreDefaults.DEFAULT_GATEWAY_PROVIDER_SIDE_MAX_REQUEST_PER_SOCKET + "}";
	
	public static final String GATEWAY_ACTIVE_SESSION_MAP = "activeSessions";
	public static final String GATEWAY_AVAILABLE_PORTS_QUEUE = "availableQueue";
	public static final String GATEWAY_ACTIVE_CONSUMER_SIDE_SOCKET_THREAD_MAP = "activeConsumerSideSocketThreads";
	public static final String GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP = "activeProviderSideSocketThreadHandlers";
	
	public static final String CHOREOGRAPHER_IS_GATEKEEPER_PRESENT = "gatekeeper_is_present";
	public static final String $CHOREOGRAPHER_IS_GATEKEEPER_PRESENT_WD = "${" + CHOREOGRAPHER_IS_GATEKEEPER_PRESENT + ":" + CoreDefaults.DEFAULT_CHOREOGRAPHER_IS_GATEKEEPER_PRESENT + "}";
	
	public static final String EVENTHANDLER_TTL_SCHEDULED = "event_handler_ttl_scheduled";
	public static final String $EVENTHANDLER_TTL_SCHEDULED_WD = "${" + EVENTHANDLER_TTL_SCHEDULED + ":" + CoreDefaults.DEFAULT_EVENTHANDLER_TTL_SCHEDULED + "}";
	public static final String EVENTHANDLER_TTL_INTERVAL = "event_handler_ttl_interval";
	public static final String $EVENTHANDLER_TTL_INTERVAL_WD = "${" + EVENTHANDLER_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_EVENTHANDLER_TTL_INTERVAL_MINUTES + "}";
	public static final String EVENTHANDLER_MAX_RETRY_CONNECT_AUTH = "event_handler_max_retry_connect_auth";
	public static final String $EVENTHANDLER_MAX_RETRY_CONNECT_AUTH_WD = "${" + EVENTHANDLER_MAX_RETRY_CONNECT_AUTH + ":" + CoreDefaults.DEFAULT_EVENTHANDLER_MAX_RETRY_CONNECT_AUTH + "}";
	public static final String EVENTHANDLER_RETRY_CONNECT_AUTH_INTERVAL_SEC = "event_handler_retry_connect_auth_interval_sec";
	public static final String $EVENTHANDLER_RETRY_CONNECT_AUTH_INTERVAL_SEC_WD = "${" + EVENTHANDLER_RETRY_CONNECT_AUTH_INTERVAL_SEC + ":" + CoreDefaults.DEFAULT_EVENTHANDLER_RETRY_CONNECT_AUTH_INTERVAL_SEC + "}";

	
	public static final String TIME_STAMP_TOLERANCE_SECONDS = "time_stamp_tolerance_seconds";
	public static final String $TIME_STAMP_TOLERANCE_SECONDS_WD = "${" + TIME_STAMP_TOLERANCE_SECONDS + ":" + CoreDefaults.DEFAULT_TIME_STAMP_TOLERANCE_SECONDS + "}";

	public static final String EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE = "SUBSCRIBER_AUTH_UPDATE";	
	public static final String EVENT_PUBLISHING_QUEUE = "eventPublishingQueue";
	public static final String EVENT_PUBLISHING_QUEUE_WATCHER_TASK = "eventPublishingQueueWatcherTask";
	public static final String EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS = "event_handler_max_express_subscribers";
	public static final String $EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS_WD = "${" + EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS + ":" + CoreDefaults.DEFAULT_EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS + "}";
	public static final String EVENT_PUBLISHING_EXPRESS_EXECUTOR = "eventPublishingExpressExecutor";

	public static final String PING_TTL_INTERVAL = "ping_ttl_interval_minutes";
	public static final String $PING_TTL_INTERVAL_WD = "${" + PING_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_PING_TTL_INTERVAL_MINUTES + "}";
	
	public static final String CLOUD_PING_TTL_INTERVAL = "cloud_ping_ttl_interval_minutes";
	public static final String $CLOUD_PING_TTL_INTERVAL_WD = "${" + CLOUD_PING_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_CLOUD_PING_TTL_INTERVAL_MINUTES + "}";
	
	public static final String RELAY_ECHO_TTL_INTERVAL = "relay_echo_ttl_interval_minutes";
	public static final String $RELAY_ECHO_TTL_INTERVAL_WD = "${" + RELAY_ECHO_TTL_INTERVAL + ":" + CoreDefaults.DEFAULT_RELAY_ECHO_TTL_INTERVAL_MINUTES + "}";
	
	public static final String RELAY_TEST_BAD_GATEWAY_RETRY_MIN = "relay.test.bad_gateway_retry_min";
	public static final String $RELAY_TEST_BAD_GATEWAY_RETRY_MIN_WD = "${" + RELAY_TEST_BAD_GATEWAY_RETRY_MIN + ":" + CoreDefaults.DEFAULT_RELAY_TEST_BAD_GATEWAY_RETRY_MIN + "}";
	public static final String RELAY_TEST_TIME_TO_REPEAT = "relay.test.time_to_repeat";
	public static final String $RELAY_TEST_TIME_TO_REPEAT_WD = "${" + RELAY_TEST_TIME_TO_REPEAT + ":" + CoreDefaults.DEFAULT_RELAY_TEST_TIME_TO_REPEAT + "}";
	public static final String RELAY_TEST_TIMEOUT = "rely.test.timeout"; 
	public static final String $RELAY_TEST_TIMEOUT_WD = "${" + RELAY_TEST_TIMEOUT + ":" + CoreDefaults.DEFAULT_RELAY_TEST_TIMEOUT + "}";
	public static final String RELAY_TEST_MESSAGE_SIZE = "relay.test.messsage_size";
	public static final String $RELAY_TEST_MESSAGE_SIZE_WD = "${" + RELAY_TEST_MESSAGE_SIZE + ":" + CoreDefaults.DEFAULT_RELAY_TEST_MESSAGE_SIZE + "}";
	public static final String RELAY_TEST_LOG_MEASUREMENTS_IN_DB = "relay.test.log_measurements_in_db";
	public static final String $RELAY_TEST_LOG_MEASUREMENTS_IN_DB_WD = "${" + RELAY_TEST_LOG_MEASUREMENTS_IN_DB + ":" + CoreDefaults.DEFAULT_RELAY_TEST_LOG_MEASUREMENTS_IN_DB + "}";
	
	public static final String CHOREOGRAPHER_MAX_PLAN_ITERATION = "max_plan_iteration";
	public static final String $CHOREOGRAPHER_MAX_PLAN_ITERATION_WD = "${" + CHOREOGRAPHER_MAX_PLAN_ITERATION + ":" + CoreDefaults.DEFAULT_CHOREOGRAPHER_MAX_PLAN_ITERATION + "}";
	
	public static final String CERTIFICATE_FORMAT = "X.509";

	// Translator-Fiware
	public static final String FIWARE_SERVER_HOST = "fiware.server.host";
	public static final String $FIWARE_SERVER_HOST = "${" + FIWARE_SERVER_HOST + ":}";
	public static final String FIWARE_SERVER_PORT = "fiware.server.port";
	public static final String $FIWARE_SERVER_PORT = "${" + FIWARE_SERVER_PORT + ":0}";

	// MQTT
	public static final String MQTT_BROKER_ENABLED = "mqtt.broker.enabled";
	public static final String $MQTT_BROKER_ENABLED = "${" + MQTT_BROKER_ENABLED + ":false}";
	public static final String MQTT_BROKER_ADDRESS = "mqtt.broker.address";
	public static final String $MQTT_BROKER_ADDRESS = "${" + MQTT_BROKER_ADDRESS + ":0.0.0.0}";
	public static final String MQTT_BROKER_PORT = "mqtt.broker.port";
	public static final String $MQTT_BROKER_PORT = "${" + MQTT_BROKER_PORT + ":0}";
	public static final String MQTT_BROKER_USERNAME = "mqtt.broker.username";
	public static final String $MQTT_BROKER_USERNAME = "${" + MQTT_BROKER_USERNAME + ":}";
	public static final String MQTT_BROKER_PASSWORD = "mqtt.broker.password";
	public static final String $MQTT_BROKER_PASSWORD = "${" + MQTT_BROKER_PASSWORD + ":}";
	public static final String MQTT_BROKER_CAFILE = "mqtt.broker.cafile";
	public static final String $MQTT_BROKER_CAFILE = "${" + MQTT_BROKER_CAFILE + ":}";
	public static final String MQTT_BROKER_CERTFILE = "mqtt.broker.certfile";
	public static final String $MQTT_BROKER_CERTFILE = "${" + MQTT_BROKER_CERTFILE + ":}";
	public static final String MQTT_BROKER_KEYFILE = "mqtt.broker.keyfile";
	public static final String $MQTT_BROKER_KEYFILE = "${" + MQTT_BROKER_KEYFILE + ":}";

    //=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreCommonConstants() {
		throw new UnsupportedOperationException();
	}
}
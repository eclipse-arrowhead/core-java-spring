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

public class CoreDefaults {

	//=================================================================================================
	// members

	public static final int VARCHAR_LOG = 100;
	public static final int VARCHAR_BASIC = 255;
	public static final int VARCHAR_EXTENDED = 2047;
	
	public static final boolean DEFAULT_LOG_ALL_REQUEST_AND_RESPONSE = false;
	public static final boolean DEFAULT_USE_STRICT_SERVICE_INTF_NAME_VERIFIER = false;
	public static final boolean DEFAULT_USE_STRICT_SERVICE_DEFINITION_VERIFIER = false;
	public static final boolean DEFAULT_USE_NETWORK_ADDRESS_DETECTOR = false;
	public static final String DEFAULT_FILTER_PROXY_ADDRESSES = "";
	public static final boolean DEFAULT_ALLOW_SELF_ADDRESSING = false;
	public static final boolean DEFAULT_ALLOW_NON_ROUTABLE_ADDRESSING = false;
	public static final int DEFAULT_URI_CRAWLER_INTERVAL = 30;
	public static final int DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES = -1; // never expires
	public static final int DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES_WITH_QOS_ENABLED = 60; // when QoS enabled, we want to make sure that tokens expires eventually
	
	public static final boolean DEFAULT_SERVICEREGISTRY_PING_SCHEDULED = false;
	public static final int DEFAULT_SERVICEREGISTRY_PING_INTERVAL_MINUTES = 60;
	public static final int DEFAULT_SERVICEREGISTRY_PING_TIMEOUT_MILLISECONDS = 5000;
	public static final boolean DEFAULT_SERVICEREGISTRY_TTL_SCHEDULED = false;
	public static final int DEFAULT_SERVICEREGISTRY_TTL_INTERVAL_MINUTES = 13;

	public static final int DEFAULT_SYSTEMREGISTRY_PING_INTERVAL_MINUTES = 60;
	public static final int DEFAULT_SYSTEMREGISTRY_PING_TIMEOUT_MILLISECONDS = 5000;

	public static final String DEFAULT_OWN_CLOUD_OPERATOR = "default-operator";
	public static final String DEFAULT_OWN_CLOUD_NAME = "default-insecure-cloud";
	
	public static final String DEFAULT_REQUEST_PARAM_DIRECTION_VALUE = CoreCommonConstants.SORT_ORDER_ASCENDING;
	
	public static final boolean DEFAULT_AUTHORIZATION_IS_EVENTHANDLER_PRESENT = false;
	
	public static final boolean DEFAULT_ORCHESTRATOR_USE_FLEXIBLE_STORE = false;
	public static final boolean DEFAULT_ORCHESTRATOR_IS_GATEKEEPER_PRESENT = false;

	public static final int DEFAULT_RELAY_CHECK_INTERVAL = 9;
	public static final int DEFAULT_NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS = 50;
	
	public static final boolean DEFAULT_GATEKEEPER_IS_GATEWAY_PRESENT = false;
	public static final boolean DEFAULT_GATEKEEPER_IS_GATEWAY_MANDATORY = false;
	
	public static final int DEFAULT_GATEWAY_INACTIVE_BRIDGE_TIMEOUT = 60; // in seconds
	public static final int DEFAULT_GATEWAY_SOCKET_TIMEOUT = 30000;
	public static final int DEFAULT_GATEWAY_MIN_PORT = 8000;
	public static final int DEFAULT_GATEWAY_MAX_PORT = 8100;
	public static final int DEFAULT_GATEWAY_PROVIDER_SIDE_MAX_REQUEST_PER_SOCKET = 50;
	
	public static final boolean DEFAULT_CHOREOGRAPHER_IS_GATEKEEPER_PRESENT = false;

	public static final boolean DEFAULT_EVENTHANDLER_TTL_SCHEDULED = false;
	public static final int DEFAULT_EVENTHANDLER_TTL_INTERVAL_MINUTES = 17;
	public static final int DEFAULT_EVENTHANDLER_MAX_RETRY_CONNECT_AUTH = 3;
	public static final int DEFAULT_EVENTHANDLER_RETRY_CONNECT_AUTH_INTERVAL_SEC = 10;

	public static final long DEFAULT_TIME_STAMP_TOLERANCE_SECONDS = 120;
	public static final int DEFAULT_EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS = 10;
	
	public static final int DEFAULT_PING_TTL_INTERVAL_MINUTES = 10;
	public static final int DEFAULT_CLOUD_PING_TTL_INTERVAL_MINUTES = 10;
	public static final int DEFAULT_RELAY_ECHO_TTL_INTERVAL_MINUTES = 10;

	public static final boolean DEFAULT_QOS_ENABLED = false;
	public static final int DEFAULT_QOS_RESERVATION_CHECK_INTERVAL = 60; // in seconds
	public static final int DEFAULT_QOS_TEMPORARY_LOCK_DURATION = 60; // in seconds
	public static final int DEFAULT_QOS_MAX_RESERVATION_DURATION = 3600; // in seconds
	public static final int DEFAULT_QOS_PING_MEASUREMENT_CACHE_THRESHOLD = 600; // in seconds
	public static final boolean DEFAULT_QOS_NOT_MEASURED_SYSTEM_VERIFY_RESULT = true;
	public static final int DEFAULT_QOS_DEFAULT_REFERENCE_MIN_RESPONSE_TIME = 30;
	public static final int DEFAULT_QOS_DEFAULT_REFERENCE_MAX_RESPONSE_TIME = 34;
	public static final int DEFAULT_QOS_DEFAULT_REFERENCE_MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT = 32;
	public static final int DEFAULT_QOS_DEFAULT_REFERENCE_JITTER_WITHOUT_TIMEOUT = 2;
	public static final int DEFAULT_QOS_DEFAULT_REFERENCE_LOST_PER_MEASUREMENT_PERCENT = 0;
	public static final boolean DEFAULT_QOS_ENABLED_RELAY_TASK = false;
	public static final boolean DEFAULT_QOS_IS_GATEKEEPER_PRESENT = false;
	public static final String DEFAULT_QOS_MONITOR_PROVIDER_TYPE = "defaultexternal";
	public static final String DEFAULT_QOS_MONITOR_PROVIDER_NAME = "qosmeasurer";
	public static final String DEFAULT_QOS_MONITOR_PROVIDER_ADDRESS = "127.0.0.1";
	public static final int DEFAULT_QOS_MONITOR_PROVIDER_PORT = 8888;
	public static final String DEFAULT_QOS_MONITOR_PROVIDER_PATH = "/ping-icmp";
	public static final boolean DEFAULT_QOS_MONITOR_PROVIDER_SECURE = true;

	public static final int DEFAULT_RELAY_TEST_BAD_GATEWAY_RETRY_MIN = 30; //in minutes
	public static final byte DEFAULT_RELAY_TEST_TIME_TO_REPEAT = 35;
	public static final int DEFAULT_RELAY_TEST_TIMEOUT = 5000; // in milliseconds
	public static final int DEFAULT_RELAY_TEST_MESSAGE_SIZE = 2048; // in bytes
	public static final boolean DEFAULT_RELAY_TEST_LOG_MEASUREMENTS_IN_DB = true;
	
	public static final long DEFAULT_CHOREOGRAPHER_MAX_PLAN_ITERATION = Long.MAX_VALUE;
	
	public static final int DEFAULT_VERIFICATION_INTERVAL = 3600; // in seconds

    //=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private CoreDefaults() {
		throw new UnsupportedOperationException();
	}
}
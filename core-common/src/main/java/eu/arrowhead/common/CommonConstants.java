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

import eu.arrowhead.common.core.CoreSystemService;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.util.List;

public class CommonConstants {

	//=================================================================================================
	// members
	
	public static final String BASE_PACKAGE = "eu.arrowhead";

	public static final String CORE_SYSTEM_AUTHORIZATION = "Authorization";
	public static final String CORE_SYSTEM_CERTIFICATE_AUTHORITY = "Certificate Authority";
	public static final String CORE_SYSTEM_CHOREOGRAPHER = "Choreographer";
	public static final String CORE_SYSTEM_EVENT_HANDLER = "Event Handler";
	public static final String CORE_SYSTEM_DATAMANAGER  = "DataManager";
	public static final String CORE_SYSTEM_GATEKEEPER = "Gatekeeper";
	public static final String CORE_SYSTEM_GATEWAY = "Gateway";
	public static final String CORE_SYSTEM_ORCHESTRATOR = "Orchestrator";
	public static final String CORE_SYSTEM_SERVICE_REGISTRY = "Service Registry";
	public static final String CORE_SYSTEM_QOS_MONITOR = "Quality of Service Monitor";
    public static final String CORE_SYSTEM_TRANSLATOR = "Translator";

	public static final String CORE_SYSTEM_SYSTEM_REGISTRY = "System Registry";
	public static final String CORE_SYSTEM_DEVICE_REGISTRY = "Device Registry";
	public static final String CORE_SYSTEM_ONBOARDING = "Onboarding Controller";

	public static final String CORE_SERVICE_AUTH_TOKEN_GENERATION = "token-generation";
	public static final String CORE_SERVICE_AUTH_PUBLIC_KEY = "auth-public-key";
	public static final String CORE_SERVICE_AUTH_CONTROL_INTRA = "authorization-control-intra";
	public static final String CORE_SERVICE_AUTH_CONTROL_INTER = "authorization-control-inter";
	public static final String CORE_SERVICE_AUTH_CONTROL_SUBSCRIPTION = "authorization-control-subscription";
	
	public static final String CORE_SERVICE_ORCH_PROCESS = "orchestration-service";
	public static final String CORE_SERVICE_ORCH_QOS_ENABLED = "orchestration-qos-enabled";
	public static final String CORE_SERVICE_ORCH_QOS_RESERVATIONS = "orchestration-qos-reservations";
	public static final String CORE_SERVICE_ORCH_QOS_TEMPORARY_LOCK = "orchestration-qos-temporary-lock";
	public static final String CORE_SERVICE_ORCH_QOS_CONFIRM_RESERVATION = "orchestration-qos-confirm-reservation";
	
	public static final String CORE_SERVICE_GATEKEEPER_GSD = "global-service-discovery";
	public static final String CORE_SERVICE_GATEKEEPER_ICN = "inter-cloud-negotiations";
	public static final String CORE_SERVICE_GATEKEEPER_PULL_CLOUDS = "pull-clouds";
	public static final String CORE_SERVICE_GATEKEEPER_COLLECT_SYSTEM_ADDRESSES = "collect-system-addresses";
	public static final String CORE_SERVICE_GATEKEEPER_COLLECT_ACCESS_TYPES = "collect-access-types";
	public static final String CORE_SERVICE_GATEKEEPER_RELAY_TEST = "gatekeeper-relay-test";
	public static final String CORE_SERVICE_GATEKEEPER_GET_CLOUD = "gatekeeper-get-cloud";
	
	public static final String CORE_SERVICE_GATEWAY_PUBLIC_KEY = "gw-public-key";
	public static final String CORE_SERVICE_GATEWAY_CONNECT_CONSUMER = "gw-connect-consumer";
	public static final String CORE_SERVICE_GATEWAY_CONNECT_PROVIDER = "gw-connect-provider";

	public static final String CORE_SERVICE_EVENT_HANDLER_PUBLISH = "event-publish";
	public static final String CORE_SERVICE_EVENT_HANDLER_SUBSCRIBE = "event-subscribe";
	public static final String CORE_SERVICE_EVENT_HANDLER_UNSUBSCRIBE = "event-unsubscribe";
	public static final String CORE_SERVICE_EVENT_HANDLER_PUBLISH_AUTH_UPDATE = "event-publish-auth-update";

	public static final String CORE_SERVICE_DATAMANAGER_PROXY = "proxy";
	public static final String CORE_SERVICE_DATAMANAGER_HISTORIAN = "historian";

	public static final String CORE_SERVICE_CHOREOGRAPHER_PROCESS = "choreographer-service";

	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_SIGN = "ca-sign";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_LIST_CERTIFICATES = "ca-list-certificates";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_CHECK_CERTIFICATE = "ca-check-certificate";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_REVOKE_CERTIFICATE = "ca-revoke-certificate";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_LIST_TRUSTED_KEYS = "ca-list-trusted-keys";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_CHECK_TRUSTED_KEY = "ca-check-trusted-key";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_ADD_TRUSTED_KEY = "ca-add-trusted-key";
	public static final String CORE_SERVICE_CERTIFICATE_AUTHORITY_DELETE_TRUSTED_KEY = "ca-delete-trusted-key";
	
	public static final String CORE_SERVICE_QOS_MONITOR_INTRA_PING_MEASUREMENT = "qos-monitor-intra-ping-measurement";
	public static final String CORE_SERVICE_QOS_MONITOR_INTRA_PING_MEDIAN_MEASUREMENT = "qos-monitor-intra-ping-median-measurement";
	public static final String CORE_SERVICE_QOS_MONITOR_INTER_DIRECT_PING_MEASUREMENT = "qos-monitor-inter-direct-ping-measurement";
	public static final String CORE_SERVICE_QOS_MONITOR_INTER_RELAY_ECHO_MEASUREMENT = "qos-monitor-inter-relay-echo-measurement";
	public static final String CORE_SERVICE_QOS_MONITOR_PUBLIC_KEY = "qos-monitor-public-key";
	public static final String CORE_SERVICE_QOS_MONITOR_JOIN_RELAY_TEST = "qos-monitor-join-relay-test";
	public static final String CORE_SERVICE_QOS_MONITOR_INIT_RELAY_TEST = "qos-monitor-init-relay-test";

	public static final String CORE_SERVICE_SERVICE_REGISTRY_REGISTER = "service-register";
	public static final String CORE_SERVICE_SERVICE_REGISTRY_UNREGISTER = "service-unregister";

	public static final String CORE_SERVICE_SYSTEM_REGISTRY_REGISTER = "system-register";
	public static final String CORE_SERVICE_SYSTEM_REGISTRY_UNREGISTER = "system-unregister";
	public static final String CORE_SERVICE_SYSTEM_REGISTRY_ONBOARDING_WITH_NAME = "system-onboarding-with-name";
	public static final String CORE_SERVICE_SYSTEM_REGISTRY_ONBOARDING_WITH_CSR = "system-onboarding-with-csr";

	public static final String CORE_SERVICE_DEVICE_REGISTRY_REGISTER = "device-register";
	public static final String CORE_SERVICE_DEVICE_REGISTRY_UNREGISTER = "device-unregister";
	public static final String CORE_SERVICE_DEVICE_REGISTRY_ONBOARDING_WITH_NAME = "device-onboarding-with-name";
	public static final String CORE_SERVICE_DEVICE_REGISTRY_ONBOARDING_WITH_CSR = "device-onboarding-with-csr";

	public static final String CORE_SERVICE_ONBOARDING_WITH_CERTIFICATE_AND_NAME = "onboarding-with-certificate-and-name";
	public static final String CORE_SERVICE_ONBOARDING_WITH_CERTIFICATE_AND_CSR = "onboarding-with-certificate-and-csr";
	public static final String CORE_SERVICE_ONBOARDING_WITH_SHARED_SECRET_AND_NAME = "onboarding-with-shared-secret-and-name";
	public static final String CORE_SERVICE_ONBOARDING_WITH_SHARED_SECRET_AND_CSR = "onboarding-with-shared-secret-and-csr";

    public static final String CORE_SERVICE_TRANSLATOR = "translation-service";
    public static final String CORE_SERVICE_TRANSLATOR_FIWARE = "translation-fiware-service";
    public static final String CORE_SERVICE_TRANSLATOR_PLUGIN = "translation-plugin-service";

	public static final String COMMON_FIELD_NAME_ID = "id";
	
	public static final String ARROWHEAD_CONTEXT = "arrowheadContext";
	public static final String SERVER_COMMON_NAME = "server.common.name";
	public static final String SERVER_PUBLIC_KEY = "server.public.key";
	public static final String SERVER_PRIVATE_KEY = "server.private.key";
	public static final String SERVER_CERTIFICATE = "server.certificate";

	public static final String HTTPS = "https";
	public static final String HTTP = "http";
	public static final String JSON = "JSON";
	public static final String XML = "XML";
	public static final String HTTP_SECURE_JSON = HTTP + "-SECURE-" + JSON; 
	public static final String HTTP_INSECURE_JSON = HTTP + "-INSECURE-" + JSON;
	public static final String UNKNOWN_ORIGIN = "<unknown>";
	
	public static final String SERVICE_REGISTRY_ADDRESS = "sr_address";
	public static final String $SERVICE_REGISTRY_ADDRESS_WD = "${" + SERVICE_REGISTRY_ADDRESS + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_ADDRESS + "}";
	public static final String SERVICE_REGISTRY_PORT = "sr_port";
	public static final String $SERVICE_REGISTRY_PORT_WD = "${" + SERVICE_REGISTRY_PORT + ":" + Defaults.DEFAULT_SERVICE_REGISTRY_PORT + "}";
	
	public static final String ONBOARDING_URI = "/onboarding";
	public static final String ONBOARDING_AUTH_WITH_CERTIFICATE_URI = "/certificate";
	public static final String ONBOARDING_AUTH_WITH_SHARED_SECRET_URI = "/sharedsecret";
	public static final String ONBOARDING_WITH_NAME_URI = "/name";
	public static final String ONBOARDING_WITH_CSR_URI = "/csr";
	public static final String OP_ONBOARDING_WITH_CERTIFICATE_AND_NAME = ONBOARDING_AUTH_WITH_CERTIFICATE_URI + ONBOARDING_WITH_NAME_URI;
	public static final String OP_ONBOARDING_WITH_CERTIFICATE_AND_CSR = ONBOARDING_AUTH_WITH_CERTIFICATE_URI + ONBOARDING_WITH_CSR_URI;
	public static final String OP_ONBOARDING_WITH_SHARED_SECRET_AND_NAME = ONBOARDING_AUTH_WITH_SHARED_SECRET_URI + ONBOARDING_WITH_NAME_URI;
	public static final String OP_ONBOARDING_WITH_SHARED_SECRET_AND_CSR = ONBOARDING_AUTH_WITH_SHARED_SECRET_URI + ONBOARDING_WITH_CSR_URI;

	public static final String SERVICE_REGISTRY_URI = "/serviceregistry";
	public static final String OP_SERVICE_REGISTRY_REGISTER_URI = "/register";
	public static final String OP_SERVICE_REGISTRY_REGISTER_SYSTEM_URI = "/register-system";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_URI = "/unregister";
	public static final String OP_SERVICE_REGISTRY_QUERY_URI = "/query";	
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME = "system_name";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS = "address";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT = "port";
	public static final String OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";

	public static final String SYSTEM_REGISTRY_URI = "/systemregistry";
	public static final String OP_SYSTEM_REGISTRY_REGISTER_URI = "/register";
	public static final String OP_SYSTEM_REGISTRY_UNREGISTER_URI = "/unregister";
	public static final String OP_SYSTEM_REGISTRY_QUERY_URI = "/query";
	public static final String OP_SYSTEM_REGISTRY_ONBOARDING_WITH_NAME_URI = ONBOARDING_WITH_NAME_URI;
	public static final String OP_SYSTEM_REGISTRY_ONBOARDING_WITH_CSR_URI = ONBOARDING_WITH_CSR_URI;
	public static final String OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME = "system_name";
	public static final String OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS = "address";
	public static final String OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT = "port";

	public static final String DEVICE_REGISTRY_URI = "/deviceregistry";
	public static final String OP_DEVICE_REGISTRY_REGISTER_URI = "/register";
	public static final String OP_DEVICE_REGISTRY_UNREGISTER_URI = "/unregister";
	public static final String OP_DEVICE_REGISTRY_QUERY_URI = "/query";
	public static final String OP_DEVICE_REGISTRY_ONBOARDING_WITH_NAME_URI = ONBOARDING_WITH_NAME_URI;
	public static final String OP_DEVICE_REGISTRY_ONBOARDING_WITH_CSR_URI = ONBOARDING_WITH_CSR_URI;
	public static final String OP_DEVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_DEVICE_NAME = "device_name";
	public static final String OP_DEVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_MAC_ADDRESS = "mac_address";

	public static final String AUTHORIZATION_URI = "/authorization";
	public static final String OP_AUTH_TOKEN_URI = "/token";
	public static final String OP_AUTH_KEY_URI = "/publickey";
	public static final String OP_AUTH_INTRA_CHECK_URI = "/intracloud/check";
	public static final String OP_AUTH_INTER_CHECK_URI = "/intercloud/check";
	public static final String OP_AUTH_SUBSCRIPTION_CHECK_URI = "/subscription/check";

	public static final String CERTIFICATE_AUTHRORITY_URI = "/certificate-authority";
	public static final String OP_CA_MGMT_CERTIFICATES_URI = "/mgmt/certificates";
	public static final String OP_CA_MGMT_CERTIFICATES_URI_SUFFIX = "/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_CA_MGMT_TRUSTED_KEYS_URI = "/mgmt/keys";
	public static final String OP_CA_MGMT_TRUSTED_KEYS_URI_SUFFIX = "/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_CA_CHECK_CERTIFICATE_URI = "/checkCertificate";
	public static final String OP_CA_CHECK_TRUSTED_KEY_URI = "/checkTrustedKey";
	public static final String OP_CA_SIGN_CERTIFICATE_URI = "/sign";

	public static final String ORCHESTRATOR_URI = "/orchestrator";
	public static final String OP_ORCH_PROCESS_URI = "/orchestration";
	public static final String OP_ORCH_QOS_ENABLED_URI = "/qos_enabled";
	public static final String OP_ORCH_QOS_RESERVATIONS_URI = "/qos_reservations";
	public static final String OP_ORCH_QOS_TEMPORARY_LOCK_URI = "/qos_temporary_lock";
	
	public static final String ORCHESTRATION_FLAG_MATCHMAKING = "matchmaking";
	public static final String ORCHESTRATION_FLAG_METADATA_SEARCH = "metadataSearch";
	public static final String ORCHESTRATION_FLAG_ONLY_PREFERRED = "onlyPreferred";
	public static final String ORCHESTRATION_FLAG_PING_PROVIDERS = "pingProviders";
	public static final String ORCHESTRATION_FLAG_OVERRIDE_STORE = "overrideStore";
	public static final String ORCHESTRATION_FLAG_TRIGGER_INTER_CLOUD = "triggerInterCloud";
	public static final String ORCHESTRATION_FLAG_EXTERNAL_SERVICE_REQUEST = "externalServiceRequest";
	public static final String ORCHESTRATION_FLAG_ENABLE_INTER_CLOUD = "enableInterCloud";
	public static final String ORCHESTRATION_FLAG_ENABLE_QOS = "enableQoS";
	
	public static final String GATEKEEPER_URI = "/gatekeeper";
	public static final String OP_GATEKEEPER_GSD_SERVICE = "/init_gsd";
	public static final String OP_GATEKEEPER_ICN_SERVICE = "/init_icn";
	public static final String OP_GATEKEEPER_PULL_CLOUDS_SERVICE = "/pull_clouds";
	public static final String OP_GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_SERVICE = "/collect_system_addresses";
	public static final String OP_GATEKEEPER_COLLECT_ACCESS_TYPES_SERVICE = "/collect_access_types";
	public static final String OP_GATEKEEPER_RELAY_TEST_SERVICE = "/init_relay_test";
	public static final String OP_GATEKEEPER_GET_CLOUD_SERVICE = "/cloud/";
	public static final String OP_GATEKEEPER_GET_CLOUD_SERVICE_SUFFIX = "{operator}/{name}";

	public static final String CHOREOGRAPHER_URI = "/choreographer";
	public static final String OP_CHOREOGRAPHER_NOTIFY_STEP_DONE = "/notifyStepDone";

	public static final String GATEWAY_URI = "/gateway";
	public static final String OP_GATEWAY_KEY_URI = "/publickey";
	public static final String OP_GATEWAY_CONNECT_PROVIDER_URI = "/connect_provider";
	public static final String OP_GATEWAY_CONNECT_CONSUMER_URI = "/connect_consumer";
	
	public static final String EVENT_HANDLER_URI = "/eventhandler";
	public static final String OP_EVENT_HANDLER_PUBLISH = "/publish";
	public static final String OP_EVENT_HANDLER_SUBSCRIBE = "/subscribe";

	public static final String OP_EVENT_HANDLER_UNSUBSCRIBE = "/unsubscribe";
	public static final String OP_EVENT_HANDLER_PUBLISH_AUTH_UPDATE = "/publish/authupdate";

	public static final String QOS_MONITOR_URI = "/qosmonitor";
	public static final String OP_QOS_MONITOR_INTRA_PING_MEASUREMENT = "/measurements/intracloud/ping";
	public static final String OP_QOS_MONITOR_INTRA_PING_MEASUREMENT_SUFFIX = "/{" + COMMON_FIELD_NAME_ID + "}";
	public static final String OP_QOS_MONITOR_INTRA_PING_MEDIAN_MEASUREMENT = "/measurements/intracloud/ping_median/{attribute}";
	public static final String OP_QOS_MONITOR_INTER_DIRECT_PING_MEASUREMENT = "/measurements/intercloud/ping";
	public static final String OP_QOS_MONITOR_INTER_RELAY_ECHO_MEASUREMENT = "/measurements/intercloud/relay_echo";
	public static final String OP_QOS_MONITOR_KEY_URI = "/publickey";
	public static final String OP_QOS_MONITOR_JOIN_RELAY_TEST_URI = "/join_relay_test";
	public static final String OP_QOS_MONITOR_INIT_RELAY_TEST_URI = "/init_relay_test";

	public static final String DATAMANAGER_URI = "/datamanager";
	public static final String OP_DATAMANAGER_PROXY = "/proxy";
	public static final String OP_DATAMANAGER_HISTORIAN = "/historian";

    public static final String TRANSLATOR_URI = "/translator";
    public static final String OP_TRANSLATOR_FIWARE_URI = "/v2";
    public static final String OP_TRANSLATOR_PLUGIN_URI = "/plugin";

	public static final String SWAGGER_COMMON_PACKAGE = "eu.arrowhead.common.swagger";
	public static final String SWAGGER_UI_URI = "/swagger-ui.html";
	public static final String SWAGGER_HTTP_200_MESSAGE = "Core service is available";
	public static final String SWAGGER_HTTP_400_MESSAGE = "Bad request";
	public static final String SWAGGER_HTTP_401_MESSAGE = "You are not authorized";
	public static final String SWAGGER_HTTP_409_MESSAGE = "Request caused a conflict";
	public static final String SWAGGER_HTTP_500_MESSAGE = "Core service is not available";

	public static final String REQUEST_PARAM_TOKEN = "token";
	
	public static final String ECHO_URI = "/echo";
	
	public static final List<CoreSystemService> PUBLIC_CORE_SYSTEM_SERVICES = List.of(CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
  			  																		  CoreSystemService.EVENT_PUBLISH_SERVICE, CoreSystemService.EVENT_SUBSCRIBE_SERVICE, CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE, 
																					  CoreSystemService.PROXY_SERVICE, CoreSystemService.HISTORIAN_SERVICE, CoreSystemService.CHOREOGRAPHER_SERVICE, CoreSystemService.TRANSLATOR_SERVICE);
	
	public static final String HTTP_CLIENT_CONNECTION_TIMEOUT = "http.client.connection.timeout";
	public static final String $HTTP_CLIENT_CONNECTION_TIMEOUT_WD = "${" + HTTP_CLIENT_CONNECTION_TIMEOUT + ":" + Defaults.DEFAULT_CONNECTION_TIMEOUT + "}";
	public static final String HTTP_CLIENT_SOCKET_TIMEOUT = "http.client.socket.timeout";
	public static final String $HTTP_CLIENT_SOCKET_TIMEOUT_WD = "${" + HTTP_CLIENT_SOCKET_TIMEOUT + ":" + Defaults.DEFAULT_SOCKET_TIMEOUT + "}";
	public static final String HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT = "http.client.connection.manager.timeout";
	public static final String $HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT_WD = "${" + HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT + ":" + Defaults.DEFAULT_CONNECTION_MANAGER_TIMEOUT + "}";
	
	public static final String COMMON_NAME_FIELD_NAME = "CN";
	public static final String ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";
	
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
	
	public static final String CA_CERT_VALIDITY_NEG_OFFSET_MINUTES = "ca.validity.negative-offest-minutes";
	public static final String $CA_CERT_VALIDITY_NEG_OFFSET_MINUTES = "${" + CA_CERT_VALIDITY_NEG_OFFSET_MINUTES + ":" + Defaults.DEFAULT_CA_CERT_VALIDITY_NEG_OFFSET_MINUTES + "}";
	public static final String CA_CERT_VALIDITY_POS_OFFSET_MINUTES = "ca.validity.positive-offest-minutes";
	public static final String $CA_CERT_VALIDITY_POS_OFFSET_MINUTES = "${" + CA_CERT_VALIDITY_POS_OFFSET_MINUTES + ":" + Defaults.DEFAULT_CA_CERT_VALIDITY_POS_OFFSET_MINUTES + "}";

	public static final String JWT_CLAIM_CONSUMER_ID = "cid";
	public static final String JWT_CLAIM_SERVICE_ID = "sid";
	public static final String JWT_CLAIM_INTERFACE_ID = "iid";
	public static final String JWT_CLAIM_MESSAGE_TYPE = "mst";
	public static final String JWT_CLAIM_SESSION_ID = "sid"; // can be the same as service id because we don't use service id and session id at the same time
	public static final String JWT_CLAIM_PAYLOAD = "pld"; 
	public static final String JWE_KEY_MANAGEMENT_ALG = KeyManagementAlgorithmIdentifiers.RSA_OAEP_256;
	public static final String JWE_ENCRYPTION_ALG = ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512;
	public static final String JWS_SIGN_ALG = AlgorithmIdentifiers.RSA_USING_SHA512;
	
	public static final int SYSTEM_PORT_RANGE_MIN = 0;
	public static final int SYSTEM_PORT_RANGE_MAX = 65535;
	
	public static final long CONVERSION_MILLISECOND_TO_SECOND = 1000;
	public static final long CONVERSION_MILLISECOND_TO_MINUTE = 60000;
	public static final long CONVERSION_SECOND_TO_MINUTE = 60;

	public static final String SORT_ORDER_ASCENDING = "ASC";
	public static final String SORT_ORDER_DESCENDING = "DESC";
	
	public static final String SORT_FIELD_PRIORITY = "priority";
		
	public static final String INTRA_CLOUD_PROVIDER_MATCHMAKER = "intraCloudProviderMatchmaker";
	public static final String INTER_CLOUD_PROVIDER_MATCHMAKER = "interCloudProviderMatchmaker";
	public static final String ICN_PROVIDER_MATCHMAKER = "icnProviderMatchmaker";
	public static final String GATEKEEPER_MATCHMAKER = "gatekeeperMatchmaker";
	public static final String GATEWAY_MATCHMAKER = "gatewayMatchmaker";
	public static final String CLOUD_MATCHMAKER = "cloudMatchmaker";
	
	public static final int TOP_PRIORITY = 1;
		
	public static final String EVENT_METADATA_FILTER = "metadataFilter";

	public static final String LOCALHOST = "localhost";
	public static final int HTTP_PORT = 8080;

	public static final String OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE = "event_type";
	public static final String OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME = "system_name";
	public static final String OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS = "address";
	public static final String OP_EVENT_HANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT = "port";
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CommonConstants() {
		throw new UnsupportedOperationException();
	}
}

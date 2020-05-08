package eu.arrowhead.common.core;

import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;

public enum CoreSystem {
	
	//=================================================================================================
	// elements
	
	SERVICE_REGISTRY(Defaults.DEFAULT_SERVICE_REGISTRY_PORT, null),
	AUTHORIZATION(Defaults.DEFAULT_AUTHORIZATION_PORT, List.of(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE, CoreSystemService.AUTH_CONTROL_INTER_SERVICE,
															   CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
															   CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE)),
	ORCHESTRATOR(Defaults.DEFAULT_ORCHESTRATOR_PORT, List.of(CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.ORCHESTRATION_QOS_ENABLED_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_RESERVATIONS_SERVICE, CoreSystemService.ORCHESTRATION_QOS_TEMPORARY_LOCK_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_CONFIRM_RESERVATION_SERVICE)), 
	GATEKEEPER(Defaults.DEFAULT_GATEKEEPER_PORT, List.of(CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY, CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION,
														 CoreSystemService.GATEKEEPER_PULL_CLOUDS, CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES,
														 CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES, CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE,
														 CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE)),
	EVENT_HANDLER(Defaults.DEFAULT_EVENT_HANDLER_PORT, List.of(CoreSystemService.EVENT_PUBLISH_SERVICE, CoreSystemService.EVENT_SUBSCRIBE_SERVICE,
															   CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE, CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE)),
	GATEWAY(Defaults.DEFAULT_GATEWAY_PORT, List.of(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE, CoreSystemService.GATEWAY_PROVIDER_SERVICE,CoreSystemService.GATEWAY_CONSUMER_SERVICE)),
	CHOREOGRAPHER(Defaults.DEFAULT_CHOREOGRAPHER_PORT, List.of()), // TODO: add services
	QOS_MONITOR(Defaults.DEFAULT_QOS_MONITOR_PORT, List.of(
			CoreSystemService.QOS_MONITOR_INTRA_PING_MEASUREMENT_SERVICE,
			CoreSystemService.QOS_MONITOR_INTRA_PING_MEDIAN_MEASUREMENT_SERVICE,
			CoreSystemService.QOS_MONITOR_INTER_DIRECT_PING_MEASUREMENT_SERVICE,
			CoreSystemService.QOS_MONITOR_INTER_RELAY_ECHO_MEASUREMENT_SERVICE,
			CoreSystemService.QOS_MONITOR_PUBLIC_KEY_SERVICE,
			CoreSystemService.QOS_MONITOR_JOIN_RELAY_TEST_SERVICE,
			CoreSystemService.QOS_MONITOR_INIT_RELAY_TEST_SERVICE)),
	CERTIFICATE_AUTHORITY(Defaults.DEFAULT_CERTIFICATE_AUTHORITY_PORT, List.of(CoreSystemService.CERTIFICATE_AUTHORITY_SIGN_SERVICE));
	
	//=================================================================================================
	// members
	
	private final int defaultPort;
	private final List<CoreSystemService> services;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public int getDefaultPort() { return defaultPort; }
	public List<CoreSystemService> getServices() { return services; }
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CoreSystem(final int defaultPort, final List<CoreSystemService> services) {
		Assert.isTrue(defaultPort > CommonConstants.SYSTEM_PORT_RANGE_MIN && defaultPort < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Default port is invalid.");
		this.services = services != null ? Collections.unmodifiableList(services) : List.of();
		this.defaultPort = defaultPort;
	}
}

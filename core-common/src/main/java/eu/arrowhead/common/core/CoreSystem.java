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

package eu.arrowhead.common.core;

import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;

public enum CoreSystem {
	
	//=================================================================================================
	// elements
	
	SERVICE_REGISTRY(Defaults.DEFAULT_SERVICE_REGISTRY_PORT, List.of(CoreSystemService.SERVICE_REGISTRY_REGISTER_SERVICE,
																	 CoreSystemService.SERVICE_REGISTRY_UNREGISTER_SERVICE)),
	
    SYSTEM_REGISTRY(Defaults.DEFAULT_SYSTEM_REGISTRY_PORT, List.of(CoreSystemService.SYSTEM_REGISTRY_REGISTER_SERVICE,
    															   CoreSystemService.SYSTEM_REGISTRY_UNREGISTER_SERVICE,
    															   CoreSystemService.SYSTEM_REGISTRY_ONBOARDING_WITH_NAME_SERVICE,
    															   CoreSystemService.SYSTEM_REGISTRY_ONBOARDING_WITH_CSR_SERVICE)),
    
    DEVICE_REGISTRY(Defaults.DEFAULT_DEVICE_REGISTRY_PORT, List.of(CoreSystemService.DEVICE_REGISTRY_REGISTER_SERVICE,
    															   CoreSystemService.DEVICE_REGISTRY_UNREGISTER_SERVICE,
    															   CoreSystemService.DEVICE_REGISTRY_ONBOARDING_WITH_NAME_SERVICE,
    															   CoreSystemService.DEVICE_REGISTRY_ONBOARDING_WITH_CSR_SERVICE)),
    
    ONBOARDING_CONTROLLER(Defaults.DEFAULT_ONBOARDING_PORT, List.of(CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_NAME_SERVICE,
														    		CoreSystemService.ONBOARDING_WITH_SHARED_SECRET_AND_NAME_SERVICE,
														    		CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_CSR_SERVICE,
														    		CoreSystemService.ONBOARDING_WITH_SHARED_SECRET_AND_CSR_SERVICE)),
    
	AUTHORIZATION(Defaults.DEFAULT_AUTHORIZATION_PORT, List.of(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE,
															   CoreSystemService.AUTH_CONTROL_INTER_SERVICE,
															   CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE,
															   CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
															   CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE)),
	
	ORCHESTRATOR(Defaults.DEFAULT_ORCHESTRATOR_PORT, List.of(CoreSystemService.ORCHESTRATION_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_ENABLED_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_RESERVATIONS_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_TEMPORARY_LOCK_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_CONFIRM_RESERVATION_SERVICE)),
	
	GATEKEEPER(Defaults.DEFAULT_GATEKEEPER_PORT, List.of(CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY,
														 CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION,
														 CoreSystemService.GATEKEEPER_PULL_CLOUDS,
														 CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES,
														 CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES,
														 CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE,
														 CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE)),
	
	EVENT_HANDLER(Defaults.DEFAULT_EVENT_HANDLER_PORT, List.of(CoreSystemService.EVENT_PUBLISH_SERVICE,
															   CoreSystemService.EVENT_SUBSCRIBE_SERVICE,
															   CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE,
															   CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE)),

        DATAMANAGER(Defaults.DEFAULT_DATAMANAGER_PORT, List.of(CoreSystemService.PROXY_SERVICE, CoreSystemService.HISTORIAN_SERVICE)),
	
	GATEWAY(Defaults.DEFAULT_GATEWAY_PORT, List.of(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE,
												   CoreSystemService.GATEWAY_PROVIDER_SERVICE,
												   CoreSystemService.GATEWAY_CONSUMER_SERVICE)),
	
	CHOREOGRAPHER(Defaults.DEFAULT_CHOREOGRAPHER_PORT, List.of(CoreSystemService.CHOREOGRAPHER_SERVICE)),
	
	QOS_MONITOR(Defaults.DEFAULT_QOS_MONITOR_PORT, List.of(CoreSystemService.QOS_MONITOR_INTRA_PING_MEASUREMENT_SERVICE,
														   CoreSystemService.QOS_MONITOR_INTRA_PING_MEDIAN_MEASUREMENT_SERVICE,
														   CoreSystemService.QOS_MONITOR_INTER_DIRECT_PING_MEASUREMENT_SERVICE,
														   CoreSystemService.QOS_MONITOR_INTER_RELAY_ECHO_MEASUREMENT_SERVICE,
														   CoreSystemService.QOS_MONITOR_PUBLIC_KEY_SERVICE,
														   CoreSystemService.QOS_MONITOR_JOIN_RELAY_TEST_SERVICE,
														   CoreSystemService.QOS_MONITOR_INIT_RELAY_TEST_SERVICE)),
	
	CERTIFICATE_AUTHORITY(Defaults.DEFAULT_CERTIFICATE_AUTHORITY_PORT, List.of(CoreSystemService.CERTIFICATE_AUTHORITY_SIGN_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_LIST_CERTIFICATES_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_CHECK_CERTIFICATE_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_REVOKE_CERTIFICATE_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_LIST_TRUSTED_KEYS_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_CHECK_TRUSTED_KEY_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_ADD_TRUSTED_KEY_SERVICE,
																				CoreSystemService.CERTIFICATE_AUTHORITY_DELETE_TRUSTED_KEY_SERVICE)),
																				
	TRANSLATOR(Defaults.DEFAULT_TRANSLATOR_PORT, List.of(CoreSystemService.TRANSLATOR_SERVICE,
												   CoreSystemService.TRANSLATOR_FIWARE_SERVICE,
												   CoreSystemService.TRANSLATOR_PLUGIN_SERVICE));
	
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

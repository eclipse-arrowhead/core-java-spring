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
	
	// Please create enum values here without underscore, because these values has a direct connection with the certificate's CN which does not support underscores!
	
	SERVICEREGISTRY(Defaults.DEFAULT_SERVICEREGISTRY_PORT, List.of(CoreSystemService.SERVICEREGISTRY_REGISTER_SERVICE,
																	 CoreSystemService.SERVICEREGISTRY_UNREGISTER_SERVICE,
																	 CoreSystemService.SERVICEREGISTRY_REGISTER_SYSTEM,
																	 CoreSystemService.SERVICEREGISTRY_UNREGISTER_SYSTEM,
																	 CoreSystemService.SERVICEREGISTRY_PULL_SYSTEMS)),

    SYSTEMREGISTRY(Defaults.DEFAULT_SYSTEMREGISTRY_PORT, List.of(CoreSystemService.SYSTEMREGISTRY_REGISTER_SERVICE,
    															   CoreSystemService.SYSTEMREGISTRY_UNREGISTER_SERVICE,
    															   CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_NAME_SERVICE,
    															   CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_CSR_SERVICE)),
    
    DEVICEREGISTRY(Defaults.DEFAULT_DEVICEREGISTRY_PORT, List.of(CoreSystemService.DEVICEREGISTRY_REGISTER_SERVICE,
    															   CoreSystemService.DEVICEREGISTRY_UNREGISTER_SERVICE,
    															   CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_NAME_SERVICE,
    															   CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_CSR_SERVICE)),

    ONBOARDINGCONTROLLER(Defaults.DEFAULT_ONBOARDING_PORT, List.of(CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_NAME_SERVICE,
														    		CoreSystemService.ONBOARDING_WITH_SHARED_SECRET_AND_NAME_SERVICE,
														    		CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_CSR_SERVICE,
														    		CoreSystemService.ONBOARDING_WITH_SHARED_SECRET_AND_CSR_SERVICE)),
    
	AUTHORIZATION(Defaults.DEFAULT_AUTHORIZATION_PORT, List.of(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE,
															   CoreSystemService.AUTH_CONTROL_INTER_SERVICE,
															   CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE,
															   CoreSystemService.AUTH_PUBLIC_KEY_SERVICE,
															   CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE)),
	
	ORCHESTRATOR(Defaults.DEFAULT_ORCHESTRATOR_PORT, List.of(CoreSystemService.ORCHESTRATION_SERVICE,
															 CoreSystemService.ORCHESTRATION_BY_PROXY_SERVICE,
															 CoreSystemService.ORCHESTRATION_CREATE_FLEXIBLE_STORE_RULES_SERVICE,
															 CoreSystemService.ORCHESTRATION_REMOVE_FLEXIBLE_STORE_RULE_SERVICE,
															 CoreSystemService.ORCHESTRATION_CLEAN_FLEXIBLE_STORE_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_ENABLED_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_RESERVATIONS_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_TEMPORARY_LOCK_SERVICE,
															 CoreSystemService.ORCHESTRATION_QOS_CONFIRM_RESERVATION_SERVICE)),
	
	GATEKEEPER(Defaults.DEFAULT_GATEKEEPER_PORT, List.of(CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY,
														 CoreSystemService.GATEKEEPER_MULTI_GLOBAL_SERVICE_DISCOVERY,
														 CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION,
														 CoreSystemService.GATEKEEPER_PULL_CLOUDS,
														 CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES,
														 CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES,
														 CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE,
														 CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE)),
	
	EVENTHANDLER(Defaults.DEFAULT_EVENTHANDLER_PORT, List.of(CoreSystemService.EVENT_PUBLISH_SERVICE,
															   CoreSystemService.EVENT_SUBSCRIBE_SERVICE,
															   CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE,
															   CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE)),

    DATAMANAGER(Defaults.DEFAULT_DATAMANAGER_PORT, List.of(CoreSystemService.PROXY_SERVICE, CoreSystemService.HISTORIAN_SERVICE)),

	TIMEMANAGER(Defaults.DEFAULT_TIMEMANAGER_PORT, List.of(CoreSystemService.TIME_SERVICE)),

	GATEWAY(Defaults.DEFAULT_GATEWAY_PORT, List.of(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE,
												   CoreSystemService.GATEWAY_PROVIDER_SERVICE,
												   CoreSystemService.GATEWAY_CONSUMER_SERVICE,
												   CoreSystemService.GATEWAY_CLOSE_SESSIONS_SERVICE)),
	
	CHOREOGRAPHER(Defaults.DEFAULT_CHOREOGRAPHER_PORT, List.of(CoreSystemService.CHOREOGRAPHER_SERVICE,
			                                                   CoreSystemService.CHOREOGRAPHER_REGISTER_EXECUTOR_SERVICE,
			                                                   CoreSystemService.CHOREOGRAPHER_UNREGISTER_EXECUTOR_SERVICE,
			                                                   CoreSystemService.CHOREOGRAPHER_START_SESSION_SERVICE,
			                                                   CoreSystemService.CHOREOGRAPHER_ABORT_SESSION_SERVICE)),

    CONFIGURATION(Defaults.DEFAULT_CONFIGURATION_PORT, List.of(CoreSystemService.CONFIGURATION_SERVICE,
                                                               CoreSystemService.CONFIGURATION_RAW_SERVICE,
                                                               CoreSystemService.CONFIGURATION_BY_PROXY_SERVICE,
                                                               CoreSystemService.CONFIGURATION_SAVE_BY_PROXY_SERVICE)),

	QOSMONITOR(Defaults.DEFAULT_QOSMONITOR_PORT, List.of(CoreSystemService.QOSMONITOR_INTRA_PING_MEASUREMENT_SERVICE,
														   CoreSystemService.QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_SERVICE,
														   CoreSystemService.QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_SERVICE,
														   CoreSystemService.QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_SERVICE,
														   CoreSystemService.QOSMONITOR_PUBLIC_KEY_SERVICE,
														   CoreSystemService.QOSMONITOR_JOIN_RELAY_TEST_SERVICE,
														   CoreSystemService.QOSMONITOR_INIT_RELAY_TEST_SERVICE)),
	
	CERTIFICATEAUTHORITY(Defaults.DEFAULT_CERTIFICATEAUTHORITY_PORT, List.of(CoreSystemService.CERTIFICATEAUTHORITY_SIGN_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_LIST_CERTIFICATES_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_CHECK_CERTIFICATE_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_REVOKE_CERTIFICATE_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_LIST_TRUSTED_KEYS_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_CHECK_TRUSTED_KEY_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_ADD_TRUSTED_KEY_SERVICE,
																				CoreSystemService.CERTIFICATEAUTHORITY_DELETE_TRUSTED_KEY_SERVICE)),

	TRANSLATOR(Defaults.DEFAULT_TRANSLATOR_PORT, List.of(CoreSystemService.TRANSLATOR_SERVICE,
												   CoreSystemService.TRANSLATOR_FIWARE_SERVICE,
												   CoreSystemService.TRANSLATOR_PLUGIN_SERVICE)),

	MSCV(Defaults.DEFAULT_MSCV_PORT, List.of(CoreSystemService.MSCV_VERIFICATION_SERVICE,
											 CoreSystemService.MSCV_PUBLIC_KEY_SERVICE,
											 CoreSystemService.MSCV_LOGIN_SERVICE)),

	PLANTDESCRIPTIONENGINE(Defaults.DEFAULT_PLANT_DESCRIPTION_ENGINE_PORT, List.of()), //TODO add PDE services



	GAMS(Defaults.DEFAULT_GENERIC_AUTONOMIC_MANAGEMENT_PORT, List.of(CoreSystemService.GAMS_SERVICE,
																	 CoreSystemService.GAMS_SENSOR_SERVICE)),
	
	HAWKBITCONFIGURATIONMANAGER(Defaults.DEFAULT_HAWKBIT_CONFIGURATION_MANAGER_PORT, List.of());

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

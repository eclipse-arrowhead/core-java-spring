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

package eu.arrowhead.core.gatekeeper;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;

import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.gatekeeper.quartz.RelaySupervisor;
import eu.arrowhead.core.gatekeeper.quartz.subscriber.RelaySubscriberDataContainer;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GetRandomAndDedicatedIfAnyGatekeeperMatchmaker;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GetRandomCommonPreferredIfAnyOrRandomCommonPublicGatewayMatchmaker;
import eu.arrowhead.core.gatekeeper.service.matchmaking.ICNProviderMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RandomICNProviderMatchmaker;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClientFactory;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClientUsingCachedSessions;

@Component
public class GatekeeperApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members

	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private long timeout;
	
	@Value(CoreCommonConstants.$GATEKEEPER_IS_GATEWAY_PRESENT_WD)
	private boolean gatewayIsPresent;
		
	@Value(CoreCommonConstants.$GATEKEEPER_IS_GATEWAY_MANDATORY_WD)
	private boolean gatewayIsMandatory;
	
	@Autowired
	private SSLProperties sslProps;
	
	private GatekeeperRelayClientUsingCachedSessions gatekeeperRelayClientWithCache;
	
	private RelaySubscriberDataContainer relaySubscriberDataContainer; // initialization is on demand to avoid circular dependencies 
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.GATEKEEPER_MATCHMAKER)
	public RelayMatchmakingAlgorithm getGatekeeperRelayMatchmakingAlgorithm() {
		return new GetRandomAndDedicatedIfAnyGatekeeperMatchmaker();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.GATEWAY_MATCHMAKER)
	public RelayMatchmakingAlgorithm getGatewayRelayMatchmakingAlgorithm() {
		return new GetRandomCommonPreferredIfAnyOrRandomCommonPublicGatewayMatchmaker();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.ICN_PROVIDER_MATCHMAKER)
	public ICNProviderMatchmakingAlgorithm getICNProviderMatchmaker() {
		return new RandomICNProviderMatchmaker();
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		if (gatewayIsPresent) {
			return List.of(CoreSystemService.AUTH_CONTROL_INTER_SERVICE, CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.ORCHESTRATION_QOS_ENABLED_SERVICE,
						   CoreSystemService.ORCHESTRATION_QOS_RESERVATIONS_SERVICE, CoreSystemService.ORCHESTRATION_QOS_TEMPORARY_LOCK_SERVICE,
						   CoreSystemService.QOSMONITOR_INTRA_PING_MEASUREMENT_SERVICE, CoreSystemService.ORCHESTRATION_QOS_CONFIRM_RESERVATION_SERVICE,
						   CoreSystemService.GATEWAY_PROVIDER_SERVICE, CoreSystemService.GATEWAY_CONSUMER_SERVICE,
						   CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE, CoreSystemService.QOSMONITOR_PUBLIC_KEY_SERVICE,
						   CoreSystemService.QOSMONITOR_JOIN_RELAY_TEST_SERVICE, CoreSystemService.QOSMONITOR_INIT_RELAY_TEST_SERVICE); 
		}
		
		return List.of(CoreSystemService.AUTH_CONTROL_INTER_SERVICE, CoreSystemService.ORCHESTRATION_SERVICE, CoreSystemService.ORCHESTRATION_QOS_ENABLED_SERVICE,
					   CoreSystemService.ORCHESTRATION_QOS_RESERVATIONS_SERVICE, CoreSystemService.ORCHESTRATION_QOS_TEMPORARY_LOCK_SERVICE,
					   CoreSystemService.QOSMONITOR_INTRA_PING_MEASUREMENT_SERVICE, CoreSystemService.ORCHESTRATION_QOS_CONFIRM_RESERVATION_SERVICE); 
	}
		
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		if (!sslProperties.isSslEnabled()) {
			throw new ServiceConfigurationError("Gatekeeper can only started in SECURE mode!");
		}
		
		if (gatewayIsMandatory && !gatewayIsPresent) {
			throw new ServiceConfigurationError("Gatekeeper can't start with 'gateway_is_present=false' property when the 'gateway_is_mandatory' property is true!");
		}
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = event.getApplicationContext().getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		final UriComponents queryAll = createQueryAllUri(CommonConstants.HTTPS);
		context.put(CoreCommonConstants.SR_QUERY_ALL, queryAll);
		
		initializeGatekeeperRelayClient(event.getApplicationContext());
		relaySubscriberDataContainer = event.getApplicationContext().getBean(RelaySubscriberDataContainer.class);
		relaySubscriberDataContainer.init();
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {
		// close connections using to listen on advertisement topic
		relaySubscriberDataContainer.close();
		
		// close connections used by web services and gatekeeper tasks
		for (final Session session : gatekeeperRelayClientWithCache.getCachedSessions()) {
			gatekeeperRelayClientWithCache.closeConnection(session);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void initializeGatekeeperRelayClient(final ApplicationContext appContext) {
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
		final PublicKey publicKey = (PublicKey) context.get(CommonConstants.SERVER_PUBLIC_KEY);
		final PrivateKey privateKey = (PrivateKey) context.get(CommonConstants.SERVER_PRIVATE_KEY);

		this.gatekeeperRelayClientWithCache = (GatekeeperRelayClientUsingCachedSessions) GatekeeperRelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, sslProps, timeout,
																																				  true, RelaySupervisor.getRegistry());
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryAllUri(final String scheme) {
		logger.debug("createQueryAllUri started...");

		final String registyUriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI;

		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registyUriStr);
	}
}
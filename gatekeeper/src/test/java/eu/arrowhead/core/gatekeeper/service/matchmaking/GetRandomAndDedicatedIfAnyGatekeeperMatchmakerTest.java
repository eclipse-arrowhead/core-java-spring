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

package eu.arrowhead.core.gatekeeper.service.matchmaking;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.RelayType;

@RunWith(SpringRunner.class)
public class GetRandomAndDedicatedIfAnyGatekeeperMatchmakerTest {
	
	//=================================================================================================
	// members
	
	private final RelayMatchmakingAlgorithm algorithm = new GetRandomAndDedicatedIfAnyGatekeeperMatchmaker();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithGatekeeperAndGeneralAndGatewayRelayTypes() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-auth-info");
		
		final Relay gatekeeperRelay = new Relay();
		gatekeeperRelay.setId(1);
		gatekeeperRelay.setAddress("1.1.1.1");
		gatekeeperRelay.setPort(1000);
		gatekeeperRelay.setSecure(true);
		gatekeeperRelay.setExclusive(false);
		gatekeeperRelay.setType(RelayType.GATEKEEPER_RELAY);
		
		final Relay generalRelay = new Relay();
		generalRelay.setId(2);
		generalRelay.setAddress("2.2.2.2");
		generalRelay.setPort(2000);
		generalRelay.setSecure(true);
		generalRelay.setExclusive(false);
		generalRelay.setType(RelayType.GENERAL_RELAY);
		
		final Relay gatewayRelay = new Relay();
		gatewayRelay.setId(3);
		gatewayRelay.setAddress("3.3.3.3");
		gatewayRelay.setPort(3000);
		gatewayRelay.setSecure(true);
		gatewayRelay.setExclusive(true);
		gatewayRelay.setType(RelayType.GATEWAY_RELAY);
		
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(gatekeeperRelay, generalRelay, gatewayRelay), System.currentTimeMillis());
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(RelayType.GATEKEEPER_RELAY, relayMatch.getType());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithGeneralAndGatewayRelayTypes() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-auth-info");
		
		final Relay generalRelay = new Relay();
		generalRelay.setId(2);
		generalRelay.setAddress("2.2.2.2");
		generalRelay.setPort(2000);
		generalRelay.setSecure(true);
		generalRelay.setExclusive(false);
		generalRelay.setType(RelayType.GENERAL_RELAY);
		
		final Relay gatewayRelay = new Relay();
		gatewayRelay.setId(3);
		gatewayRelay.setAddress("3.3.3.3");
		gatewayRelay.setPort(3000);
		gatewayRelay.setSecure(true);
		gatewayRelay.setExclusive(true);
		gatewayRelay.setType(RelayType.GATEWAY_RELAY);
		
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(generalRelay, gatewayRelay), System.currentTimeMillis());
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(RelayType.GENERAL_RELAY, relayMatch.getType());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithMoreGatekeeperRelayTypes() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-auth-info");
		
		final Relay gatekeeperRelay1 = new Relay();
		gatekeeperRelay1.setId(1);
		gatekeeperRelay1.setAddress("1.1.1.1");
		gatekeeperRelay1.setPort(1000);
		gatekeeperRelay1.setSecure(true);
		gatekeeperRelay1.setExclusive(false);
		gatekeeperRelay1.setType(RelayType.GATEKEEPER_RELAY);
		
		final Relay gatekeeperRelay2 = new Relay();
		gatekeeperRelay2.setId(2);
		gatekeeperRelay2.setAddress("2.2.2.2");
		gatekeeperRelay2.setPort(2000);
		gatekeeperRelay2.setSecure(true);
		gatekeeperRelay2.setExclusive(false);
		gatekeeperRelay2.setType(RelayType.GATEKEEPER_RELAY);
		
		final Relay gatekeeperRelay3 = new Relay();
		gatekeeperRelay3.setId(3);
		gatekeeperRelay3.setAddress("3.3.3.3");
		gatekeeperRelay3.setPort(3000);
		gatekeeperRelay3.setSecure(true);
		gatekeeperRelay3.setExclusive(false);
		gatekeeperRelay3.setType(RelayType.GATEKEEPER_RELAY);
		
		final long seed = System.currentTimeMillis();		
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(gatekeeperRelay1, gatekeeperRelay2, gatekeeperRelay3), seed);
		final Random rng = new Random(seed);
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(List.copyOf(cloud.getGatekeeperRelays()).get(rng.nextInt(3)).getRelay().getId(), relayMatch.getId());		
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private RelayMatchmakingParameters createRelayMatchmakingParameters(final Cloud cloud, final List<Relay> gatekeeperRelays, final long randomSeed) {
		for (final Relay relay : gatekeeperRelays) {
			if (relay.getType() == RelayType.GATEWAY_RELAY && relay.getExclusive()) {
				final CloudGatewayRelay conn = new CloudGatewayRelay(cloud, relay);
				cloud.getGatewayRelays().add(conn);
				relay.getCloudGateways().add(conn);
			} else {
				final CloudGatekeeperRelay conn = new CloudGatekeeperRelay(cloud, relay);
				cloud.getGatekeeperRelays().add(conn);
				relay.getCloudGatekeepers().add(conn);				
			}
		}
	
		final RelayMatchmakingParameters parameters = new RelayMatchmakingParameters(cloud);
		parameters.setRandomSeed(randomSeed);
		
		return parameters;
	}
}
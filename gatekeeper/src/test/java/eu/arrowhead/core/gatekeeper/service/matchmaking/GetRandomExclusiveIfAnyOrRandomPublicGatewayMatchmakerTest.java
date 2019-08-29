package eu.arrowhead.core.gatekeeper.service.matchmaking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@RunWith(SpringRunner.class)
public class GetRandomExclusiveIfAnyOrRandomPublicGatewayMatchmakerTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private final RelayMatchmakingAlgorithm algorithm = new GetRandomExclusiveIfAnyOrRandomPublicGatewayMatchmaker();
	
	@Mock
	private GatekeeperDBService gatekeeperDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithHavingOneExclusiveGatewayRelay() {
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
		
		assertEquals(RelayType.GATEWAY_RELAY, relayMatch.getType());
		assertTrue(relayMatch.getExclusive());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithHavingMoreExclusiveGatewayRelays() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-auth-info");
		
		final Relay gatewayRelay1 = new Relay();
		gatewayRelay1.setId(1);
		gatewayRelay1.setAddress("1.1.1.1");
		gatewayRelay1.setPort(1000);
		gatewayRelay1.setSecure(true);
		gatewayRelay1.setExclusive(true);
		gatewayRelay1.setType(RelayType.GATEWAY_RELAY);
		
		final Relay gatewayRelay2 = new Relay();
		gatewayRelay2.setId(2);
		gatewayRelay2.setAddress("2.2.2.2");
		gatewayRelay2.setPort(2000);
		gatewayRelay2.setSecure(true);
		gatewayRelay2.setExclusive(true);
		gatewayRelay2.setType(RelayType.GATEWAY_RELAY);
		
		final Relay gatewayRelay3 = new Relay();
		gatewayRelay3.setId(3);
		gatewayRelay3.setAddress("3.3.3.3");
		gatewayRelay3.setPort(3000);
		gatewayRelay3.setSecure(true);
		gatewayRelay3.setExclusive(true);
		gatewayRelay3.setType(RelayType.GATEWAY_RELAY);	

		final long seed = System.currentTimeMillis();
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(gatewayRelay1, gatewayRelay2, gatewayRelay3), seed);
		final Random rng = new Random(seed);
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(List.copyOf(cloud.getGatewayRelays()).get(rng.nextInt(3)).getRelay().getId(), relayMatch.getId());	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithHavingOnlyNoneExclusiveGatewayRelayAndGeneralRelay() {
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
		gatewayRelay.setExclusive(false);
		gatewayRelay.setType(RelayType.GATEWAY_RELAY);
		
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(), System.currentTimeMillis());
		when(gatekeeperDBService.getPublicGatewayRelays()).thenReturn(List.of(gatewayRelay, generalRelay));
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(RelayType.GATEWAY_RELAY, relayMatch.getType());
		assertFalse(relayMatch.getExclusive());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithHavingOnlyMoreNoneExclusiveGatewayRelays() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-auth-info");
		
		final Relay gatewayRelay1 = new Relay();
		gatewayRelay1.setId(1);
		gatewayRelay1.setAddress("1.1.1.1");
		gatewayRelay1.setPort(1000);
		gatewayRelay1.setSecure(true);
		gatewayRelay1.setExclusive(false);
		gatewayRelay1.setType(RelayType.GATEWAY_RELAY);
		
		final Relay gatewayRelay2 = new Relay();
		gatewayRelay2.setId(2);
		gatewayRelay2.setAddress("2.2.2.2");
		gatewayRelay2.setPort(2000);
		gatewayRelay2.setSecure(true);
		gatewayRelay2.setExclusive(false);
		gatewayRelay2.setType(RelayType.GATEWAY_RELAY);
		
		final Relay gatewayRelay3 = new Relay();
		gatewayRelay3.setId(3);
		gatewayRelay3.setAddress("3.3.3.3");
		gatewayRelay3.setPort(3000);
		gatewayRelay3.setSecure(true);
		gatewayRelay3.setExclusive(false);
		gatewayRelay3.setType(RelayType.GATEWAY_RELAY);
		
		final long seed = System.currentTimeMillis();
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(), seed);
		final Random rng = new Random(seed);
		when(gatekeeperDBService.getPublicGatewayRelays()).thenReturn(List.of(gatewayRelay1, gatewayRelay2, gatewayRelay3));
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(List.of(gatewayRelay1, gatewayRelay2, gatewayRelay3).get(rng.nextInt(3)).getId(), relayMatch.getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMatchmakingWithHavingOnlyGeneralRelays() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-auth-info");
		
		final Relay generalRelay1 = new Relay();
		generalRelay1.setId(1);
		generalRelay1.setAddress("1.1.1.1");
		generalRelay1.setPort(1000);
		generalRelay1.setSecure(true);
		generalRelay1.setExclusive(false);
		generalRelay1.setType(RelayType.GENERAL_RELAY);
		
		final Relay generalRelay2 = new Relay();
		generalRelay2.setId(2);
		generalRelay2.setAddress("2.2.2.2");
		generalRelay2.setPort(2000);
		generalRelay2.setSecure(true);
		generalRelay2.setExclusive(false);
		generalRelay2.setType(RelayType.GENERAL_RELAY);
		
		final Relay generalRelay3 = new Relay();
		generalRelay3.setId(3);
		generalRelay3.setAddress("3.3.3.3");
		generalRelay3.setPort(3000);
		generalRelay3.setSecure(true);
		generalRelay3.setExclusive(false);
		generalRelay3.setType(RelayType.GENERAL_RELAY);
		
		final long seed = System.currentTimeMillis();
		final RelayMatchmakingParameters parameters = createRelayMatchmakingParameters(cloud, List.of(), seed);
		final Random rng = new Random(seed);
		when(gatekeeperDBService.getPublicGatewayRelays()).thenReturn(List.of(generalRelay1, generalRelay2, generalRelay3));
		
		final Relay relayMatch = algorithm.doMatchmaking(parameters);
		
		assertEquals(List.of(generalRelay1, generalRelay2, generalRelay3).get(rng.nextInt(3)).getId(), relayMatch.getId());
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

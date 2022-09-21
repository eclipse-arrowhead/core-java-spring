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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

public class GetRandomCommonPreferredIfAnyOrRandomCommonPublicGatewayMatchmaker implements RelayMatchmakingAlgorithm { 

	//=================================================================================================
	// members
	private static final Logger logger = LogManager.getLogger(GetRandomCommonPreferredIfAnyOrRandomCommonPublicGatewayMatchmaker.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	private Random rng;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		logger.debug("GetRandomCommonPreferredIfAnyOrRandomCommonPublicGatewayMatchmaker.doMatchmaking started...");
		
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		Assert.notNull(parameters.getPreferredGatewayRelays(), "Preferred relay list is null");
		Assert.notNull(parameters.getKnownGatewayRelays(), "Known relay list is null");
		
		if (rng == null) {
			rng = new Random(parameters.getRandomSeed());
		}
		
		if (!parameters.getCloud().getGatewayRelays().isEmpty() && !parameters.getPreferredGatewayRelays().isEmpty()) {			
			final Relay commonPreferredRelay = getRandomPreferredCommonRelay(parameters.getCloud().getGatewayRelays(), parameters.getPreferredGatewayRelays());
			if (commonPreferredRelay != null) {
				return commonPreferredRelay;
			}
		}
		
		final List<Relay> publicGatewayRelays = gatekeeperDBService.getPublicGatewayRelays();		
		if (!publicGatewayRelays.isEmpty() && !parameters.getKnownGatewayRelays().isEmpty()) {
			return getRandomPublicCommonRelay(publicGatewayRelays, parameters.getKnownGatewayRelays());			
		}
		
		return null;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Relay getRandomPreferredCommonRelay(final Set<CloudGatewayRelay> relayConnections, final List<RelayRequestDTO> relayRequests) {
		final List<Relay> commonRelays = new ArrayList<>();
		for (final CloudGatewayRelay relayConn : relayConnections) {
			for (final RelayRequestDTO requestedRelay : relayRequests) {				
				if (relayEquals(relayConn.getRelay(), requestedRelay)) {						
					commonRelays.add(relayConn.getRelay());
				}						
			}
		}
		
		if (!commonRelays.isEmpty() ) {
			return commonRelays.get(rng.nextInt(commonRelays.size()));
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Relay getRandomPublicCommonRelay(final List<Relay> relays, final List<RelayRequestDTO> relayRequests) {
		final List<Relay> commonGatewayRelays = new ArrayList<>();
		final List<Relay> commonGeneralRelays = new ArrayList<>();
		
		for (final Relay relay : relays) {
			for (final RelayRequestDTO requestedRelay : relayRequests) {				
				if (relayEquals(relay, requestedRelay)) {						
					if (relay.getType() == RelayType.GATEWAY_RELAY) {
						commonGatewayRelays.add(relay);
					} else {
						commonGeneralRelays.add(relay);
					}
				}						
			}
		}
		
		if (!commonGatewayRelays.isEmpty() ) {
			return commonGatewayRelays.get(rng.nextInt(commonGatewayRelays.size()));
		}
		
		if (!commonGeneralRelays.isEmpty() ) {
			return commonGeneralRelays.get(rng.nextInt(commonGeneralRelays.size()));
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean relayEquals(final Relay relay, final RelayRequestDTO dto) {
		if (!Utilities.isEmpty(relay.getAuthenticationInfo()) && !Utilities.isEmpty(dto.getAuthenticationInfo())) {
			return relay.getAuthenticationInfo().equals(dto.getAuthenticationInfo());
		}
		
		return relay.getAddress().equalsIgnoreCase(dto.getAddress()) && relay.getPort() == dto.getPort();
	}
}
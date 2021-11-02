/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.relay.gatekeeper;

import org.junit.Assert;
import org.junit.Test;

import eu.arrowhead.common.dto.internal.AccessTypeRelayResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.exception.DataNotFoundException;

public class GatekeeperRelayResponseTest {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSessionIdNull() {
		try {
			new GatekeeperRelayResponse(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSessionIdEmpty() {
		try {
			new GatekeeperRelayResponse("", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageTypeNull() {
		try {
			new GatekeeperRelayResponse("sessionId", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message type is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageTypeEmpty() {
		try {
			new GatekeeperRelayResponse("sessionId", "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message type is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPayloadNull() {
		try {
			new GatekeeperRelayResponse("sessionId", "type", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGSDPollResponseOk() {
		final GSDPollResponseDTO payload = new GSDPollResponseDTO();
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", payload);
		
		final GSDPollResponseDTO result = response.getGSDPollResponse();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetGSDPollResponseProblem() {
		try {
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", new Object());
			response.getGSDPollResponse();
		} catch (final Exception ex) {
			Assert.assertEquals("The response is not a result of a GSD poll.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGSDMultiPollResponseOk() {
		final GSDMultiPollResponseDTO payload = new GSDMultiPollResponseDTO();
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", payload);
		
		final GSDMultiPollResponseDTO result = response.getGSDMultiPollResponse();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetGSDMultiPollResponseProblem() {
		try {
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", new Object());
			response.getGSDMultiPollResponse();
		} catch (final Exception ex) {
			Assert.assertEquals("The response is not a result of a multi GSD poll.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetICNProposalResponseOk() {
		final ICNProposalResponseDTO payload = new ICNProposalResponseDTO();
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", payload);
		
		final ICNProposalResponseDTO result = response.getICNProposalResponse();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetICNProposalResponseProblem() {
		try {
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", new Object());
			response.getICNProposalResponse();
		} catch (final Exception ex) {
			Assert.assertEquals("The response is not a result of an ICN proposal.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAccessTypeResponse() {
		final AccessTypeRelayResponseDTO payload = new AccessTypeRelayResponseDTO();
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", payload);
		
		final AccessTypeRelayResponseDTO result = response.getAccessTypeResponse();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetAccessTypeResponseProblem() {
		try {
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", new Object());
			response.getAccessTypeResponse();
		} catch (final Exception ex) {
			Assert.assertEquals("The response is not a result of an access type request.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSystemAddressSetResponse() {
		final SystemAddressSetRelayResponseDTO payload = new SystemAddressSetRelayResponseDTO();
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", payload);
		
		final SystemAddressSetRelayResponseDTO result = response.getSystemAddressSetResponse();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetSystemAddressSetResponseProblem() {
		try {
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", new Object());
			response.getSystemAddressSetResponse();
		} catch (final Exception ex) {
			Assert.assertEquals("The response is not a result of a system addresses request.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetQoSRelayTestProposalResponseOk() {
		final QoSRelayTestProposalResponseDTO payload = new QoSRelayTestProposalResponseDTO();
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", payload);
		
		final QoSRelayTestProposalResponseDTO result = response.getQoSRelayTestProposalResponse();

		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetQoSRelayTestProposalRequestProblem() {
		try {
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse("sessionId", "type", new Object());
			response.getQoSRelayTestProposalResponse();
		} catch (final Exception ex) {
			Assert.assertEquals("The response is not a result of a relay test request.", ex.getMessage());
			
			throw ex;
		}
	}
}
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

package eu.arrowhead.core.gatekeeper.database.service;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.repository.CloudGatekeeperRelayRepository;
import eu.arrowhead.common.database.repository.CloudGatewayRelayRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.RelayRepository;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class GatekeeperDBServiceRelayTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperDBService gatekeeperDBService;
	
	@Mock
	private CloudRepository cloudRepository;
	
	@Mock
	private RelayRepository relayRepository;
	
	@Mock
	private CloudGatekeeperRelayRepository cloudGatekeeperRelayRepository;
	
	@Mock
	private CloudGatewayRelayRepository cloudGatewayRelayRepository;
	
	//=================================================================================================
	// methods
		
	//=================================================================================================
	// Tests of getRelays
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelaysWithInvalidSortFieldParameter() {
		gatekeeperDBService.getRelays(1, 1, Direction.ASC, "invalid");
	}
	
	//=================================================================================================
	// Tests of getRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void tesGetRelayByIdWithInvalidId() {
		gatekeeperDBService.getRelayById(-1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void tesGetRelayByIdWithNotExistingRelay() {
		when(relayRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		gatekeeperDBService.getRelayById(5);
	}
	
	//=================================================================================================
	// Tests of getRelayByAddressAndPort
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAddressAndPortWithNullAddress() {
		gatekeeperDBService.getRelayByAddressAndPort(null, 10000);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAddressAndPortWithBlankAddress() {
		gatekeeperDBService.getRelayByAddressAndPort("", 10000);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAddressAndPortWithInvalidPortMin() {
		gatekeeperDBService.getRelayByAddressAndPort("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAddressAndPortWithInvalidPortMax() {
		gatekeeperDBService.getRelayByAddressAndPort("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAddressAndPortWithNotExistingRelay() {
		when(relayRepository.findByAddressAndPort(any(), anyInt())).thenReturn(Optional.ofNullable(null));
		
		gatekeeperDBService.getRelayByAddressAndPort("1.1.1.1", 10000);
	}
	
	//=================================================================================================
	// Tests of registerBulkRelays
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithNullDTOList() {
		gatekeeperDBService.registerBulkRelays(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithEmptyDTOList() {
		gatekeeperDBService.registerBulkRelays(new ArrayList<>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithDTOListContainingNullElement() {
		final List<RelayRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(null);
		dtoList.add(new RelayRequestDTO());
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithRelayWithNullAddress() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO(null, 10000, null, true, false, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithRelayWithBlankAddress() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("", 10000, null, true, false, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithRelayInvalidPortMin() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, null, true, false, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithRelayInvalidPortMax() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, null, true, false, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithRelayNullPort() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", null, null, true, false, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithInvalidType() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, false, "INVALID_RELAY_TYPE"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithExclusiveGatekeeperRelay() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, true, "GATEKEEPER_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithExclusiveGeneralRelay() {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, true, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithUniqueConstraintViolationInDTOList() {		
		final List<RelayRequestDTO> dtoList =List.of(new RelayRequestDTO("1.1.1.1", 10000, null, false, false, "GENERAL_RELAY"),
												     new RelayRequestDTO("1.1.1.1", 10000, null, true, false, "GATEKEEPER_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithUniqueConstraintViolationInRelayTable() {
		when(relayRepository.existsByAddressAndPort(any(), anyInt())).thenReturn(true);
		
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, false, "GENERAL_RELAY"));
				
		gatekeeperDBService.registerBulkRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithAuthenticationInfoUniqueConstraintViolationInRelayTable() {
		when(relayRepository.existsByAddressAndPort(anyString(), anyInt())).thenReturn(false);
		when(relayRepository.existsByAuthenticationInfo(anyString())).thenReturn(true);
		
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, "test", true, false, "GENERAL_RELAY"));
		
		try {
			gatekeeperDBService.registerBulkRelays(dtoList);
		} catch (final Exception ex) {
			Assert.assertEquals("Relay with the following authentication info already exists: test", ex.getMessage());
			
			verify(relayRepository, times(1)).existsByAddressAndPort("1.1.1.1", 10000);
			verify(relayRepository, times(1)).existsByAuthenticationInfo("test");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterBulkRelaysWithAuthenticationInfoUniqueConstraintViolationInDTOList() {
		when(relayRepository.existsByAddressAndPort(anyString(), anyInt())).thenReturn(false);
		when(relayRepository.existsByAuthenticationInfo(anyString())).thenReturn(false);
		
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, "test", true, false, "GENERAL_RELAY"),
													  new RelayRequestDTO("1.1.1.1", 10001, "test", true, false, "GENERAL_RELAY"));
		
		try {
			gatekeeperDBService.registerBulkRelays(dtoList);
		} catch (final Exception ex) {
			Assert.assertEquals("List of RelayRequestDTO contains the following authentication info multiple times: test", ex.getMessage());
			
			verify(relayRepository, times(1)).existsByAddressAndPort("1.1.1.1", 10000);
			verify(relayRepository, times(1)).existsByAddressAndPort("1.1.1.1", 10001);
			verify(relayRepository, times(2)).existsByAuthenticationInfo("test");
			
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of updateRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithInvalidId() {
		gatekeeperDBService.updateRelayById(-1, "1.1.1.1", 10000, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithNotExistingRelay() {
		when(relayRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", 10000, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithNullAddress() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, null, 10000, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithBlankAddress() {
		final Relay relay = new Relay("0.0.0.0", 5000, null, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, "", 10000, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithInvalidPortMin() {
		final Relay relay = new Relay("0.0.0.0", 5000, null, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithInvalidPortMax() {
		final Relay relay = new Relay("0.0.0.0", 5000, null, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithExclusiveGatekeeperRelay() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", 10000, null, true, true, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithExclusiveGeneralRelay() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GENERAL_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", 10000, null, true, true, RelayType.GENERAL_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithUniqueConstraintViolationInRelayTable() {
		final Relay relay = new Relay("0.0.0.0", 5000, null, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		when(relayRepository.existsByAddressAndPort(any(), anyInt())).thenReturn(true);
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", 10000, null, true, false, RelayType.GATEKEEPER_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdWithChangingRelayType() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.updateRelayById(1, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithNullRelayType() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(anyLong())).thenReturn(Optional.of(relay));
		when(relayRepository.saveAndFlush(any())).thenReturn(null);
		
		final Relay updateRelay = gatekeeperDBService.updateRelayById(1, "1.1.1.1", 10000, null, true, false, null);
		assertNull(updateRelay);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdAuthenticationInfoUniqueConstraintViolation() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(1L)).thenReturn(Optional.of(relay));
		when(relayRepository.existsByAuthenticationInfo("test")).thenReturn(true);
		
		try {
			gatekeeperDBService.updateRelayById(1, "0.0.0.0", 5000, "test", true, false, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Relay with the following authentication info already exists: test", ex.getMessage());
			
			verify(relayRepository, times(1)).findById(1L);
			verify(relayRepository, times(1)).existsByAuthenticationInfo("test");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdAuthenticationInfoChangedToNull() {
		final Relay relay = new Relay("0.0.0.0", 5000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(1L)).thenReturn(Optional.of(relay));
		when(relayRepository.existsByAddressAndPort("0.0.0.0", 5001)).thenReturn(true);
		
		try {
			gatekeeperDBService.updateRelayById(1, "0.0.0.0", 5001, null, true, false, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Relay with the following address and port already exists: 0.0.0.0, 5001", ex.getMessage());
			
			verify(relayRepository, times(1)).findById(1L);
			verify(relayRepository, never()).existsByAuthenticationInfo(anyString());
			verify(relayRepository, times(1)).existsByAddressAndPort("0.0.0.0", 5001);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateRelayByIdAuthenticationInfoNotChanged() {
		final Relay relay = new Relay("0.0.0.0", 5000, "test", true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		
		when(relayRepository.findById(1L)).thenReturn(Optional.of(relay));
		when(relayRepository.existsByAddressAndPort("0.0.0.0", 5001)).thenReturn(true);
		
		try {
			gatekeeperDBService.updateRelayById(1, "0.0.0.0", 5001, "test", true, false, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Relay with the following address and port already exists: 0.0.0.0, 5001", ex.getMessage());
			
			verify(relayRepository, times(1)).findById(1L);
			verify(relayRepository, never()).existsByAuthenticationInfo(anyString());
			verify(relayRepository, times(1)).existsByAddressAndPort("0.0.0.0", 5001);
			
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of removeRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveRelayByIdWithInvalidRelayId() {
		gatekeeperDBService.removeRelayById(-1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveRelayByIdWithRelayWhichIsTheOnlyGatekeeperRelayOfACloud() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEKEEPER_RELAY);
		relay.setId(1);
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "dfybvfsdb");
		cloud.setId(1);
		final CloudGatekeeperRelay cloudGatekeeperRelay = new CloudGatekeeperRelay(cloud, relay);
		cloudGatekeeperRelay.setId(1);
		
		relay.getCloudGatekeepers().add(cloudGatekeeperRelay);
		cloud.getGatekeeperRelays().add(cloudGatekeeperRelay);
		
		when(relayRepository.getByIdWithCloudGatekeepers(anyLong())).thenReturn(Optional.of(relay));
		
		gatekeeperDBService.removeRelayById(1);
	}
	
	//=================================================================================================
	// Tests of getRelayByAuthenticationInfo
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAuthenticationInfoNullInput() {
		try {
			gatekeeperDBService.getRelayByAuthenticationInfo(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Authentication info is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAuthenticationInfoEmptyInput() {
		try {
			gatekeeperDBService.getRelayByAuthenticationInfo("");
		} catch (final Exception ex) {
			Assert.assertEquals("Authentication info is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetRelayByAuthenticationInfoRelayNotExists() {
		when(relayRepository.findByAuthenticationInfo(anyString())).thenReturn(Optional.empty());
		
		try {
			gatekeeperDBService.getRelayByAuthenticationInfo("test");
		} catch (final Exception ex) {
			Assert.assertEquals("Relay with the following authentication info not exists: test", ex.getMessage());
			
			verify(relayRepository, times(1)).findByAuthenticationInfo("test");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetRelayByAuthenticationInfoDBProblem() {
		when(relayRepository.findByAuthenticationInfo(anyString())).thenThrow(RuntimeException.class);
		
		try {
			gatekeeperDBService.getRelayByAuthenticationInfo("test");
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(relayRepository, times(1)).findByAuthenticationInfo("test");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByAuthenticationInfoOk() {
		final Relay relay = new Relay("localhost", 1234, "test", false, false, RelayType.GENERAL_RELAY);
		relay.setId(12);
		when(relayRepository.findByAuthenticationInfo(anyString())).thenReturn(Optional.of(relay));
		
		final Relay result = gatekeeperDBService.getRelayByAuthenticationInfo("test");
		
		Assert.assertEquals(result, relay);
			
		verify(relayRepository, times(1)).findByAuthenticationInfo("test");
	}
}
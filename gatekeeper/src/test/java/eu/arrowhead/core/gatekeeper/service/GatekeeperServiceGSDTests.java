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

package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@RunWith(SpringRunner.class)
public class GatekeeperServiceGSDTests {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperService gatekeeperService;
	
	@Mock
	private GatekeeperDBService gatekeeperDBService;
	
	@Mock
	private GatekeeperDriver gatekeeperDriver;
	
	@Mock
	private CommonDBService commonDBService;
	
	//=================================================================================================
	// methods
		
	//=================================================================================================
	// Tests of initGSDPoll
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitGSDPollOK() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "dfsgdfsgdfh");
		cloud.setCreatedAt(ZonedDateTime.now());
		cloud.setUpdatedAt(ZonedDateTime.now());
		
		final Cloud ownCloud = new Cloud("operatorOwn", "nameOwn", true, true, true, "fadgafdh");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(List.of(cloud));
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		when(gatekeeperDriver.sendGSDPollRequest(any(), any())).thenReturn(List.of(new GSDPollResponseDTO(DTOConverter.convertCloudToCloudResponseDTO(cloud), "test-service",
																		   								  List.of("HTTP-SECURE-JSON"), 2, null, null, false)));
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloud);
		
		final GSDQueryResultDTO result = gatekeeperService.initGSDPoll(gsdQueryFormDTO);
		
		assertEquals("operator", result.getResults().get(0).getProviderCloud().getOperator());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitGSDPollWithErrorMessageDTOInGSDPollAnswer() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final Cloud cloud1 = new Cloud("operator1", "name1", true, true, false, "dfsgdfsgdfh");
		cloud1.setCreatedAt(ZonedDateTime.now());
		cloud1.setUpdatedAt(ZonedDateTime.now());
		
		final Cloud cloud2 = new Cloud("operator2", "name2", true, true, false, "dsvbgasdg");
		cloud2.setCreatedAt(ZonedDateTime.now());
		cloud2.setUpdatedAt(ZonedDateTime.now());
		
		final Cloud ownCloud = new Cloud("operatorOwn", "nameOwn", true, true, true, "fadgafdh");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(List.of(cloud1, cloud2));
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		when(gatekeeperDriver.sendGSDPollRequest(any(), any())).thenReturn(List.of(new GSDPollResponseDTO(), new ErrorMessageDTO(new InvalidParameterException("test"))));
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloud1);
		
		gatekeeperService.initGSDPoll(gsdQueryFormDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitGSDPollWithoutPreferredAndNeighborClouds() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(new ArrayList<>());
		
		gatekeeperService.initGSDPoll(gsdQueryFormDTO);
	}
	
	//=================================================================================================
	// Tests of doGSDPoll
	
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullGSDPollRequestDTO() {
		gatekeeperService.doGSDPoll(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithMandatoryGatewayButNotPresentInRequesterCloud() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", true);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", true);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequestedService() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(null, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequestedServiceDefinition() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement(null);
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequestedServiceDefinition() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("   ");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloud() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, null, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloudOperator() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator(null);
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequesterCloudOperator() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("   ");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloudName() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName(null);
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequesterCloudName() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("    ");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithNoServiceRegistryQueryResults() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		when(gatekeeperDriver.sendServiceRegistryQuery(any())).thenReturn(srQueryResult);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
		
		assertNull(doGSDPollResponse.getProviderCloud());
		assertNull(doGSDPollResponse.getNumOfProviders());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithNoAuthResults() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO.setId(1);
		serviceRegistryResponseDTO.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO.setProvider(new SystemResponseDTO(1L, "test-provider-operator", "1.1.1.1", 1000, "test-provider-auth-info", "", ""));
		serviceRegistryResponseDTO.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO));
		when(gatekeeperDriver.sendServiceRegistryQuery(any())).thenReturn(srQueryResult);
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(new HashMap<>());
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
		
		assertNull(doGSDPollResponse.getProviderCloud());
		assertNull(doGSDPollResponse.getNumOfProviders());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollSRAndAuthCrossCheck() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO1 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO1.setId(1);
		serviceRegistryResponseDTO1.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO2.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", ""), new ServiceInterfaceResponseDTO(2L, "XML", "", "")));
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO1, serviceRegistryResponseDTO2));
		when(gatekeeperDriver.sendServiceRegistryQuery(any())).thenReturn(srQueryResult);
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(Map.of(2L,List.of(2L)));
		final Cloud ownCloud = new Cloud("own-c-operator", "own-c-name", true, true, true, "own-c-auth-info");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
		
		assertEquals(1, (int) doGSDPollResponse.getNumOfProviders());
		assertEquals(1, doGSDPollResponse.getAvailableInterfaces().size());
		assertEquals("XML", doGSDPollResponse.getAvailableInterfaces().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithQoSRequiredButNotEnabled() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(false);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, true));
		
		assertNull(doGSDPollResponse.getProviderCloud());
		assertNull(doGSDPollResponse.getRequiredServiceDefinition());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithReservedProvidersCrossCheckWithQoSNotRequired() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO1 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO1.setId(1);
		serviceRegistryResponseDTO1.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO2.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", ""), new ServiceInterfaceResponseDTO(2L, "XML", "", "")));
		
		final QoSReservationResponseDTO reservationResponseDTO = new QoSReservationResponseDTO();
		reservationResponseDTO.setReservedProviderId(serviceRegistryResponseDTO1.getId());
		reservationResponseDTO.setReservedServiceId(serviceRegistryResponseDTO1.getServiceDefinition().getId());
		reservationResponseDTO.setReservedTo(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusMinutes(15)));
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO1, serviceRegistryResponseDTO2));
		when(gatekeeperDriver.sendServiceRegistryQuery(any())).thenReturn(srQueryResult);
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(Map.of(1L,List.of(1L), 2L,List.of(1L, 2L)));
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(List.of(reservationResponseDTO));
		final Cloud ownCloud = new Cloud("own-c-operator", "own-c-name", true, true, true, "own-c-auth-info");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, false));
		
		assertEquals(1, (int) doGSDPollResponse.getNumOfProviders());
		assertEquals(2, doGSDPollResponse.getAvailableInterfaces().size());
		assertTrue(doGSDPollResponse.getAvailableInterfaces().contains("HTTP-SECURE-JSON"));
		assertTrue(doGSDPollResponse.getAvailableInterfaces().contains("XML"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithReservedProvidersCrossCheckWithQoSRequired() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO1 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO1.setId(1);
		serviceRegistryResponseDTO1.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO2.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", ""), new ServiceInterfaceResponseDTO(2L, "XML", "", "")));
		
		final QoSReservationResponseDTO reservationResponseDTO = new QoSReservationResponseDTO();
		reservationResponseDTO.setReservedProviderId(serviceRegistryResponseDTO1.getId());
		reservationResponseDTO.setReservedServiceId(serviceRegistryResponseDTO1.getServiceDefinition().getId());
		reservationResponseDTO.setReservedTo(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusMinutes(15)));
		
		final QoSIntraPingMeasurementResponseDTO measurementResponseDTO = new QoSIntraPingMeasurementResponseDTO();
		measurementResponseDTO.setId(1L);
		measurementResponseDTO.setAvailable(true);
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO1, serviceRegistryResponseDTO2));
		when(gatekeeperDriver.sendServiceRegistryQuery(any())).thenReturn(srQueryResult);
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(Map.of(1L,List.of(1L), 2L,List.of(1L, 2L)));
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(List.of(reservationResponseDTO));
		when(gatekeeperDriver.getQoSIntraPingMeasurementsForLocalSystem(eq(serviceRegistryResponseDTO2.getId()))).thenReturn(measurementResponseDTO);
		final Cloud ownCloud = new Cloud("own-c-operator", "own-c-name", true, true, true, "own-c-auth-info");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, true));
		
		assertEquals(1, (int) doGSDPollResponse.getNumOfProviders());
		assertEquals(2, doGSDPollResponse.getAvailableInterfaces().size());
		assertTrue(doGSDPollResponse.getAvailableInterfaces().contains("HTTP-SECURE-JSON"));
		assertTrue(doGSDPollResponse.getAvailableInterfaces().contains("XML"));
		assertEquals(1, doGSDPollResponse.getQosMeasurements().size());
		assertEquals(serviceRegistryResponseDTO2.getProvider().getSystemName(), doGSDPollResponse.getQosMeasurements().get(0).getServiceRegistryEntry().getProvider().getSystemName());
		assertTrue(doGSDPollResponse.getQosMeasurements().get(0).isProviderAvailable());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithQoSRequiredButNoMeasurementAvailable() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO1 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO1.setId(1);
		serviceRegistryResponseDTO1.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO2.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", ""), new ServiceInterfaceResponseDTO(2L, "XML", "", "")));
		
		final QoSIntraPingMeasurementResponseDTO measurementResponseDTO = new QoSIntraPingMeasurementResponseDTO();
		measurementResponseDTO.setId(1L);
		measurementResponseDTO.setAvailable(true);
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO1, serviceRegistryResponseDTO2));
		when(gatekeeperDriver.sendServiceRegistryQuery(any())).thenReturn(srQueryResult);
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(Map.of(1L,List.of(1L), 2L,List.of(1L, 2L)));
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(List.of());
		when(gatekeeperDriver.getQoSIntraPingMeasurementsForLocalSystem(eq(serviceRegistryResponseDTO1.getId()))).thenReturn(new QoSIntraPingMeasurementResponseDTO());
		when(gatekeeperDriver.getQoSIntraPingMeasurementsForLocalSystem(eq(serviceRegistryResponseDTO2.getId()))).thenReturn(measurementResponseDTO);
		final Cloud ownCloud = new Cloud("own-c-operator", "own-c-name", true, true, true, "own-c-auth-info");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false, true));
		
		assertEquals(1, (int) doGSDPollResponse.getNumOfProviders());
		assertEquals(2, doGSDPollResponse.getAvailableInterfaces().size());
		assertTrue(doGSDPollResponse.getAvailableInterfaces().contains("HTTP-SECURE-JSON"));
		assertTrue(doGSDPollResponse.getAvailableInterfaces().contains("XML"));
		assertEquals(1, doGSDPollResponse.getQosMeasurements().size());
		assertEquals(serviceRegistryResponseDTO2.getProvider().getSystemName(), doGSDPollResponse.getQosMeasurements().get(0).getServiceRegistryEntry().getProvider().getSystemName());
		assertTrue(doGSDPollResponse.getQosMeasurements().get(0).isProviderAvailable());
	}
}
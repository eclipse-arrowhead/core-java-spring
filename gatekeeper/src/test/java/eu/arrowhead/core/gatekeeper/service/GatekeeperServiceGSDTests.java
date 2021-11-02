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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryResultDTO;
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
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
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
		serviceRegistryResponseDTO.setProvider(new SystemResponseDTO(1L, "test-provider-operator", "1.1.1.1", 1000, "test-provider-auth-info", null, "", ""));
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
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", null, "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", null, "", ""));
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
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", null, "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", null, "", ""));
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
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", null, "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", null, "", ""));
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
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", null, "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", null, "", ""));
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
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitMultiGSDPollInputNull() throws InterruptedException {
		try {
			gatekeeperService.initMultiGSDPoll(null);
		} catch (final Exception ex) {
			assertEquals("GSDMultiQueryFormDTO is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitMultiGSDPollRequestedServicesNull() throws InterruptedException {
		try {
			gatekeeperService.initMultiGSDPoll(new GSDMultiQueryFormDTO());
		} catch (final Exception ex) {
			assertEquals("requestedServices list is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitMultiGSDPollRequestedServicesEmpty() throws InterruptedException {
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of());
		
		try {
			gatekeeperService.initMultiGSDPoll(form);
		} catch (final Exception ex) {
			assertEquals("requestedServices list is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitMultiGSDPollServiceDefinitionRequirementNull() throws InterruptedException {
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(new ServiceQueryFormDTO()));
		
		try {
			gatekeeperService.initMultiGSDPoll(form);
		} catch (final Exception ex) {
			assertEquals("serviceDefinitionRequirement is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitMultiGSDPollServiceDefinitionRequirementEmpty() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement(" ");
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryFormDTO));
		
		try {
			gatekeeperService.initMultiGSDPoll(form);
		} catch (final Exception ex) {
			assertEquals("serviceDefinitionRequirement is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitMultiGSDPollNoClouds() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("service");
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryFormDTO));
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(List.of());
		
		try {
			gatekeeperService.initMultiGSDPoll(form);
		} catch (final Exception ex) {
			assertEquals("initMultiGSDPoll failed: Neither preferred clouds were given, nor neighbor clouds registered.", ex.getMessage());
			
			verify(gatekeeperDBService, times(1)).getNeighborClouds();
			verify(gatekeeperDBService, never()).getCloudByOperatorAndName(anyString(), anyString());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitMultiGSDPollPreferredCloudsNotExists() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("service");
		final CloudRequestDTO preferredCloud = new CloudRequestDTO();
		preferredCloud.setName("name");
		preferredCloud.setOperator("operator");
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryFormDTO));
		form.setPreferredClouds(List.of(preferredCloud));
		
		when(gatekeeperDBService.getCloudByOperatorAndName("operator", "name")).thenThrow(new InvalidParameterException("not exists"));
		
		try {
			gatekeeperService.initMultiGSDPoll(form);
		} catch (final Exception ex) {
			assertEquals("initMultiGSDPoll failed: Given preferred clouds are not exists.", ex.getMessage());
			
			verify(gatekeeperDBService, never()).getNeighborClouds();
			verify(gatekeeperDBService, times(1)).getCloudByOperatorAndName("operator", "name");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitMultiGSDPollOk() throws InterruptedException {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("service");
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryFormDTO));
		form.setPreferredClouds(List.of());
		
		final Cloud cloud1 = new Cloud();
		cloud1.setName("name1");
		cloud1.setOperator("operator1");
		final Cloud cloud2 = new Cloud();
		cloud2.setName("name2");
		cloud2.setOperator("operator2");
		final Cloud cloud3 = new Cloud();
		cloud3.setName("name3");
		cloud3.setOperator("operator3");
				
		final Cloud cloudInLocalDB = new Cloud("operator3", "name3", true, true, false, null);
		cloudInLocalDB.setCreatedAt(ZonedDateTime.now());
		cloudInLocalDB.setUpdatedAt(ZonedDateTime.now());
		
		final GSDMultiPollResponseDTO validResponse = new GSDMultiPollResponseDTO();
		validResponse.setProvidedServiceDefinitions(List.of("service"));
		validResponse.setProviderCloud(new CloudResponseDTO(123, "operator3", "name3", true, false, true, null, null, null));
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(List.of(cloud1, cloud2, cloud3));
		when(commonDBService.getOwnCloud(true)).thenReturn(new Cloud());
		when(gatekeeperDriver.sendMultiGSDPollRequest(anyList(), any(GSDMultiPollRequestDTO.class))).thenReturn(List.of(new ErrorMessageDTO("error", 500, null, null), new GSDMultiPollResponseDTO(), validResponse));
		when(gatekeeperDBService.getCloudByOperatorAndName("operator3", "name3")).thenReturn(cloudInLocalDB);
		
		final GSDMultiQueryResultDTO result = gatekeeperService.initMultiGSDPoll(form);
		
		assertEquals(2, result.getUnsuccessfulRequests());
		assertEquals(1, result.getResults().size());
		assertEquals("operator3", result.getResults().get(0).getProviderCloud().getOperator());
		assertEquals("name3", result.getResults().get(0).getProviderCloud().getName());

		verify(gatekeeperDBService, times(1)).getNeighborClouds();
		verify(commonDBService, times(1)).getOwnCloud(true);
		verify(gatekeeperDriver, times(1)).sendMultiGSDPollRequest(anyList(), any(GSDMultiPollRequestDTO.class));
		verify(gatekeeperDBService, times(1)).getCloudByOperatorAndName("operator3", "name3");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequestNull() {
		try {
			gatekeeperService.doMultiGSDPoll(null);
		} catch (final Exception ex) {
			assertEquals("GSDMultiPollRequestDTO is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollGatewayProblem() {
		final boolean oldValue = (Boolean) ReflectionTestUtils.getField(gatekeeperService, "gatewayIsMandatory");
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", true);
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(false);
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("Requester cloud must have gateway available", ex.getMessage());
			
			throw ex;
		} finally {
			ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", oldValue);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequestedServicesNull() {
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("RequestedServices list is null or empty", ex.getMessage());
			
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequestedServicesEmpty() {
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of());
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("RequestedServices list is null or empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollServiceDefinitionRequirementNull() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("serviceDefinitionRequirement is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollServiceDefinitionRequirementEmpty() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement(" ");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("serviceDefinitionRequirement is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequesterCloudNull() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement("service");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("RequesterCloud is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequesterCloudOperatorNull() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement("service");
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setName("name");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		request.setRequesterCloud(requesterCloud);
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("GSDMultiPollRequestDTO.CloudRequestDTO is invalid due to the following reasons: operator is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequesterCloudOperatorEmpty() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement("service");
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator(" ");
		requesterCloud.setName("name");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		request.setRequesterCloud(requesterCloud);
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("GSDMultiPollRequestDTO.CloudRequestDTO is invalid due to the following reasons: operator is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequesterCloudNameNull() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement("service");
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("operator");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		request.setRequesterCloud(requesterCloud);
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("GSDMultiPollRequestDTO.CloudRequestDTO is invalid due to the following reasons: name is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoMultiGSDPollRequesterCloudNameEmpty() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement("service");
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("operator");
		requesterCloud.setName(" ");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		request.setRequesterCloud(requesterCloud);
		
		try {
			gatekeeperService.doMultiGSDPoll(request);
		} catch (final Exception ex) {
			assertEquals("GSDMultiPollRequestDTO.CloudRequestDTO is invalid due to the following reasons: name is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoMultiGSDPollOk() {
		final ServiceQueryFormDTO formDTO = new ServiceQueryFormDTO();
		formDTO.setServiceDefinitionRequirement("service");
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("operator");
		requesterCloud.setName("name");
		final GSDMultiPollRequestDTO request = new GSDMultiPollRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequestedServices(List.of(formDTO));
		request.setRequesterCloud(requesterCloud);
		
		final ServiceQueryResultDTO queryResult = new ServiceQueryResultDTO();
		queryResult.setServiceQueryData(List.of(new ServiceRegistryResponseDTO()));
		final ServiceQueryResultListDTO queryList = new ServiceQueryResultListDTO();
		queryList.setResults(List.of(queryResult));
		
		final Cloud ownCloud = new Cloud("operator2", "name2", true, false, true, null);
		ownCloud.setId(111);
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		
		when(gatekeeperDriver.sendServiceRegistryMultiQuery(any(ServiceQueryFormListDTO.class))).thenReturn(queryList);
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(anyList(), any(CloudRequestDTO.class), eq("service"))).thenReturn(Map.of(1L, List.of()));
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		
		final GSDMultiPollResponseDTO result = gatekeeperService.doMultiGSDPoll(request);
		
		assertEquals("operator2", result.getProviderCloud().getOperator());
		assertEquals("name2", result.getProviderCloud().getName());
		assertEquals("service", result.getProvidedServiceDefinitions().get(0));
		
		verify(gatekeeperDriver, times(1)).sendServiceRegistryMultiQuery(any(ServiceQueryFormListDTO.class));
		verify(gatekeeperDriver, times(1)).sendInterCloudAuthorizationCheckQuery(anyList(), any(CloudRequestDTO.class), eq("service"));
		verify(commonDBService, times(1)).getOwnCloud(true);
	}
}
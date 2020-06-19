/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class QoSMonitorDriverTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private QoSMonitorDriver testingObject;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private HttpService httpService;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryAllUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		testingObject.queryServiceRegistryAll();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceRegistryAllOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(ServiceRegistryListResponseDTO.class))).thenReturn(new ResponseEntity<>(new ServiceRegistryListResponseDTO(), HttpStatus.OK));
		
		testingObject.queryServiceRegistryAll();
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(ServiceRegistryListResponseDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperAllCloudUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		testingObject.queryGatekeeperAllCloud();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryGatekeeperAllCloudOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(CloudWithRelaysAndPublicRelaysListResponseDTO.class))).thenReturn(new ResponseEntity<>(new CloudWithRelaysAndPublicRelaysListResponseDTO(), HttpStatus.OK));
		
		testingObject.queryGatekeeperAllCloud();
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(CloudWithRelaysAndPublicRelaysListResponseDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperAllSystemAddressesUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		testingObject.queryGatekeeperAllSystemAddresses(new CloudRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperAllSystemAddressesNullCloud() {
		testingObject.queryGatekeeperAllSystemAddresses(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryGatekeeperAllSystemAddressesOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(SystemAddressSetRelayResponseDTO.class), any(CloudRequestDTO.class))).thenReturn(new ResponseEntity<>(new SystemAddressSetRelayResponseDTO(), HttpStatus.OK));
		
		testingObject.queryGatekeeperAllSystemAddresses(new CloudRequestDTO());
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(SystemAddressSetRelayResponseDTO.class), any(CloudRequestDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperCloudAccessTypesUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		testingObject.queryGatekeeperCloudAccessTypes(List.of(new CloudRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudAccessTypesNullCloudList() {
		testingObject.queryGatekeeperCloudAccessTypes(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryGatekeeperCloudAccessTypesOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(CloudAccessListResponseDTO.class), any(List.class))).thenReturn(new ResponseEntity<>(new CloudAccessListResponseDTO(), HttpStatus.OK));
		
		testingObject.queryGatekeeperCloudAccessTypes(List.of(new CloudRequestDTO()));
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(CloudAccessListResponseDTO.class), any(List.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoOperatorNull() {
		testingObject.queryGatekeeperCloudInfo(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoOperatorEmpty() {
		testingObject.queryGatekeeperCloudInfo("", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoNameNull() {
		testingObject.queryGatekeeperCloudInfo("aitia", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoNameEmpty() {
		testingObject.queryGatekeeperCloudInfo("aitia", " ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperCloudInfoUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		
		testingObject.queryGatekeeperCloudInfo("aitia", "testcloud");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperCloudInfoUriWrongType() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn("not an uri object");
		
		testingObject.queryGatekeeperCloudInfo("aitia", "testcloud");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryGatekeeperCloudInfoOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(CloudWithRelaysResponseDTO.class))).thenReturn(new ResponseEntity<>(new CloudWithRelaysResponseDTO(), HttpStatus.OK));
		
		testingObject.queryGatekeeperCloudInfo("aitia", "testcloud");
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(CloudWithRelaysResponseDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testRequestGatekeeperInitRelayTestUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		testingObject.requestGatekeeperInitRelayTest(new QoSRelayTestProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRequestGatekeeperInitRelayTestNullRequest() {
		testingObject.requestGatekeeperInitRelayTest(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRequestGatekeeperInitRelayTestNullRequestOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(QoSRelayTestProposalRequestDTO.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
		
		testingObject.requestGatekeeperInitRelayTest(new QoSRelayTestProposalRequestDTO());
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(QoSRelayTestProposalRequestDTO.class));
	}
}
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperDriver.GSDMultiPollRequestExecutorFactory;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

@RunWith(SpringRunner.class)
public class GatekeeperDriverGSDTest {
	
	//=================================================================================================
	// members
		
	@InjectMocks
	private GatekeeperDriver testingObject;
	
	@Mock
	private RelayMatchmakingAlgorithm gatekeeperMatchmaker;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private HttpService httpService;
	
	@Mock
	private SSLProperties sslProps;
	
	@Mock
	private GatekeeperRelayClient relayClient;
	
	@Mock
	private GSDMultiPollRequestExecutorFactory multiGSDExecutorFactory;

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullCloudList() throws InterruptedException {
		testingObject.sendGSDPollRequest(null, getGSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithEmptyCloudList() throws InterruptedException {
		testingObject.sendGSDPollRequest(new ArrayList<>(), getGSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullGSDPollRequestDTO() throws InterruptedException {
		testingObject.sendGSDPollRequest(List.of(new Cloud()), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequestedService() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.setRequestedService(null);
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequestedServiceDefinition() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.getRequestedService().setServiceDefinitionRequirement(null);
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithBlankRequestedServiceDefinition() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.getRequestedService().setServiceDefinitionRequirement("   ");
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO); 
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequesterCloud() throws InterruptedException {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.setRequesterCloud(null);;
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendServiceReistryQueryNullQueryForm() {
		testingObject.sendServiceRegistryQuery(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullQueryDataList() {
		testingObject.sendInterCloudAuthorizationCheckQuery(null, new CloudRequestDTO(), "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullCloud() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), null, "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullServiceDefinition() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), new CloudRequestDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithBlankServiceDefinition() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), new CloudRequestDTO(), "  ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendServiceRegistryMultiQueryNullInput() {
		try {
			testingObject.sendServiceRegistryMultiQuery(null);
		} catch (final Exception ex) {
			Assert.assertEquals("ServiceQueryFormListDTO is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendServiceRegistryMultiQueryNoUri() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(false);
		
		try {
			testingObject.sendServiceRegistryMultiQuery(new ServiceQueryFormListDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Gatekeeper can't find Service Registry multi-query URI.", ex.getMessage());
			
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendServiceRegistryMultiQueryWrongUriType() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn("uri");
		
		try {
			testingObject.sendServiceRegistryMultiQuery(new ServiceQueryFormListDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Gatekeeper can't find Service Registry multi-query URI.", ex.getMessage());

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendServiceRegistryMultiQueryOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "abc");
		
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI)).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<>(new ServiceQueryResultListDTO(), HttpStatus.OK));
		
		final ServiceQueryResultListDTO result = testingObject.sendServiceRegistryMultiQuery(new ServiceQueryFormListDTO());
		
		Assert.assertNotNull(result);
		
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_MULTI_QUERY_URI);
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestNullCloudList() throws InterruptedException {
		try {
			testingObject.sendMultiGSDPollRequest(null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("cloudsToContact list is null or empty", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestEmptyCloudList() throws InterruptedException {
		try {
			testingObject.sendMultiGSDPollRequest(List.of(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("cloudsToContact list is null or empty", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestDTONull() throws InterruptedException {
		try {
			testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), null);
		} catch (final Exception ex) {
			Assert.assertEquals("gsdPollRequestDTO is null", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestRequestedServicesNull() throws InterruptedException {
		try {
			testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), new GSDMultiPollRequestDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("requestedServices list is null or empty", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestRequestedServicesEmpty() throws InterruptedException {
		final GSDMultiPollRequestDTO requestDTO = new GSDMultiPollRequestDTO();
		requestDTO.setRequestedServices(List.of());

		try {
			testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), requestDTO);
		} catch (final Exception ex) {
			Assert.assertEquals("requestedServices list is null or empty", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestServiceDefinitionRequirementNull() throws InterruptedException {
		final GSDMultiPollRequestDTO requestDTO = new GSDMultiPollRequestDTO();
		requestDTO.setRequestedServices(List.of(new ServiceQueryFormDTO()));

		try {
			testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), requestDTO);
		} catch (final Exception ex) {
			Assert.assertEquals("serviceDefinitionRequirement is null or empty", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestServiceDefinitionRequirementEmpty() throws InterruptedException {
		final ServiceQueryFormDTO queryFormDTO = new ServiceQueryFormDTO();
		queryFormDTO.setServiceDefinitionRequirement(" ");
		final GSDMultiPollRequestDTO requestDTO = new GSDMultiPollRequestDTO();
		requestDTO.setRequestedServices(List.of(queryFormDTO));

		try {
			testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), requestDTO);
		} catch (final Exception ex) {
			Assert.assertEquals("serviceDefinitionRequirement is null or empty", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendMultiGSDPollRequestRequesterCloudNull() throws InterruptedException {
		final ServiceQueryFormDTO queryFormDTO = new ServiceQueryFormDTO();
		queryFormDTO.setServiceDefinitionRequirement("service");
		final GSDMultiPollRequestDTO requestDTO = new GSDMultiPollRequestDTO();
		requestDTO.setRequestedServices(List.of(queryFormDTO));

		try {
			testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), requestDTO);
		} catch (final Exception ex) {
			Assert.assertEquals("requesterCloud is null", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testSendMultiGSDPollRequestOk() throws InterruptedException {
		final ServiceQueryFormDTO queryFormDTO = new ServiceQueryFormDTO();
		queryFormDTO.setServiceDefinitionRequirement("service");
		final GSDMultiPollRequestDTO requestDTO = new GSDMultiPollRequestDTO();
		requestDTO.setRequestedServices(List.of(queryFormDTO));
		requestDTO.setRequesterCloud(new CloudRequestDTO());
		
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(new Relay());
		when(multiGSDExecutorFactory.newExecutor(any(BlockingQueue.class), any(GatekeeperRelayClient.class), any(GSDMultiPollRequestDTO.class), anyMap())).thenAnswer(new Answer<GSDMultiPollRequestExecutor>() {
			@Override
			public GSDMultiPollRequestExecutor answer(final InvocationOnMock invocation) throws Throwable {
				final BlockingQueue<ErrorWrapperDTO> queue = invocation.getArgument(0);
				return getDummyGSDMultiPollRequestExecutor(queue);
			}
		});

		final List<ErrorWrapperDTO> result = testingObject.sendMultiGSDPollRequest(List.of(new Cloud()), requestDTO);
		
		verify(gatekeeperMatchmaker, times(1)).doMatchmaking(any(RelayMatchmakingParameters.class));
		verify(multiGSDExecutorFactory, times(1)).newExecutor(any(BlockingQueue.class), any(GatekeeperRelayClient.class), any(GSDMultiPollRequestDTO.class), anyMap());
		
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(GSDPollResponseDTO.class, result.get(0).getClass());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private GSDPollRequestDTO getGSDPollRequestDTO() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		
		return new GSDPollRequestDTO(serviceQueryFormDTO, cloudRequestDTO, false, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	private GSDMultiPollRequestExecutor getDummyGSDMultiPollRequestExecutor(final BlockingQueue<ErrorWrapperDTO> queue) {
		return new GSDMultiPollRequestExecutor(queue, relayClient, null, Map.of(new Cloud(), new Relay())) {
			
			@Override
			public void execute() {
				queue.add(new GSDPollResponseDTO());
			}
		};
	}
}
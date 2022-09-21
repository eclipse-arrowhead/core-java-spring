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

package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.dto.internal.TokenDataDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class OrchestratorDriverTest {
	
	//=================================================================================================
	//  members
	
	@InjectMocks
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private HttpService httpService;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceReqistryFormNull() {
		orchestratorDriver.queryServiceRegistry(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceReqistryFlagsNull() {
		try {
			orchestratorDriver.queryServiceRegistry(new ServiceQueryFormDTO(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("Flags is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryNoAddressTypeFlagSetNoPreviousFormSetting() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		final OrchestrationFlags flags = new OrchestrationFlags();
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertNull(form.getProviderAddressTypeRequirements());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryNoAddressTypeFlagSetFormSettingPreserved() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setProviderAddressTypeRequirements(List.of(AddressType.HOSTNAME));
		final OrchestrationFlags flags = new OrchestrationFlags();
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertEquals(1, form.getProviderAddressTypeRequirements().size());
			Assert.assertEquals(AddressType.HOSTNAME, form.getProviderAddressTypeRequirements().get(0));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryOnlyIPv4Set() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_IPV4_ADDRESS_RESPONSE, true);
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertEquals(1, form.getProviderAddressTypeRequirements().size());
			Assert.assertEquals(AddressType.IPV4, form.getProviderAddressTypeRequirements().get(0));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryOnlyIPv6Set() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_IPV6_ADDRESS_RESPONSE, true);
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertEquals(1, form.getProviderAddressTypeRequirements().size());
			Assert.assertEquals(AddressType.IPV6, form.getProviderAddressTypeRequirements().get(0));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryOnlyIPSet() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_IP_ADDRESS_RESPONSE, true);
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertEquals(2, form.getProviderAddressTypeRequirements().size());
			Assert.assertTrue(form.getProviderAddressTypeRequirements().contains(AddressType.IPV6));
			Assert.assertTrue(form.getProviderAddressTypeRequirements().contains(AddressType.IPV4));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryBothIPv4AndIPv6Set() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_IPV4_ADDRESS_RESPONSE, true);
		flags.put(Flag.ONLY_IPV6_ADDRESS_RESPONSE, true);
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertEquals(2, form.getProviderAddressTypeRequirements().size());
			Assert.assertTrue(form.getProviderAddressTypeRequirements().contains(AddressType.IPV6));
			Assert.assertTrue(form.getProviderAddressTypeRequirements().contains(AddressType.IPV4));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryAllIPRelatedFlagSet() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false); // just to make sure the method stops without actual HTTP request
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_IPV4_ADDRESS_RESPONSE, true);
		flags.put(Flag.ONLY_IPV6_ADDRESS_RESPONSE, true);
		flags.put(Flag.ONLY_IP_ADDRESS_RESPONSE, true);
		
		try {
			orchestratorDriver.queryServiceRegistry(form, flags);
		} catch (final Exception ex) {
			Assert.assertEquals(2, form.getProviderAddressTypeRequirements().size());
			Assert.assertTrue(form.getProviderAddressTypeRequirements().contains(AddressType.IPV6));
			Assert.assertTrue(form.getProviderAddressTypeRequirements().contains(AddressType.IPV4));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryQueryUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.queryServiceRegistry(new ServiceQueryFormDTO(), new OrchestrationFlags());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryQueryUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.queryServiceRegistry(new ServiceQueryFormDTO(), new OrchestrationFlags());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGenerateAuthTokensRequestNull() {
		orchestratorDriver.generateAuthTokens(null, null);
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGenerateAuthTokensORListNull() {
		orchestratorDriver.generateAuthTokens(new OrchestrationFormRequestDTO(), null);
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateAuthTokensNoNeedForAuthTokensBecauseOfEmptyORList() {
		final List<OrchestrationResultDTO> result = orchestratorDriver.generateAuthTokens(new OrchestrationFormRequestDTO(), List.of());

		Assert.assertEquals(true, result.isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateAuthTokensNoNeedForAuthTokensBecauseOfSecurityType() {
		final OrchestrationResultDTO dto1 = new OrchestrationResultDTO();
		dto1.setSecure(ServiceSecurityType.CERTIFICATE);
		final OrchestrationResultDTO dto2 = new OrchestrationResultDTO();
		dto2.setSecure(ServiceSecurityType.NOT_SECURE);
		final List<OrchestrationResultDTO> result = orchestratorDriver.generateAuthTokens(new OrchestrationFormRequestDTO(), List.of(dto1, dto2));
		
		Assert.assertEquals(2, result.size());
		Assert.assertNull(result.get(0).getAuthorizationTokens());
		Assert.assertNull(result.get(1).getAuthorizationTokens());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGenerateAuthTokensUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		final OrchestrationResultDTO dto = getOrchestrationResultDTO(1);
		
		orchestratorDriver.generateAuthTokens(new OrchestrationFormRequestDTO(), List.of(dto));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGenerateAuthTokensUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		final OrchestrationResultDTO dto = getOrchestrationResultDTO(1);
		
		orchestratorDriver.generateAuthTokens(new OrchestrationFormRequestDTO(), List.of(dto));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateAuthTokensTokenIsFilled() {
		final OrchestrationResultDTO dto = getOrchestrationResultDTO(1);
		final UriComponents tokenUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8445, "/token");
		final Map<String,String> tokenMap = Map.of(dto.getInterfaces().get(0).getInterfaceName(), "ABCDE");
		final TokenDataDTO tokenDataDTO = new TokenDataDTO(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(dto.getProvider()), tokenMap);
		final TokenGenerationResponseDTO responseDTO = new TokenGenerationResponseDTO();
		responseDTO.setTokenData(List.of(tokenDataDTO));

		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(tokenUri);
		when(httpService.sendRequest(eq(tokenUri), eq(HttpMethod.POST), eq(TokenGenerationResponseDTO.class), any(TokenGenerationRequestDTO.class))).
																												thenReturn(new ResponseEntity<TokenGenerationResponseDTO>(responseDTO, HttpStatus.OK));
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(new SystemRequestDTO());
		
		orchestratorDriver.generateAuthTokens(request, List.of(dto));
		
		Assert.assertEquals("ABCDE", dto.getAuthorizationTokens().get(dto.getInterfaces().get(0).getInterfaceName()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateAuthTokensTokenNoCalculatedServiceTimeFrame() {
		ReflectionTestUtils.setField(orchestratorDriver, "tokenDuration", 1001);
		
		final OrchestrationResultDTO dto = getOrchestrationResultDTO(1);
		final UriComponents tokenUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8445, "/token");
		final Map<String,String> tokenMap = Map.of(dto.getInterfaces().get(0).getInterfaceName(), "ABCDE");
		final TokenDataDTO tokenDataDTO = new TokenDataDTO(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(dto.getProvider()), tokenMap);
		final TokenGenerationResponseDTO responseDTO = new TokenGenerationResponseDTO();
		responseDTO.setTokenData(List.of(tokenDataDTO));

		final ArgumentCaptor<TokenGenerationRequestDTO> captor = ArgumentCaptor.forClass(TokenGenerationRequestDTO.class);
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(tokenUri);
		when(httpService.sendRequest(eq(tokenUri), eq(HttpMethod.POST), eq(TokenGenerationResponseDTO.class), captor.capture())).
																												thenReturn(new ResponseEntity<TokenGenerationResponseDTO>(responseDTO, HttpStatus.OK));
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(new SystemRequestDTO());
		
		orchestratorDriver.generateAuthTokens(request, List.of(dto));
		
		final TokenGenerationRequestDTO requestDTO = captor.getValue();
		Assert.assertEquals(1001, requestDTO.getProviders().get(0).getTokenDuration());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateAuthTokensTokenInvalidCalculatedServiceTimeFrame() {
		ReflectionTestUtils.setField(orchestratorDriver, "tokenDuration", 1000);
		
		final OrchestrationResultDTO dto = getOrchestrationResultDTO(1);
		dto.getMetadata().put(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME, "300invalid");
		final UriComponents tokenUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8445, "/token");
		final Map<String,String> tokenMap = Map.of(dto.getInterfaces().get(0).getInterfaceName(), "ABCDE");
		final TokenDataDTO tokenDataDTO = new TokenDataDTO(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(dto.getProvider()), tokenMap);
		final TokenGenerationResponseDTO responseDTO = new TokenGenerationResponseDTO();
		responseDTO.setTokenData(List.of(tokenDataDTO));

		final ArgumentCaptor<TokenGenerationRequestDTO> captor = ArgumentCaptor.forClass(TokenGenerationRequestDTO.class);
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(tokenUri);
		when(httpService.sendRequest(eq(tokenUri), eq(HttpMethod.POST), eq(TokenGenerationResponseDTO.class), captor.capture())).
																												thenReturn(new ResponseEntity<TokenGenerationResponseDTO>(responseDTO, HttpStatus.OK));
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(new SystemRequestDTO());
		
		orchestratorDriver.generateAuthTokens(request, List.of(dto));
		
		final TokenGenerationRequestDTO requestDTO = captor.getValue();
		Assert.assertEquals(1000, requestDTO.getProviders().get(0).getTokenDuration());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateAuthTokensTokenValidCalculatedServiceTimeFrame() {
		ReflectionTestUtils.setField(orchestratorDriver, "tokenDuration", 1000);
		
		final OrchestrationResultDTO dto = getOrchestrationResultDTO(1);
		dto.getMetadata().put(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME, "300");
		final UriComponents tokenUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8445, "/token");
		final Map<String,String> tokenMap = Map.of(dto.getInterfaces().get(0).getInterfaceName(), "ABCDE");
		final TokenDataDTO tokenDataDTO = new TokenDataDTO(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(dto.getProvider()), tokenMap);
		final TokenGenerationResponseDTO responseDTO = new TokenGenerationResponseDTO();
		responseDTO.setTokenData(List.of(tokenDataDTO));

		final ArgumentCaptor<TokenGenerationRequestDTO> captor = ArgumentCaptor.forClass(TokenGenerationRequestDTO.class);
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(tokenUri);
		when(httpService.sendRequest(eq(tokenUri), eq(HttpMethod.POST), eq(TokenGenerationResponseDTO.class), captor.capture())).
																												thenReturn(new ResponseEntity<TokenGenerationResponseDTO>(responseDTO, HttpStatus.OK));
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(new SystemRequestDTO());
		
		orchestratorDriver.generateAuthTokens(request, List.of(dto));
		
		final TokenGenerationRequestDTO requestDTO = captor.getValue();
		Assert.assertEquals(300, requestDTO.getProviders().get(0).getTokenDuration());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryAuthorizationConsumerNull() {
		orchestratorDriver.queryAuthorization(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryAuthorizationProvidersNull() {
		orchestratorDriver.queryAuthorization(new SystemRequestDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryAuthorizationUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.queryAuthorization(new SystemRequestDTO(), List.of(new ServiceRegistryResponseDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryAuthorizationUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.queryAuthorization(new SystemRequestDTO(), List.of(new ServiceRegistryResponseDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryAuthorizationFilter() {
		final UriComponents checkUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8445, "/intracloud/check");
		final AuthorizationIntraCloudCheckResponseDTO responseDTO = new AuthorizationIntraCloudCheckResponseDTO(new SystemResponseDTO(), 1, List.of(new IdIdListDTO(2L, List.of(1L))));
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(checkUri);
		when(httpService.sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationIntraCloudCheckResponseDTO.class), any(AuthorizationIntraCloudCheckRequestDTO.class))).
																									thenReturn(new ResponseEntity<AuthorizationIntraCloudCheckResponseDTO>(responseDTO, HttpStatus.OK));
		
		final List<ServiceRegistryResponseDTO> srResults = getSRResults();
		final List<ServiceRegistryResponseDTO> afterAuthorization = orchestratorDriver.queryAuthorization(new SystemRequestDTO(), srResults);
		
		Assert.assertEquals(1, afterAuthorization.size());
		Assert.assertEquals(1, srResults.size());
		Assert.assertEquals(2, afterAuthorization.get(0).getProvider().getId());
		Assert.assertEquals(1, afterAuthorization.get(0).getInterfaces().size());
		Assert.assertEquals(1, afterAuthorization.get(0).getInterfaces().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceRegistryBySystemIdOk() {
		final UriComponents queryBySystemIdUriBeforeExpand = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8443, CommonConstants.SERVICEREGISTRY_URI +
																	 CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_URI);
		final UriComponents queryBySystemIdUriForTestRequest = queryBySystemIdUriBeforeExpand.expand(Map.of(CoreCommonConstants.COMMON_FIELD_NAME_ID, String.valueOf(1)));
		
		final SystemResponseDTO responseDTO = new SystemResponseDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(queryBySystemIdUriBeforeExpand);
		when(httpService.sendRequest(eq(queryBySystemIdUriForTestRequest), eq(HttpMethod.GET), eq(SystemResponseDTO.class))).thenReturn(new ResponseEntity<SystemResponseDTO>(responseDTO,
																																											  HttpStatus.OK));
		
		final SystemResponseDTO systemResponseDTO = orchestratorDriver.queryServiceRegistryBySystemId(1L);
		
		Assert.assertNotNull(systemResponseDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryBySystemIdApplicationContextNotContainingKey() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.queryServiceRegistryBySystemId(1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryBySystemIdApplicationContextReturningIncorrectClass() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(new Object());
		
		orchestratorDriver.queryServiceRegistryBySystemId(1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryByIdWithInvalidSystemId() {
		orchestratorDriver.queryServiceRegistryBySystemId(-1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceRegistryBySystemRequestDTOOk() {
		final UriComponents queryBySystemDTOUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8443, CommonConstants.SERVICEREGISTRY_URI +
																	  CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI);
		
		final SystemResponseDTO responseDTO = new SystemResponseDTO();
		final SystemRequestDTO requestDTO = new SystemRequestDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(queryBySystemDTOUri);
		when(httpService.sendRequest(eq(queryBySystemDTOUri), eq(HttpMethod.POST), eq(SystemResponseDTO.class), any(SystemRequestDTO.class))).thenReturn(
																																	new ResponseEntity<SystemResponseDTO>(responseDTO, HttpStatus.OK));
		
		final SystemResponseDTO systemResponseDTO = orchestratorDriver.queryServiceRegistryBySystemRequestDTO(requestDTO);
		
		Assert.assertNotNull(systemResponseDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryBySystemRequestDTOContextNotCointainingKey() {
		final SystemRequestDTO requestDTO = new SystemRequestDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.queryServiceRegistryBySystemRequestDTO(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryBySystemRequestDTOApplicationContextReturningIncorrectClass() {
		final SystemRequestDTO requestDTO = new SystemRequestDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(new Object());
		
		orchestratorDriver.queryServiceRegistryBySystemRequestDTO(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryBySystemRequestDTONullDTO() {
		orchestratorDriver.queryServiceRegistryBySystemRequestDTO(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGlobalServiceDiscoveryOk() {
		final UriComponents queryGSDUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8449, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GSD_SERVICE);
		
		final GSDQueryResultDTO responseDTO = new GSDQueryResultDTO(List.of(new GSDPollResponseDTO()), 1);
		final GSDQueryFormDTO requestDTO = new GSDQueryFormDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(queryGSDUri);
		when(httpService.sendRequest(eq(queryGSDUri), eq(HttpMethod.POST), eq(GSDQueryResultDTO.class), any(GSDQueryFormDTO.class))).thenReturn(new ResponseEntity<GSDQueryResultDTO>(responseDTO,
																																													  HttpStatus.OK));
		
		final GSDQueryResultDTO gsdQueryResultDTO = orchestratorDriver.doGlobalServiceDiscovery(requestDTO);

		Assert.assertNotNull(gsdQueryResultDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDoGlobalServiceDiscoveryContextNotContainsKey() {
		final GSDQueryFormDTO requestDTO = new GSDQueryFormDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.doGlobalServiceDiscovery(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDoGlobalServiceDiscoveryContextReturnNotCorrectClass() {
		final GSDQueryFormDTO requestDTO = new GSDQueryFormDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(new Object());
		
		orchestratorDriver.doGlobalServiceDiscovery(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testDoGlobalServiceDiscoveryNullRequest() {
		orchestratorDriver.doGlobalServiceDiscovery(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoInterCloudNegotiationsOk() {
		final UriComponents queryICNUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8449, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_ICN_SERVICE);
		
		final ICNResultDTO responseDTO = new ICNResultDTO();
		final ICNRequestFormDTO requestDTO = new ICNRequestFormDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(queryICNUri);
		when(httpService.sendRequest(eq(queryICNUri), eq(HttpMethod.POST), eq(ICNResultDTO.class), any(ICNRequestFormDTO.class))).thenReturn(new ResponseEntity<ICNResultDTO>(responseDTO,
																																											  HttpStatus.OK));
		
		final ICNResultDTO icnResultDTO = orchestratorDriver.doInterCloudNegotiation(requestDTO);
		
		Assert.assertNotNull(icnResultDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDoInterCloudNegotiationsNotContainsKey() {
		final ICNRequestFormDTO requestDTO = new ICNRequestFormDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.doInterCloudNegotiation(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDoInterCloudNegotiationsContextReturnsNotCorrectClass() {
		final ICNRequestFormDTO requestDTO = new ICNRequestFormDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(new Object());
		
		orchestratorDriver.doInterCloudNegotiation(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testDoInterCloudNegotiationsNullRequest() {
		orchestratorDriver.doInterCloudNegotiation(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudsWithExclusiveGatewayAndPublicRelaysOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8449, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GET_CLOUD_SERVICE + 
																								"/test-op/test-n");
		final CloudWithRelaysResponseDTO responseDTO = new CloudWithRelaysResponseDTO();
		
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(uri);
		when(httpService.sendRequest(eq(uri), eq(HttpMethod.GET), eq(CloudWithRelaysResponseDTO.class))).thenReturn(new ResponseEntity<CloudWithRelaysResponseDTO>(responseDTO, HttpStatus.OK));
		
		final CloudWithRelaysResponseDTO result = orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays("test-op", "test-n");
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetCloudsWithExclusiveGatewayAndPublicRelaysUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays("test-op", "test-n");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetCloudsWithExclusiveGatewayAndPublicRelaysUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.getCloudsWithExclusiveGatewayAndPublicRelays("test-op", "test-n");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetIntraPingMeasurementUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.getIntraPingMeasurement(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetIntraPingMeasurementUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.getIntraPingMeasurement(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraPingMeasurementOk() {
		final int systemId = 23;
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8451, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + 
				 									  CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT_SUFFIX).expand(systemId);
		Assert.assertTrue(uri.toString().contains("/measurements/intracloud/ping"));
		
		final QoSIntraPingMeasurementResponseDTO responseDTO = new QoSIntraPingMeasurementResponseDTO();
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(uri);
		when(httpService.sendRequest(eq(uri), eq(HttpMethod.GET), eq(QoSIntraPingMeasurementResponseDTO.class))).thenReturn(new ResponseEntity<QoSIntraPingMeasurementResponseDTO>(responseDTO, HttpStatus.OK));
		
		final QoSIntraPingMeasurementResponseDTO result = orchestratorDriver.getIntraPingMeasurement(systemId);
		
		Assert.assertNotNull(result);
		Assert.assertFalse(result.hasRecord());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetIntraPingMedianMeasurementUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.getIntraPingMedianMeasurement(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetIntraPingMedianMeasurementUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.getIntraPingMedianMeasurement(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraPingMedianMeasurementOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8451, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT)
										   .expand(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT.name());
		Assert.assertTrue(uri.toString().contains("/measurements/intracloud/ping_median"));
		
		final QoSIntraPingMeasurementResponseDTO responseDTO = new QoSIntraPingMeasurementResponseDTO();
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(uri);
		when(httpService.sendRequest(eq(uri), eq(HttpMethod.GET), eq(QoSIntraPingMeasurementResponseDTO.class))).thenReturn(new ResponseEntity<QoSIntraPingMeasurementResponseDTO>(responseDTO, HttpStatus.OK));
		
		final QoSIntraPingMeasurementResponseDTO result = orchestratorDriver.getIntraPingMedianMeasurement(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
		
		Assert.assertNotNull(result);
		Assert.assertFalse(result.hasRecord());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetInterDirectPingMeasurementUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.getInterDirectPingMeasurement(new CloudSystemFormDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetInterDirectPingMeasurementUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.getInterDirectPingMeasurement(new CloudSystemFormDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterDirectPingMeasurementOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8451, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT);
		Assert.assertTrue(uri.toString().contains("/measurements/intercloud/ping"));
		
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO();
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = new QoSInterDirectPingMeasurementResponseDTO();
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(uri);
		when(httpService.sendRequest(eq(uri), eq(HttpMethod.POST), eq(QoSInterDirectPingMeasurementResponseDTO.class), eq(requestDTO))).thenReturn(new ResponseEntity<QoSInterDirectPingMeasurementResponseDTO>(responseDTO, HttpStatus.OK));
		
		final QoSInterDirectPingMeasurementResponseDTO result = orchestratorDriver.getInterDirectPingMeasurement(requestDTO);
		
		Assert.assertNotNull(result);
		Assert.assertFalse(result.hasRecord());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetInterRelayEchoMeasurementUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.getInterRelayEchoMeasurement(new CloudRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetInterRelayEchoMeasurementUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.getInterRelayEchoMeasurement(new CloudRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterRelayEchoMeasurementOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8451, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT);
		Assert.assertTrue(uri.toString().contains("/measurements/intercloud/relay_echo"));
		
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		
		final QoSInterRelayEchoMeasurementListResponseDTO  responseDTO = new QoSInterRelayEchoMeasurementListResponseDTO(List.of(new QoSInterRelayEchoMeasurementResponseDTO()), 1);
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(uri);
		when(httpService.sendRequest(eq(uri), eq(HttpMethod.POST), eq(QoSInterRelayEchoMeasurementListResponseDTO.class), eq(requestDTO))).thenReturn(new ResponseEntity<QoSInterRelayEchoMeasurementListResponseDTO>(responseDTO, HttpStatus.OK));
		
		final QoSInterRelayEchoMeasurementListResponseDTO result = orchestratorDriver.getInterRelayEchoMeasurement(requestDTO);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getCount() == 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testMultiQueryServiceRegistryFormListNull() {
		try {
			orchestratorDriver.multiQueryServiceRegistry(null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Form list is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testMultiQueryServiceRegistryFormListEmpty() {
		try {
			orchestratorDriver.multiQueryServiceRegistry(List.of());
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Form list is empty.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void testMultiQueryServiceRegistryURINotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		try {
			orchestratorDriver.multiQueryServiceRegistry(List.of(new ServiceQueryFormDTO()));
		} catch (final ArrowheadException ex) {
			Assert.assertEquals("Orchestrator can't find Service Registry Multi Query URI.", ex.getMessage());
			verify(arrowheadContext).containsKey(anyString());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void testMultiQueryServiceRegistryURIWrongType() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn("invalid");
		try {
			orchestratorDriver.multiQueryServiceRegistry(List.of(new ServiceQueryFormDTO()));
		} catch (final ArrowheadException ex) {
			verify(arrowheadContext).containsKey(anyString());
			verify(arrowheadContext).get(anyString());
			Assert.assertEquals("Orchestrator can't find Service Registry Multi Query URI.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMultiQueryServiceRegistryOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn(Utilities.createURI("http", "localhost", 1234, "/test"));
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class))).thenReturn(new ResponseEntity<ServiceQueryResultListDTO>(new ServiceQueryResultListDTO(), HttpStatus.OK));

		orchestratorDriver.multiQueryServiceRegistry(List.of(new ServiceQueryFormDTO()));
		verify(arrowheadContext).containsKey(anyString());
		verify(arrowheadContext).get(anyString());
		verify(httpService).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(ServiceQueryResultListDTO.class), any(ServiceQueryFormListDTO.class));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResultDTO getOrchestrationResultDTO(final int index) {
		final SystemResponseDTO provider = new SystemResponseDTO(index, "system" + index, "127.0.0." + index, 1200 + index, null, null, null, null); 
		final ServiceDefinitionResponseDTO service = new ServiceDefinitionResponseDTO(index, "service" + index, null, null);
		final List<ServiceInterfaceResponseDTO> intfs = new ArrayList<>(1);
		intfs.add(new ServiceInterfaceResponseDTO(1, "HTTP-SECURE-JSON", null, null));
		
		return new OrchestrationResultDTO(provider, service, "/uri" + index, ServiceSecurityType.TOKEN, new HashMap<>(), intfs, null);
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> getSRResults() {
		final ServiceDefinitionResponseDTO service = new ServiceDefinitionResponseDTO(1, "service", null, null);
		final List<ServiceInterfaceResponseDTO> intfs = new ArrayList<>(1);
		ServiceInterfaceResponseDTO interface1 = new ServiceInterfaceResponseDTO(1, "HTTP-SECURE-JSON", null, null);
		intfs.add(interface1);
		final List<ServiceInterfaceResponseDTO> intfs2 = new ArrayList<>(1);
		intfs2.add(interface1);
		intfs2.add(new ServiceInterfaceResponseDTO(2, "HTTP-SECURE-XML", null, null));
		final SystemResponseDTO provider1 = new SystemResponseDTO(1, "system1", "127.0.0.1", 1201, null, null, null, null); 
		final SystemResponseDTO provider2 = new SystemResponseDTO(2, "system2", "127.0.0.2", 1202, null, null, null, null);
		
		final ServiceRegistryResponseDTO dto1 = new ServiceRegistryResponseDTO();
		dto1.setId(1);
		dto1.setInterfaces(intfs);
		dto1.setProvider(provider1);
		dto1.setSecure(ServiceSecurityType.CERTIFICATE);
		dto1.setServiceDefinition(service);
		
		final ServiceRegistryResponseDTO dto2 = new ServiceRegistryResponseDTO();
		dto2.setId(2);
		dto2.setInterfaces(intfs2);
		dto2.setProvider(provider2);
		dto2.setSecure(ServiceSecurityType.CERTIFICATE);
		dto2.setServiceDefinition(service);

		final List<ServiceRegistryResponseDTO> result = new ArrayList<>(2);
		result.add(dto1);
		result.add(dto2);
		
		return result;
	}
}
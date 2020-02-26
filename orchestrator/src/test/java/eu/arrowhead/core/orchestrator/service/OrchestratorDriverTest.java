package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.TokenDataDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
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
		orchestratorDriver.queryServiceRegistry(null, false, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryQueryUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.queryServiceRegistry(new ServiceQueryFormDTO(), false, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceReqistryQueryUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.queryServiceRegistry(new ServiceQueryFormDTO(), false, false);
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
		final UriComponents queryBySystemIdUriBeforeExpand = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8443, CommonConstants.SERVICE_REGISTRY_URI +
																	 CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI);
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
		final UriComponents queryBySystemDTOUri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8443, CommonConstants.SERVICE_REGISTRY_URI +
																	  CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI);
		
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
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementUriNotFound() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(false);
		
		orchestratorDriver.getPingMeasurement(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementUriWrongType() {
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn("invalid");
		
		orchestratorDriver.getPingMeasurement(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementOk() {
		final int systemId = 23;
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 8451, CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + 
				 									  CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT_SUFFIX).expand(systemId);
		Assert.assertTrue(uri.toString().contains("/ping/measurement/23"));
		
		final PingMeasurementResponseDTO responseDTO = new PingMeasurementResponseDTO();
		when(arrowheadContext.containsKey(any(String.class))).thenReturn(true);
		when(arrowheadContext.get(any(String.class))).thenReturn(uri);
		when(httpService.sendRequest(eq(uri), eq(HttpMethod.GET), eq(PingMeasurementResponseDTO.class))).thenReturn(new ResponseEntity<PingMeasurementResponseDTO>(responseDTO, HttpStatus.OK));
		
		final PingMeasurementResponseDTO result = orchestratorDriver.getPingMeasurement(systemId);
		
		Assert.assertNotNull(result);
		Assert.assertFalse(result.hasRecord());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResultDTO getOrchestrationResultDTO(final int index) {
		final SystemResponseDTO provider = new SystemResponseDTO(index, "system" + index, "127.0.0." + index, 1200 + index, null, null, null); 
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
		final SystemResponseDTO provider1 = new SystemResponseDTO(1, "system1", "127.0.0.1", 1201, null, null, null); 
		final SystemResponseDTO provider2 = new SystemResponseDTO(2, "system2", "127.0.0.2", 1202, null, null, null);
		
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
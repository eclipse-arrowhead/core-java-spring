package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.dto.TokenDataDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationResponseDTO;
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
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResultDTO getOrchestrationResultDTO(final int index) {
		final SystemResponseDTO provider = new SystemResponseDTO(index, "system" + index, "127.0.0." + index, 1200 + index, null, null, null); 
		final ServiceDefinitionResponseDTO service = new ServiceDefinitionResponseDTO(index, "service" + index, null, null);
		final List<ServiceInterfaceResponseDTO> intfs = new ArrayList<>(1);
		intfs.add(new ServiceInterfaceResponseDTO(1, "HTTP-SECURE-JSON", null, null));
		
		return new OrchestrationResultDTO(provider, service, "/uri" + index, ServiceSecurityType.TOKEN, null, intfs, null);
	}

}
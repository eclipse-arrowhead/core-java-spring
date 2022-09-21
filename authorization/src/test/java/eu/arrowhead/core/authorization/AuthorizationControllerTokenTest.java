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

package eu.arrowhead.core.authorization;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.internal.TokenGenerationMultiServiceResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.authorization.token.TokenGenerationService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@ContextConfiguration(classes = { AuthorizationServiceTestContext.class })
public class AuthorizationControllerTokenTest {

	//=================================================================================================
	// members
	
	private static final String AUTH_TOKEN_GENERATION_URI = CommonConstants.AUTHORIZATION_URI + "/token";
	private static final String AUTH_MULTI_TOKEN_GENERATION_URI = CommonConstants.AUTHORIZATION_URI + "/token/multi";
	private static final String AUTH_PUBLIC_KEY_URI = CommonConstants.AUTHORIZATION_URI + "/publickey";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockTokenGenerationService") 
	private TokenGenerationService tokenGenerationService;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerNull() throws Exception {
		final MvcResult result = postGenerateTokens(new TokenGenerationRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Consumer system is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerNameNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setSystemName(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerNameEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setSystemName(" ");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerAddressNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setAddress(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerAddressEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setAddress("\n");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerPortNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setPort(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System port is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerPortTooLow() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setPort(-1);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerPortTooHigh() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumer().setPort(123456);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerCloudOperatorNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumerCloud().setOperator(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Consumer cloud's operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerCloudOperatorEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumerCloud().setOperator(" ");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Consumer cloud's operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerCloudNameNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumerCloud().setName(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Consumer cloud's name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensConsumerCloudNameEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getConsumerCloud().setName(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Consumer cloud's name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProvidersListNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.setProviders(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Provider list is null or empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProvidersListEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.setProviders(List.of());
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Provider list is null or empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderNameNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setSystemName(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderNameEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setSystemName(" ");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderAddressNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setAddress(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderAddressEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setAddress("\n");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderPortNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setPort(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System port is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderPortTooLow() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setPort(-1);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderPortTooHigh() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setPort(123456);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderAuthInfoNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setAuthenticationInfo(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System authentication info is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensProviderAuthInfoEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).getProvider().setAuthenticationInfo("\t  ");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("System authentication info is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensServiceInterfacesNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).setServiceInterfaces(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Service interface list is null or empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensServiceInterfacesEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).setServiceInterfaces(List.of());
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Service interface list is null or empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensServiceInterfaceInvalid() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.getProviders().get(0).setServiceInterfaces(List.of("INVALID"));
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Specified interface name is not valid: INVALID", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensServiceNull() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.setService(null);
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Service is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGenerateTokensServiceEmpty() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		request.setService(" \r");
		final MvcResult result = postGenerateTokens(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(AUTH_TOKEN_GENERATION_URI, error.getOrigin());
		Assert.assertEquals("Service is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testGenerateTokensOk() throws Exception {
		final TokenGenerationRequestDTO request = getRequest();
		when(tokenGenerationService.generateTokensResponse(any(TokenGenerationRequestDTO.class))).thenReturn(new TokenGenerationResponseDTO());
		postGenerateTokens(request, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGenerateMultiServiceTokens() throws Exception {
		final TokenGenerationRequestDTO request1 = getRequest();
		request1.getConsumer().setSystemName("consumer1");
		final TokenGenerationRequestDTO request2 = getRequest();
		request2.getConsumer().setSystemName("consumer2");
		final List<TokenGenerationRequestDTO> requestList = List.of(request1, request2);
		ArgumentCaptor<List<TokenGenerationRequestDTO>> captor = ArgumentCaptor.forClass(List.class);
		when(tokenGenerationService.generateMultiServiceTokensResponse(captor.capture())).thenReturn(new TokenGenerationMultiServiceResponseDTO());
		postGenerateMultiTokens(requestList, status().isOk());
		
		final List<TokenGenerationRequestDTO> captured = captor.getValue();
		Assert.assertEquals(request1.getConsumer().getSystemName(), captured.get(0).getConsumer().getSystemName());
		Assert.assertEquals(request2.getConsumer().getSystemName(), captured.get(1).getConsumer().getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPublicKeyNotSecure() throws Exception {
		assumeFalse(secure);
		final MvcResult result = getPublicKey(status().isInternalServerError());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.ARROWHEAD, error.getExceptionType());
		Assert.assertEquals(AUTH_PUBLIC_KEY_URI, error.getOrigin());
		Assert.assertEquals("Authorization core service runs in insecure mode.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPublicKeyNotAvailable() throws Exception {
		assumeTrue(secure);

		final Object publicKey = arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		try {
			arrowheadContext.remove(CommonConstants.SERVER_PUBLIC_KEY);
			final MvcResult result = getPublicKey(status().isInternalServerError());
			final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
			
			Assert.assertEquals(ExceptionType.ARROWHEAD, error.getExceptionType());
			Assert.assertEquals(AUTH_PUBLIC_KEY_URI, error.getOrigin());
			Assert.assertEquals("Public key is not available.", error.getErrorMessage());
		} finally {
			arrowheadContext.put(CommonConstants.SERVER_PUBLIC_KEY, publicKey);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testGetPublicKeyOk() throws Exception {
		assumeTrue(secure);
		getPublicKey(status().isOk());
	}
		
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postGenerateTokens(final TokenGenerationRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(AUTH_TOKEN_GENERATION_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postGenerateMultiTokens(final List<TokenGenerationRequestDTO> request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(AUTH_MULTI_TOKEN_GENERATION_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult getPublicKey(final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(get((AUTH_PUBLIC_KEY_URI))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private TokenGenerationRequestDTO getRequest() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setAddress("localhost");
		consumer.setPort(8765);
		
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setOperator("aitia");
		consumerCloud.setName("testcloud2");
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(18765);
		provider.setAuthenticationInfo("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1aaeuv1I4bF5dxMIvUvLMxjRn309kdJewIIH08DfL17/LSssD70ZaLz0yxNfbPPQpFK8LMK+HQHDiGZH5yp4qJDuEgfmUrqWibnBIBc/K3Ob45lQy0zdFVtFsVJYBFVymQwgxJT6th0hI3RGLbCJMzbmpDzT7g0IDsN+64tMyi08ZCPrqk99uzYgioSSWNb9bhG2Z9646b3oiY5utQWRhP/2z/t6vVJHtRYeyaXPl6Z2M/5KnjpSvpSeZQhNrw+Is1DEE5DHiEjfQFWrLwDOqPKDrvmFyIlJ7P7OCMax6dIlSB7GEQSSP+j4eIxDWgjm+Pv/c02UVDc0x3xX/UGtNwIDAQAB");
		
		final TokenGenerationProviderDTO providerDTO = new TokenGenerationProviderDTO(provider, 6000, List.of("HTTP-SECURE-JSON"));
		
		return new TokenGenerationRequestDTO(consumer, consumerCloud, List.of(providerDTO), "aservice");
	}
}
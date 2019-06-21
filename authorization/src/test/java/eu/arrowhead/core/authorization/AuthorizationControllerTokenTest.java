package eu.arrowhead.core.authorization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.authorization.token.TokenGenerationService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@ContextConfiguration(classes = { AuthorizationServiceTestContext.class })
public class AuthorizationControllerTokenTest {

	//=================================================================================================
	// members
	
	private static final String AUTH_TOKEN_GENERATION_URI = CommonConstants.AUTHORIZATION_URI + "/token";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockTokenGenerationService") 
	private TokenGenerationService tokenGenerationService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
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
	
	//TODO: continue with consumer cloud checks
	
	
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
	private TokenGenerationRequestDTO getRequest() {
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setAddress("localhost");
		consumer.setPort(8765);
		
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setOperator("aitia");
		consumerCloud.setName("testcloud2");
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		
		return new TokenGenerationRequestDTO(consumer, consumerCloud, List.of(provider), "aservice", null);
	}
}
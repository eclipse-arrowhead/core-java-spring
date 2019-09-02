package eu.arrowhead.core.gateway;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayMain.class)
public class GatewayControllerTest {

	//=================================================================================================
	// members
	
	private static final String GATEWAY_PUBLIC_KEY_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_KEY_URI;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
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
	public void testGetPublicKeyNotSecure() throws Exception {
		assumeFalse(secure);
		final MvcResult result = getPublicKey(status().isInternalServerError());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.ARROWHEAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_PUBLIC_KEY_URI, error.getOrigin());
		Assert.assertEquals("Gateway core service runs in insecure mode.", error.getErrorMessage());
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
			Assert.assertEquals(GATEWAY_PUBLIC_KEY_URI, error.getOrigin());
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
	private MvcResult getPublicKey(final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(get((GATEWAY_PUBLIC_KEY_URI))
						   .accept(MediaType.TEXT_PLAIN))
						   .andExpect(matcher)
						   .andReturn();
	}
}

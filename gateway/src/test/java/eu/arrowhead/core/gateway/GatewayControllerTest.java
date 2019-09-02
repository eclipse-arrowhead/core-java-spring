package eu.arrowhead.core.gateway;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.service.ActiveSessionListDTO;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayMain.class)
public class GatewayControllerTest {

	//=================================================================================================
	// members
	
	private static final String GATEWAY_PUBLIC_KEY_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_KEY_URI;
	private static final String GATEWAY_ACTIVE_SESSIONS_URI = CommonConstants.GATEWAY_URI + CommonConstants.MGMT_URI + "/sessions";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	private ConcurrentHashMap<String,ActiveSessionDTO> activeSessions;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		
		fillActiveSessions();
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
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetActiveSessionsWithoutPageAndSizeParameter() throws Exception {
		final MvcResult result = getActiveSessions(status().isOk(), null, null);
		final ActiveSessionListDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ActiveSessionListDTO.class);
		
		Assert.assertEquals(activeSessions.size(), responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetActiveSessionsWithValidPageAndSizeParameter() throws Exception {
		final int page = 1;
		final int size = 5;
		final MvcResult result = getActiveSessions(status().isOk(), String.valueOf(page), String.valueOf(size));
		final ActiveSessionListDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ActiveSessionListDTO.class);
		
		Assert.assertEquals(activeSessions.size(), responseBody.getCount());
		Assert.assertEquals(size, responseBody.getData().size());
		Assert.assertEquals("2019-01-06 01:01:01", responseBody.getData().get(0).getSessionStartedAt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetActiveSessionsWithInvalidPageParameter() throws Exception {
		final MvcResult result = getActiveSessions(status().isBadRequest(), "-2", "3");
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_ACTIVE_SESSIONS_URI, error.getOrigin());
		Assert.assertEquals("Page parameter has to be equals or greater than zero and size parameter has to be equals or greater than one.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetActiveSessionsWithInvalidSizeParameter() throws Exception {
		final MvcResult result = getActiveSessions(status().isBadRequest(), "0", "-1");
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_ACTIVE_SESSIONS_URI, error.getOrigin());
		Assert.assertEquals("Page parameter has to be equals or greater than zero and size parameter has to be equals or greater than one.", error.getErrorMessage());
	}
	
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private void fillActiveSessions() {
		for (int i = 1; i <= 31; ++i) {
			final ActiveSessionDTO activeSessionDTO = new ActiveSessionDTO();
			activeSessionDTO.setSessionStartedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.of(2019, 1, i, 1, 1, 1, 1, ZoneOffset.UTC)));
			activeSessions.put("test-key-" + i, activeSessionDTO);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private MvcResult getPublicKey(final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(get((GATEWAY_PUBLIC_KEY_URI))
						   .accept(MediaType.TEXT_PLAIN))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult getActiveSessions(final ResultMatcher matcher, final String page, final String size) throws Exception {
		return this.mockMvc.perform(get((GATEWAY_ACTIVE_SESSIONS_URI))
						   .param("page", page)
						   .param("item_per_page", size)
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
}

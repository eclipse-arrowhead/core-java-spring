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

package eu.arrowhead.core.gateway;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.shared.ActiveSessionCloseErrorDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.service.ActiveSessionListDTO;
import eu.arrowhead.core.gateway.service.GatewayService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayMain.class)
public class GatewayControllerTest {

	//=================================================================================================
	// members
	
	private static final String GATEWAY_PUBLIC_KEY_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_KEY_URI;
	private static final String GATEWAY_ACTIVE_SESSIONS_URI = CommonConstants.GATEWAY_URI + CoreCommonConstants.MGMT_URI + "/sessions";
	private static final String GATEWAY_CLOSE_SESSIONS_MGMT_URI = GATEWAY_ACTIVE_SESSIONS_URI + "/close";
	private static final String GATEWAY_CONNECT_PROVIDER_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CONNECT_PROVIDER_URI;
	private static final String GATEWAY_CONNECT_CONSUMER_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CONNECT_CONSUMER_URI;
	private static final String GATEWAY_CLOSE_SESSIONS_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CLOSE_SESSIONS;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	private ConcurrentHashMap<String,ActiveSessionDTO> activeSessions;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@MockBean(name = "mockGatewayService")
	private GatewayService gatewayService;
	
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
	public void testGetActiveSessionsWithNoActiveSessions() throws Exception {
		activeSessions.clear();
		final MvcResult result = getActiveSessions(status().isOk(), "1", "8");
		final ActiveSessionListDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ActiveSessionListDTO.class);
		
		Assert.assertEquals(0, responseBody.getData().size());
		Assert.assertEquals(0, responseBody.getCount());
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
		Assert.assertEquals("2019-01-06T01:01:01Z", responseBody.getData().get(0).getSessionStartedAt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetActiveSessionsWithInvalidPageParameter() throws Exception {
		final MvcResult result = getActiveSessions(status().isBadRequest(), null, "3");
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_ACTIVE_SESSIONS_URI, error.getOrigin());
		Assert.assertEquals("Defined page or size could not be with undefined size or page.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetActiveSessionsWithInvalidSizeParameter() throws Exception {
		final MvcResult result = getActiveSessions(status().isBadRequest(), "0", null);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_ACTIVE_SESSIONS_URI, error.getOrigin());
		Assert.assertEquals("Defined page or size could not be with undefined size or page.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullQueueId() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId(null);
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("queueId is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankQueueId() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("   ");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("queueId is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullPeerName() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName(null);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("peerName is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankPeerName() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("   ");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("peerName is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullServiceDefinition() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition(null);
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("serviceDefinition id is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankServiceDefinition() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("  ");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("serviceDefinition id is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullRequestQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue(null);
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("requestQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankRequestQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("   ");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("requestQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullRequestControlQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue(null);
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("requestControlQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankRequestControlQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("   ");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("requestControlQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullResponseQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue(null);
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("responseQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankResponseQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("   ");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("responseQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullResponseControlQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue(null);
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("responseControlQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankResponseControlQueue() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("   ");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("responseControlQueue is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullSessionStartedAt() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt(null);
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("sessionStartedAt is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankSessionStartedAt() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("  ");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("sessionStartedAt is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerSystem() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(null);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerSystemName() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName(null);
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankConsumerSystemName() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("   ");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerSystemAddress() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress(null);
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankConsumerSystemAddress() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("  ");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01Z01:01:01T");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerSystemPort() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(null);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System port is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithConsumerSystemPortOutOfRangeMin() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithConsumerSystemPortOutOfRangeMax() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	// we skip the provider check tests because it uses the same method than consumer check
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerCloud() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(null);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Cloud is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerCloudOperator() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator(null);
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());
	}
		
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankConsumerCloudOperator() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("  ");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullConsumerCloudName() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName(null);
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankConsumerCloudName() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("  ");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank", error.getErrorMessage());
	}	

	// we skip the provider cloud check tests because it uses the same method than consumer check
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullRelay() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(null);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullRelayAddress() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(null);
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankRelayAddress() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("  ");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullRelayPort() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(null);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay port is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithRelayPortOutOfRangeMin() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithRelayPortOutOfRangeMax() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithNullRelayType() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType(null);
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithBlankRelayType() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("   ");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionWithInvalidRelayType() throws Exception {
		final ActiveSessionDTO request = new ActiveSessionDTO();
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		request.setServiceDefinition("test-service");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEKEEPER_RELAY");
		request.setRelay(relay);
		request.setRequestQueue("test-request-queue");
		request.setRequestControlQueue("test-request-control-queue");
		request.setResponseQueue("test-response-queue");
		request.setResponseControlQueue("test-response-control-queue");
		request.setSessionStartedAt("2019-01-01T01:01:01Z");
		
		final MvcResult result = postMgmtCloseSession(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_MGMT_URI, error.getOrigin());
		Assert.assertEquals("Relay type is invalid", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullServiceDefinition() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition(null);
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankServiceDefinition() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("   ");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullGWPublicKey() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey(null);
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Consumer gateway public key is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankGWPublicKey() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("  ");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Consumer gateway public key is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullRelay() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		request.setRelay(null);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullRelayAddress() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(null);
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankRelayAddress() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("   ");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullRelayPort() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(null);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay port is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderRelayPortOutOfRangeMin() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderRelayPortOutOfRangeMax() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullRelayType() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType(null);
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankRelayType() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("   ");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderInvalidRelayType() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEKEEPER_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Relay type is invalid", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullSystem() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setConsumer(null);
		request.setProvider(null);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullSystemName() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName(null);
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankSystemName() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("   ");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullSystemAddress() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress(null);
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankSystemAddress() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("    ");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullSystemPort() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(null);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System port is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderSystemPortOutOfRangeMin() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderSystemPortOutOfRangeMax() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullCloud() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		request.setConsumerCloud(null);
		request.setProviderCloud(null);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Cloud is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullCloudOperator() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator(null);
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankCloudOperator() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("    ");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderNullCloudName() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName(null);
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderBlankCloudName() throws Exception {
		final GatewayProviderConnectionRequestDTO request = new GatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setConsumerGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("   ");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectProvider(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_PROVIDER_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullServiceDefinition() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition(null);
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankServiceDefinition() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("   ");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullGWPublicKey() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey(null);
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Provider gateway public key is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankGWPublicKey() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("   ");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Provider gateway public key is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullQueueId() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId(null);
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Queue id is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankQueueId() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId(null);
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Queue id is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullPeerName() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName(null);
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Peer name is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankPeerName() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("    ");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Peer name is null or blank.", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullRelay() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		request.setRelay(null);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullRelayAddress() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(null);
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankRelayAddress() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("       ");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullRelayPort() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(null);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay port is null", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerRelayPortOutOfRangeMin() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerRelayPortOutOfRangeMax() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullRelayType() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType(null);
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankRelayType() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("    ");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerInvalidRelayType() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEKEEPER_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Relay type is invalid", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullSystem() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		request.setConsumer(null);
		request.setProvider(null);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullSystemName() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName(null);
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankSystemName() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("   ");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullSystemAdress() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress(null);
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankSystemAdress() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("      ");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullSystemPort() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(null);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System port is null", error.getErrorMessage());
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerSystemPortOutOfRangeMin() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(CommonConstants.SYSTEM_PORT_RANGE_MIN - 1);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerSystemPortOutOfRangeMax() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(CommonConstants.SYSTEM_PORT_RANGE_MAX + 1);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullCloud() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		request.setConsumerCloud(null);
		request.setProviderCloud(null);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Cloud is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullCloudOperator() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator(null);
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankCloudOperator() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("   ");
		cloud.setName("test-name");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerNullCloudName() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName(null);
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerBlankCloudName() throws Exception {
		final GatewayConsumerConnectionRequestDTO request = new GatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("test-service");
		request.setQueueId("test-queue-id");
		request.setPeerName("test.peer.name");
		request.setProviderGWPublicKey("test-key");
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("test-address");
		relay.setPort(2000);
		relay.setType("GATEWAY_RELAY");
		request.setRelay(relay);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-system");
		system.setAddress("test-system-address");
		system.setPort(1000);
		request.setConsumer(system);
		request.setProvider(system);
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-operator");
		cloud.setName("   ");
		request.setConsumerCloud(cloud);
		request.setProviderCloud(cloud);
		
		final MvcResult result = postConnectConsumer(status().isBadRequest(), request);
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CONNECT_CONSUMER_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsEmptyPortList() throws Exception {
		final MvcResult result = postCloseSession(status().isBadRequest(), List.of());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(GATEWAY_CLOSE_SESSIONS_URI, error.getOrigin());
		Assert.assertEquals("Ports list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsPortTooLow() throws Exception {
		final MvcResult result = postCloseSession(status().isOk(), List.of(3000));
		final TypeReference<List<ActiveSessionCloseErrorDTO>> typeRef = new TypeReference<>() {};
		final List<ActiveSessionCloseErrorDTO> error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), typeRef);
		
		Assert.assertEquals(1, error.size());
		Assert.assertEquals(3000, error.get(0).getPort());
		Assert.assertEquals("Invalid active session port.", error.get(0).getError());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsPortTooHigh() throws Exception {
		final MvcResult result = postCloseSession(status().isOk(), List.of(8500));
		final TypeReference<List<ActiveSessionCloseErrorDTO>> typeRef = new TypeReference<>() {};
		final List<ActiveSessionCloseErrorDTO> error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), typeRef);
		
		Assert.assertEquals(1, error.size());
		Assert.assertEquals(8500, error.get(0).getPort());
		Assert.assertEquals("Invalid active session port.", error.get(0).getError());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsPortNotFound() throws Exception {
		when(gatewayService.closeSession(8050)).thenReturn("not found");
		
		final MvcResult result = postCloseSession(status().isOk(), List.of(8050));
		final TypeReference<List<ActiveSessionCloseErrorDTO>> typeRef = new TypeReference<>() {};
		final List<ActiveSessionCloseErrorDTO> error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), typeRef);
		
		Assert.assertEquals(1, error.size());
		Assert.assertEquals(8050, error.get(0).getPort());
		Assert.assertEquals("not found", error.get(0).getError());
		
		verify(gatewayService, times(1)).closeSession(8050);
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsOk() throws Exception {
		when(gatewayService.closeSession(8050)).thenReturn(null);
		
		final MvcResult result = postCloseSession(status().isOk(), List.of(8050));
		final TypeReference<List<ActiveSessionCloseErrorDTO>> typeRef = new TypeReference<>() {};
		final List<ActiveSessionCloseErrorDTO> error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), typeRef);
		
		Assert.assertEquals(0, error.size());
		
		verify(gatewayService, times(1)).closeSession(8050);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private void fillActiveSessions() {
		for (int i = 1; i <= 31; ++i) {
			final ActiveSessionDTO activeSessionDTO = new ActiveSessionDTO();
			activeSessionDTO.setConsumerServerSocketPort(8000 + i);
			activeSessionDTO.setSessionStartedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.of(2019, 1, i, 1, 1, 1, 0, ZoneOffset.UTC)));
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
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postMgmtCloseSession(final ResultMatcher matcher, final ActiveSessionDTO request) throws Exception {
		return this.mockMvc.perform(post((GATEWAY_CLOSE_SESSIONS_MGMT_URI))
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postConnectProvider(final ResultMatcher matcher, final GatewayProviderConnectionRequestDTO request) throws Exception {
		return this.mockMvc.perform(post((GATEWAY_CONNECT_PROVIDER_URI))
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postConnectConsumer(final ResultMatcher matcher, final GatewayConsumerConnectionRequestDTO request) throws Exception {
		return this.mockMvc.perform(post((GATEWAY_CONNECT_CONSUMER_URI))
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postCloseSession(final ResultMatcher matcher, final List<Integer> ports) throws Exception {
		return this.mockMvc.perform(post((GATEWAY_CLOSE_SESSIONS_URI))
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(ports))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
}
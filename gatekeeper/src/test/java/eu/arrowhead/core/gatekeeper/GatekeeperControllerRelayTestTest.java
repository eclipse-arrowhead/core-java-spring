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

package eu.arrowhead.core.gatekeeper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.http.HttpStatus;
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
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.gatekeeper.service.GatekeeperService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration(classes = { GatekeeperServiceTestContext.class })
public class GatekeeperControllerRelayTestTest {

	//=================================================================================================
	// members
	
	private static final String INIT_RELAY_TEST_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_RELAY_TEST_SERVICE;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockGatekeeperService") 
	private GatekeeperService gatekeeperService;
	
	@MockBean(name = "mockCnVerifier")
	private CommonNamePartVerifier cnVerifier;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		when(cnVerifier.isValid(anyString())).thenReturn(true);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudNull() throws Exception {
		final MvcResult result = postInitICN(new QoSRelayTestProposalRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudNullOperator() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("operator is empty"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudEmptyOperator() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("operator is empty"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudNullName() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("name is empty"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudEmptyName() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName(" ");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("name is empty"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("RelayRequestDTO is empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayAddressNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("address is empty"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayAddressEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(" ");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("address is empty"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayPortNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("port is null or should be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayPortTooLow() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(-1);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);

		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("port is null or should be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayPortTooHigh() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(123456);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("port is null or should be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayTypeInvalid() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("invalid");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains("type 'invalid' is not valid"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayGeneralExclusive() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GENERAL_RELAY");
		relay.setExclusive(true);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertTrue(error.getErrorMessage().contains(" GENERAL_REALY type couldn't be exclusive"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayGatekeeper() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEKEEPER_RELAY");
		relay.setExclusive(false);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("Invalid relay type for testing: " + RelayType.GATEKEEPER_RELAY, error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestOk() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		relay.setExclusive(false);
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		doNothing().when(gatekeeperService).initRelayTest(request);
		
		postInitICN(request, status().isOk());
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postInitICN(final QoSRelayTestProposalRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(INIT_RELAY_TEST_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request)))
						   .andExpect(matcher)
						   .andReturn();
	}
}
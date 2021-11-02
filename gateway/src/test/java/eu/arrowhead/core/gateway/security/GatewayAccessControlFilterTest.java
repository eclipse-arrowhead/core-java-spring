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

package eu.arrowhead.core.gateway.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

/**
* IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
*
*/
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class GatewayAccessControlFilterTest {

	//=================================================================================================
	// members
	
	private static final String GATEWAY_ECHO_URI = CommonConstants.GATEWAY_URI + CommonConstants.ECHO_URI;
	private static final String GATEWAY_ACTIVE_SESSIONS_MGMT_URI = CommonConstants.GATEWAY_URI + CoreCommonConstants.MGMT_URI + "/sessions";
	private static final String GATEWAY_CONNECT_PROVIDER_URI = CommonConstants.GATEWAY_URI + "/connect_provider";
	private static final String GATEWAY_CONNECT_CONSUMER_URI = CommonConstants.GATEWAY_URI + "/connect_consumer";
	private static final String GATEWAY_CLOSE_SESSIONS_URI = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CLOSE_SESSIONS;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private MockMvc mockMvc;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		assumeTrue(secure);
		
		final GatewayAccessControlFilter gwFilter = appContext.getBean(GatewayAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(gwFilter)
									  .build();			
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateSameCloud() throws Exception {
		this.mockMvc.perform(get(GATEWAY_ECHO_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(GATEWAY_ECHO_URI)
				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtCertificateSysop() throws Exception {
		this.mockMvc.perform(get(GATEWAY_ACTIVE_SESSIONS_MGMT_URI)
				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(GATEWAY_ACTIVE_SESSIONS_MGMT_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderCertificateGatekeeper() throws Exception {
		this.mockMvc.perform(post(GATEWAY_CONNECT_PROVIDER_URI)
				    .secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(createGatewayProviderConnectionRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectProviderCertificateNotGatekeeper() throws Exception {
		this.mockMvc.perform(post(GATEWAY_CONNECT_PROVIDER_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(createGatewayProviderConnectionRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerCertificateGatekeeper() throws Exception {
		this.mockMvc.perform(post(GATEWAY_CONNECT_CONSUMER_URI)
				    .secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(createGatewayConsumerConnectionRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConnectConsumerCertificateNotGatekeeper() throws Exception {
		this.mockMvc.perform(post(GATEWAY_CONNECT_CONSUMER_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(createGatewayConsumerConnectionRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsChoreographer() throws Exception {
		this.mockMvc.perform(post(GATEWAY_CLOSE_SESSIONS_URI)
					.secure(true)
					.with(x509("certificates/choreographer.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(List.of()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseSessionsNotChoreographer() throws Exception {
		this.mockMvc.perform(post(GATEWAY_CLOSE_SESSIONS_URI)
					.secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(List.of()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GatewayProviderConnectionRequestDTO createGatewayProviderConnectionRequestDTO() {
		return new GatewayProviderConnectionRequestDTO(new RelayRequestDTO(), new SystemRequestDTO(), new SystemRequestDTO(), new CloudRequestDTO(), new CloudRequestDTO(), "test-service", "");
	}
	
	//-------------------------------------------------------------------------------------------------
	private GatewayConsumerConnectionRequestDTO createGatewayConsumerConnectionRequestDTO() {
		return new GatewayConsumerConnectionRequestDTO(new RelayRequestDTO(), "queueId", "peerName", "key", new SystemRequestDTO(), new SystemRequestDTO(), new CloudRequestDTO(),
													   new CloudRequestDTO(), "test-service");
	}
}
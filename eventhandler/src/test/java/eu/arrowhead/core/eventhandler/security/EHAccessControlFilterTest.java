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

package eu.arrowhead.core.eventhandler.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;


/**
* IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
*
*/
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class EHAccessControlFilterTest {

	//=================================================================================================
	// members
	
	private static final String EVENT_HANDLER_ECHO_URI = CommonConstants.EVENTHANDLER_URI + CommonConstants.ECHO_URI;
	private static final String EVENT_HANDLER_MGMT_URI = CommonConstants.EVENTHANDLER_URI + CoreCommonConstants.MGMT_URI + "/subscriptions";
	private static final String EVENT_HANDLER_PUBLISH_AUTH_UPDATE_URI = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH_AUTH_UPDATE;
	private static final String EVENT_HANDLER_SUBSCRIBE_URI = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE;
	private static final String EVENT_HANDLER_PUBLISH_URI = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH;
	
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
		
		final EHAccessControlFilter gkFilter = appContext.getBean(EHAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(gkFilter)
									  .build();			
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateSameCloud() throws Exception {
		this.mockMvc.perform(get(EVENT_HANDLER_ECHO_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(EVENT_HANDLER_ECHO_URI)
				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtSubscriptionsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(EVENT_HANDLER_MGMT_URI)
				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtSubscriptionsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(EVENT_HANDLER_MGMT_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishAuthUpdateCertificateAuthorization() throws Exception {
		this.mockMvc.perform(post(EVENT_HANDLER_PUBLISH_AUTH_UPDATE_URI)
				    .secure(true)
					.with(x509("certificates/authorization.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getEventPublishRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishAuthUpdateCertificateNotAuthorization() throws Exception {
		this.mockMvc.perform(post(EVENT_HANDLER_PUBLISH_AUTH_UPDATE_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes( getEventPublishRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishCertificatePublisher() throws Exception {
		this.mockMvc.perform(post(EVENT_HANDLER_PUBLISH_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes( getEventPublishRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishCertificateNotPublisher() throws Exception {
		this.mockMvc.perform(post(EVENT_HANDLER_PUBLISH_URI)
				    .secure(true)
					.with(x509("certificates/authorization.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes( getEventPublishRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSubscribeCertificateSubscriber() throws Exception {
		this.mockMvc.perform(post(EVENT_HANDLER_SUBSCRIBE_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes( getSubscriptionRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSubscribeCertificateNotSubscriber() throws Exception {
		this.mockMvc.perform(post(EVENT_HANDLER_SUBSCRIBE_URI)
				    .secure(true)
					.with(x509("certificates/authorization.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes( getSubscriptionRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private SystemRequestDTO getSystemRequestDTO() {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setSystemName("client-demo-provider");
		systemRequestDTO.setAddress("localhost");
		systemRequestDTO.setPort(12345);	
		
		return systemRequestDTO;
	}

	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		return new EventPublishRequestDTO("eventType", getSystemRequestDTO(), null, "payload", Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
	
	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOForTest() {
		return new SubscriptionRequestDTO(
				"eventType", 
				getSystemRequestDTO(), 
				null, // filterMetaData
				"notifyUri", 
				false, // matchMetaData
				null, // startDate
				null, // endDate 
				null); // sources
	}
}
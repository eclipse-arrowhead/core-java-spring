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

package eu.arrowhead.core.orchestrator.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleRequestDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class OrchestratorAccessControlFilterTest {
	
	//=================================================================================================
	// members
	
	private static final String ORCH_ECHO = CommonConstants.ORCHESTRATOR_URI + CommonConstants.ECHO_URI;
	private static final String ORCH_ORCHESTRATION = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI;
	private static final String ORCH_QOS_ENABLED = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_ENABLED_URI;
	private static final String ORCH_QOS_TEMPORARY_LOCK = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI;
	private static final String ORCH_QOS_RESERVATION = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI;
	private static final String ORCH_FLEX_STORE_CREATE = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI;
	private static final String ORCH_FLEX_STORE_REMOVE = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_REMOVE_FLEXIBLE_STORE_RULE_URI.replace("{id}", "5");
	private static final String ORCH_FLEX_STORE_CLEAN = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CLEAN_FLEXIBLE_STORE_URI;
	private static final String ORCH_ORCHESTRATION_BY_PROXY = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_BY_PROXY_URI;
	
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
		
		final OrchestratorAccessControlFilter orchFilter = appContext.getBean(OrchestratorAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(orchFilter)
									  .build();
			
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateSameCloud() throws Exception {
		this.mockMvc.perform(get(ORCH_ECHO)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(ORCH_ECHO)
				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalOrchestrationWithGatekeeper() throws Exception {
		final Map<String,Boolean> flags = new HashMap<>();
		flags.put(CommonConstants.ORCHESTRATION_FLAG_EXTERNAL_SERVICE_REQUEST, true);
		final OrchestrationFormRequestDTO requestDTO = createOrchestrationFromRequestDTO("", flags);
		
		this.mockMvc.perform(post(ORCH_ORCHESTRATION)
				    .secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(requestDTO))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalOrchestrationWithoutGatekeeper() throws Exception {
		final Map<String,Boolean> flags = new HashMap<>();
		flags.put(CommonConstants.ORCHESTRATION_FLAG_EXTERNAL_SERVICE_REQUEST, true);
		final OrchestrationFormRequestDTO requestDTO = createOrchestrationFromRequestDTO("", flags);
		
		this.mockMvc.perform(post(ORCH_ORCHESTRATION)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(requestDTO))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInternalOrchestrationWithCertificatedSystemName() throws Exception {
		final OrchestrationFormRequestDTO requestDTO = createOrchestrationFromRequestDTO("client-demo-provider", new HashMap<>());
		
		this.mockMvc.perform(post(ORCH_ORCHESTRATION)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(requestDTO))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInternalOrchestrationWithNotCertificatedSystemName() throws Exception {
		final OrchestrationFormRequestDTO requestDTO = createOrchestrationFromRequestDTO("not-certificated-provider", new HashMap<>());
		
		this.mockMvc.perform(post(ORCH_ORCHESTRATION)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(requestDTO))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQoSEnabledWithGatekeeper() throws Exception {
		this.mockMvc.perform(get(ORCH_QOS_ENABLED)
				    .secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQoSEnabledWithNotGatekeeper() throws Exception {
		this.mockMvc.perform(get(ORCH_QOS_ENABLED)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetQoSReservationWithGatekeeper() throws Exception {
		this.mockMvc.perform(get(ORCH_QOS_RESERVATION)
			    	.secure(true)
			    	.with(x509("certificates/gatekeeper.pem"))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetQoSReservationWithNotGatekeeper() throws Exception {
		this.mockMvc.perform(get(ORCH_QOS_RESERVATION)
			    	.secure(true)
			    	.with(x509("certificates/provider.pem"))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTemporaryLockWithGatekeeper() throws Exception {
		this.mockMvc.perform(post(ORCH_QOS_TEMPORARY_LOCK)
				    .secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSTemporaryLockRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTemporaryLockWithNotGatekeeper() throws Exception {
		this.mockMvc.perform(post(ORCH_QOS_TEMPORARY_LOCK)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSTemporaryLockRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConfirmReservationWithGatekeeper() throws Exception {
		this.mockMvc.perform(post(ORCH_QOS_RESERVATION)
				    .secure(true)
					.with(x509("certificates/gatekeeper.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSReservationRequestDTO(null, null, null)))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConfirmReservationWithNotGatekeeper() throws Exception {
		this.mockMvc.perform(post(ORCH_QOS_RESERVATION)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new QoSReservationRequestDTO(null, null, null)))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateFlexibleStoreRulesWithPlantDescriptionEngine() throws Exception {
		this.mockMvc.perform(post(ORCH_FLEX_STORE_CREATE)
				    .secure(true)
					.with(x509("certificates/plantdescriptionengine.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(List.of(new OrchestratorStoreFlexibleRequestDTO())))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateFlexibleStoreRulesWithNotPlantDescriptionEngine() throws Exception {
		this.mockMvc.perform(post(ORCH_FLEX_STORE_CREATE)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(List.of(new OrchestratorStoreFlexibleRequestDTO())))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveFlexibleStoreRuleWithPlantDescriptionEngine() throws Exception {
		this.mockMvc.perform(delete(ORCH_FLEX_STORE_REMOVE)
				    .secure(true)
					.with(x509("certificates/plantdescriptionengine.pem")))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveFlexibleStoreRuleWithNotPlantDescriptionEngine() throws Exception {
		this.mockMvc.perform(delete(ORCH_FLEX_STORE_REMOVE)
				    .secure(true)
					.with(x509("certificates/provider.pem")))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCleanFlexibleStoreWithPlantDescriptionEngine() throws Exception {
		this.mockMvc.perform(delete(ORCH_FLEX_STORE_CLEAN)
				    .secure(true)
					.with(x509("certificates/plantdescriptionengine.pem")))
					.andExpect(status().isBadRequest()); // Bad request result means that the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCleanFlexibleStoreWithNotPlantDescriptionEngine() throws Exception {
		this.mockMvc.perform(delete(ORCH_FLEX_STORE_CLEAN)
				    .secure(true)
					.with(x509("certificates/provider.pem")))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationByProxyWithChoreographer() throws Exception {
		this.mockMvc.perform(post(ORCH_ORCHESTRATION_BY_PROXY)
				    .secure(true)
				    .with(x509("certificates/choreographer.pem"))
				    .contentType(MediaType.APPLICATION_JSON)
				    .content(objectMapper.writeValueAsBytes(new OrchestrationFormRequestDTO()))
				    .accept(MediaType.APPLICATION_JSON))
				    .andExpect(status().isBadRequest()); // Bad request result means the the request gone through the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationByProxyWithNotChoreographer() throws Exception {
		this.mockMvc.perform(post(ORCH_ORCHESTRATION_BY_PROXY)
				    .secure(true)
				    .with(x509("certificates/provider.pem"))
				    .contentType(MediaType.APPLICATION_JSON)
				    .content(objectMapper.writeValueAsBytes(new OrchestrationFormRequestDTO()))
				    .accept(MediaType.APPLICATION_JSON))
				    .andExpect(status().isUnauthorized()); 
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationFormRequestDTO createOrchestrationFromRequestDTO(final String requesterSystemName, final Map<String,Boolean> flags) {
		final OrchestrationFormRequestDTO dto = new OrchestrationFormRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(requesterSystemName);		
		
		dto.setRequesterSystem(requesterSystem);
		dto.getOrchestrationFlags().putAll(flags);
		
		return dto;
	}
}
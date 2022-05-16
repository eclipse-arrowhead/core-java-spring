/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.choreographer.security;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class ChoreographerExecutorNotifyAccessControlFilterTest {
	
	//=================================================================================================
	// members
	
	private static final String CHOREOGRAPHER_NOTIFY = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockChoreographerSessionDBService")
	private ChoreographerSessionDBService sessionDBService;
	
	private MockMvc mockMvc;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		assumeTrue(secure);
		
		final ChoreographerAccessControlFilter chacFilter = appContext.getAutowireCapableBeanFactory().createBean(ChoreographerAccessControlFilter.class);
		final ChoreographerExecutorNotifyAccessControlFilter chexacFilter = appContext.getAutowireCapableBeanFactory().createBean(ChoreographerExecutorNotifyAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(chacFilter, chexacFilter)
									  .build();
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyWithNoSessionId() throws Exception {
		// Filter breaks the filter chain => the controller method will rejects the ill-formed request
		
		postNotify(new ChoreographerExecutedStepResultDTO(), "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyWithInvalidSessionId() throws Exception {
		// Filter breaks the filter chain => the controller method will rejects the ill-formed request
		
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionStepId(-1L);
		
		postNotify(payload, "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyWithNonExistentSessionStep() throws Exception {
		when(sessionDBService.getSessionStepById(1)).thenThrow(InvalidParameterException.class);
		
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionStepId(1L);
		
		postNotify(payload, "certificates/provider.pem", status().isUnauthorized());
		
		verify(sessionDBService, times(1)).getSessionStepById(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyWithNotAuthorizedExecutor() throws Exception {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setName("executor");
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setExecutor(executor);
		when(sessionDBService.getSessionStepById(1)).thenReturn(sessionStep);
		
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionStepId(1L);
		
		postNotify(payload, "certificates/provider.pem", status().isUnauthorized());

		verify(sessionDBService, times(1)).getSessionStepById(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyWithAuthorizedExecutorExactNameMatch() throws Exception {
		// Filter pass, but the controller method will rejects the ill-formed request
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setName("client-demo-provider");
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setExecutor(executor);
		when(sessionDBService.getSessionStepById(1)).thenReturn(sessionStep);
		
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionStepId(1L);
		
		postNotify(payload, "certificates/provider.pem", status().isBadRequest());
		
		verify(sessionDBService, atLeast(1)).getSessionStepById(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyWithAuthorizedExecutorCaseInsensitiveNameMatch() throws Exception {
		// Filter pass, but the controller method will rejects the ill-formed request
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setName("client-DEMO-provider");
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setExecutor(executor);
		when(sessionDBService.getSessionStepById(1)).thenReturn(sessionStep);
		
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionStepId(1L);
		
		postNotify(payload, "certificates/provider.pem", status().isBadRequest());
		
		verify(sessionDBService, atLeast(1)).getSessionStepById(1);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void postNotify(final ChoreographerExecutedStepResultDTO request, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(CHOREOGRAPHER_NOTIFY)
			    	.secure(true)
			    	.with(x509(certificatePath))
			    	.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(request))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
}
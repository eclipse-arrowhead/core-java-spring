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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.choreographer.service.ChoreographerExecutorService;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class ChoreographerAccessControlFilterTest {
	
	//=================================================================================================
	// members
	
	private static final String CHOREOGRAPHER_ECHO = CommonConstants.CHOREOGRAPHER_URI + "/echo";
	private static final String CHOREOGRAPHER_MGMT_PLANS = CommonConstants.CHOREOGRAPHER_URI + CoreCommonConstants.MGMT_URI + "/plan";
	private static final String CHOREOGRAPHER_EXECUTOR_REGISTER = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_REGISTER; 
	private static final String CHOREOGRAPHER_EXECUTOR_UNREGISTER = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER; 

	@Autowired
	private ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockChoreographerExecutorService")
	private ChoreographerExecutorService executorService;

	private MockMvc mockMvc;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		assumeTrue(secure);
		
		final ChoreographerAccessControlFilter chacFilter = appContext.getAutowireCapableBeanFactory().createBean(ChoreographerAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(chacFilter)
									  .build();
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(CHOREOGRAPHER_ECHO)
 				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtServiceNoSysop() throws Exception {
		this.mockMvc.perform(get(CHOREOGRAPHER_MGMT_PLANS)
 				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtServiceSysop() throws Exception {
		this.mockMvc.perform(get(CHOREOGRAPHER_MGMT_PLANS)
 				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorRegisterWithNoSystem() throws Exception {
		// Filter breaks the filter chain => the real controller method will rejects the ill-formed request
		
		postRegister(new ChoreographerExecutorRequestDTO(), "certificates/provider.pem", status().isCreated());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorRegisterWithNoName() throws Exception {
		// Filter breaks the filter chain => the real controller method will rejects the ill-formed request
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(new SystemRequestDTO());
		
		postRegister(request, "certificates/provider.pem", status().isCreated());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorRegisterExecutorNameAndClientNameDoesNotMatch() throws Exception {
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("something-else");
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(systemDto);
		
		postRegister(request, "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorRegisterExecutorNameAndClientNameExactMatch() throws Exception {
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("client-demo-provider");
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(systemDto);
		
		postRegister(request, "certificates/provider.pem", status().isCreated());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorRegisterExecutorNameAndClientNameCaseInsensitiveMatch() throws Exception {
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("CLIENT-demo-provider");
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setSystem(systemDto);
			
		postRegister(request, "certificates/provider.pem", status().isCreated());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorUnregisterWithNoExecutorName() throws Exception {
		// Filter breaks the filter chain => the real controller method will rejects the ill-formed request
		
		deleteUnregister("", "certificates/provider.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorUnregisterExecutorNameAndClientNameDoesNotMatch() throws Exception {
		deleteUnregister("something-else", "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorUnregisterExecutorNameAndClientNameExactMatch() throws Exception {
		deleteUnregister("client-demo-provider", "certificates/provider.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecutorUnregisterExecutorNameAndClientNameCaseInsensitiveMatch() throws Exception {
		deleteUnregister("CLIENT-demo-provider", "certificates/provider.pem", status().isOk());
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void postRegister(final ChoreographerExecutorRequestDTO request, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(CHOREOGRAPHER_EXECUTOR_REGISTER)
			    	.secure(true)
			    	.with(x509(certificatePath))
			    	.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(request))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}

	//-------------------------------------------------------------------------------------------------
	private void deleteUnregister(final String executorName, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(delete(CHOREOGRAPHER_EXECUTOR_UNREGISTER + "?" + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_NAME + "=" + executorName)
			    	.secure(true)
			    	.with(x509(certificatePath)))
					.andExpect(matcher);
	}
}
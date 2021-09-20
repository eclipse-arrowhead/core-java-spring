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

package eu.arrowhead.core.choreographer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepStatus;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChoreographerMain.class)
@ContextConfiguration (classes = { ChoreographerServiceTestContext.class })
public class ChoreographerNotifyControllerTest {
	
	//=================================================================================================
	// members
	
	private static final String CHOREOGRAPHER_NOTIFY = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockJmsService")
    private JmsTemplate jms;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneSessionIdNull() throws Exception {
		final MvcResult result = postNotify(new ChoreographerExecutedStepResultDTO(), status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Invalid session id.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneSessionIdInvalid() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(-1L);
		
		final MvcResult result = postNotify(request, status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Invalid session id.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneSessionStepIdNull() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(1L);
		
		final MvcResult result = postNotify(request, status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Invalid session step id.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneSessionStepIdInvalid() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(1L);
		request.setSessionStepId(-1L);
		
		final MvcResult result = postNotify(request, status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Invalid session step id.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneMissingStatus() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(1L);
		request.setSessionStepId(2L);
		
		final MvcResult result = postNotify(request, status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Missing status.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneErrorMessageNull() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(1L);
		request.setSessionStepId(2L);
		request.setStatus(ChoreographerExecutedStepStatus.ERROR);
		
		final MvcResult result = postNotify(request, status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Message is null or blank.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneErrorMessageEmpty() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(1L);
		request.setSessionStepId(2L);
		request.setStatus(ChoreographerExecutedStepStatus.ERROR);
		request.setMessage(" ");
		
		final MvcResult result = postNotify(request, status().isBadRequest());
		final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, response.getExceptionType());
		Assert.assertEquals(400, response.getErrorCode());
		Assert.assertEquals("Message is null or blank.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testNotifyStepDoneOk() throws Exception {
		final ChoreographerExecutedStepResultDTO request = new ChoreographerExecutedStepResultDTO();
		request.setSessionId(1L);
		request.setSessionStepId(2L);
		request.setStatus(ChoreographerExecutedStepStatus.ERROR);
		request.setMessage("error");
		
		doNothing().when(jms).convertAndSend(eq("session-step-done"), any(ChoreographerExecutedStepResultDTO.class));
		
		postNotify(request, status().isOk());
		
		verify(jms, times(1)).convertAndSend(eq("session-step-done"), any(ChoreographerExecutedStepResultDTO.class));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postNotify(final ChoreographerExecutedStepResultDTO request, final ResultMatcher matcher) throws Exception {
		final MvcResult result = this.mockMvc.perform(post(CHOREOGRAPHER_NOTIFY)
			    							 .contentType(MediaType.APPLICATION_JSON)
			    							 .content(objectMapper.writeValueAsBytes(request))
			    							 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(matcher)
											 .andReturn();
		
		return result;
	}
}
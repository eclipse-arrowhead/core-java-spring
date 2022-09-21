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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.shared.ChoreographerSessionListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStepListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStepResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerWorklogListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerWorklogResponseDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChoreographerMain.class)
@ContextConfiguration (classes = { ChoreographerServiceTestContext.class })
public class ChoreographerSessionControllerTest {

	//=================================================================================================
	// members
	
	private static final String GET_SESSIONS_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_SESSION_MGMT_URI;
	private static final String GET_SESSIONS_STEPS_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_SESSION_STEPS_MGMT_URI;
	private static final String GET_WORKLOG_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_WORKLOG_MGMT_URI;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private ChoreographerSessionDBService sessionDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessions_1() throws Exception {
		final ChoreographerSessionResponseDTO session = new ChoreographerSessionResponseDTO();
		session.setId(54);
		session.setPlanid(5);
		final ChoreographerSessionListResponseDTO returnValue = new ChoreographerSessionListResponseDTO(List.of(session), 1);
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		when(sessionDBService.getSessionsResponse(intCaptor.capture(), intCaptor.capture(), directionCaptor.capture(), stringCaptor.capture(), longCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_SESSIONS_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerSessionListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerSessionListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(Integer.MAX_VALUE, intCaptor.getAllValues().get(1).intValue());
		assertEquals(CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE, directionCaptor.getValue().name());
		assertEquals(CoreCommonConstants.COMMON_FIELD_NAME_ID, stringCaptor.getAllValues().get(0));
		assertTrue(longCaptor.getValue() == null);
		assertTrue(stringCaptor.getAllValues().get(1) == null);
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(session.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessions_2() throws Exception {
		final ChoreographerSessionResponseDTO session = new ChoreographerSessionResponseDTO();
		session.setId(54);
		session.setPlanid(5);
		final ChoreographerSessionListResponseDTO returnValue = new ChoreographerSessionListResponseDTO(List.of(session), 1);
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		when(sessionDBService.getSessionsResponse(intCaptor.capture(), intCaptor.capture(), directionCaptor.capture(), stringCaptor.capture(), longCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_SESSIONS_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE,"20")
											   .param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "DESC")
											   .param(CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, "plan_id")
											   .param("plan_id", "6")
											   .param("status", "DONE")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerSessionListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerSessionListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(20, intCaptor.getAllValues().get(1).intValue());
		assertEquals("DESC", directionCaptor.getValue().name());
		assertEquals("plan_id", stringCaptor.getAllValues().get(0));
		assertEquals(6, longCaptor.getValue().longValue());
		assertEquals("DONE", stringCaptor.getAllValues().get(1));
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(session.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessionSteps_1() throws Exception {
		final ChoreographerSessionStepResponseDTO sessionStep = new ChoreographerSessionStepResponseDTO();
		sessionStep.setId(54);
		final ChoreographerSessionStepListResponseDTO returnValue = new ChoreographerSessionStepListResponseDTO(List.of(sessionStep), 1);
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		when(sessionDBService.getSessionStepsResponse(intCaptor.capture(), intCaptor.capture(), directionCaptor.capture(), stringCaptor.capture(), longCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_SESSIONS_STEPS_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerSessionStepListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerSessionStepListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(Integer.MAX_VALUE, intCaptor.getAllValues().get(1).intValue());
		assertEquals(CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE, directionCaptor.getValue().name());
		assertEquals(CoreCommonConstants.COMMON_FIELD_NAME_ID, stringCaptor.getAllValues().get(0));
		assertTrue(longCaptor.getValue() == null);
		assertTrue(stringCaptor.getAllValues().get(1) == null);
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(sessionStep.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessionSteps_2() throws Exception {
		final ChoreographerSessionStepResponseDTO sessionStep = new ChoreographerSessionStepResponseDTO();
		sessionStep.setId(54);
		final ChoreographerSessionStepListResponseDTO returnValue = new ChoreographerSessionStepListResponseDTO(List.of(sessionStep), 1);
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		when(sessionDBService.getSessionStepsResponse(intCaptor.capture(), intCaptor.capture(), directionCaptor.capture(), stringCaptor.capture(), longCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_SESSIONS_STEPS_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE,"20")
											   .param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "DESC")
											   .param(CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, "session_id")
											   .param("session_id", "6")
											   .param("status", "DONE")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerSessionStepListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerSessionStepListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(20, intCaptor.getAllValues().get(1).intValue());
		assertEquals("DESC", directionCaptor.getValue().name());
		assertEquals("session_id", stringCaptor.getAllValues().get(0));
		assertEquals(6, longCaptor.getValue().longValue());
		assertEquals("DONE", stringCaptor.getAllValues().get(1));
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(sessionStep.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetWorklog_1() throws Exception {
		final ChoreographerWorklogResponseDTO worklog = new ChoreographerWorklogResponseDTO();
		worklog.setId(54);
		final ChoreographerWorklogListResponseDTO returnValue = new ChoreographerWorklogListResponseDTO(List.of(worklog), 1);
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		when(sessionDBService.getWorklogsResponse(intCaptor.capture(), intCaptor.capture(), directionCaptor.capture(), stringCaptor.capture(), longCaptor.capture(),
												  stringCaptor.capture(), stringCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_WORKLOG_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerWorklogListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerWorklogListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(Integer.MAX_VALUE, intCaptor.getAllValues().get(1).intValue());
		assertEquals(CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE, directionCaptor.getValue().name());
		assertEquals(CoreCommonConstants.COMMON_FIELD_NAME_ID, stringCaptor.getAllValues().get(0));
		assertTrue(longCaptor.getValue() == null);
		assertTrue(stringCaptor.getAllValues().get(1) == null);
		assertTrue(stringCaptor.getAllValues().get(2) == null);
		assertTrue(stringCaptor.getAllValues().get(3) == null);
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(worklog.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetWorklog_2() throws Exception {
		final ChoreographerWorklogResponseDTO worklog = new ChoreographerWorklogResponseDTO();
		worklog.setId(54);
		final ChoreographerWorklogListResponseDTO returnValue = new ChoreographerWorklogListResponseDTO(List.of(worklog), 1);
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		when(sessionDBService.getWorklogsResponse(intCaptor.capture(), intCaptor.capture(), directionCaptor.capture(), stringCaptor.capture(), longCaptor.capture(),
				  								  stringCaptor.capture(), stringCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_WORKLOG_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE,"20")
											   .param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "DESC")
											   .param(CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, "session_id")
											   .param("session_id", "6")
											   .param("plan_name", "test-plan")
											   .param("action_name", "test-action")
											   .param("step_name", "test-step")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerWorklogListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerWorklogListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(20, intCaptor.getAllValues().get(1).intValue());
		assertEquals("DESC", directionCaptor.getValue().name());
		assertEquals("session_id", stringCaptor.getAllValues().get(0));
		assertEquals(6, longCaptor.getValue().longValue());
		assertEquals("test-plan", stringCaptor.getAllValues().get(1));
		assertEquals("test-action", stringCaptor.getAllValues().get(2));
		assertEquals("test-step", stringCaptor.getAllValues().get(3));
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(worklog.getId(), result.getData().get(0).getId());
	}
}

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.shared.ChoreographerPlanListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanExecutionChecker;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanValidator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChoreographerMain.class)
@ContextConfiguration (classes = { ChoreographerServiceTestContext.class })
public class ChoreographerPlanControllerTest {
	
	//=================================================================================================
	// members
	
    private static final String PLAN_MGMT_URI = CommonConstants.CHOREOGRAPHER_URI + CoreCommonConstants.MGMT_URI + "/plan";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockChoreographerPlanDBService")
    private ChoreographerPlanDBService planDBService;
    
    @MockBean(name = "mockChoreographerSessionDBService")
    private ChoreographerSessionDBService sessionDBService;
    
    @MockBean(name = "mockPlanValidator")
    private ChoreographerPlanValidator planValidator;

    @MockBean(name = "mockPlanChecker")
    private ChoreographerPlanExecutionChecker planChecker;

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
	public void testEcho() throws Exception {
		this.mockMvc.perform(get("/choreographer/echo")
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlansWithoutParameters() throws Exception {
		final int numOfEntries = 4;
		final ChoreographerPlanListResponseDTO dtoList = createMockPlanDTOList(numOfEntries);
		
		when(planDBService.getPlanEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dtoList);
		
		final MvcResult response = this.mockMvc.perform(get(PLAN_MGMT_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerPlanListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerPlanListResponseDTO.class);
		
		Assert.assertEquals(numOfEntries, responseBody.getData().size());
		Assert.assertEquals(numOfEntries, responseBody.getCount());
		
		verify(planDBService, times(1)).getPlanEntriesResponse(anyInt(), anyInt(), any(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlansWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 4;
		final ChoreographerPlanListResponseDTO dtoList = createMockPlanDTOList(numOfEntries);
		
		when(planDBService.getPlanEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dtoList);
		
		final MvcResult response = this.mockMvc.perform(get(PLAN_MGMT_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(numOfEntries))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerPlanListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerPlanListResponseDTO.class);
		
		Assert.assertEquals(numOfEntries, responseBody.getData().size());
		Assert.assertEquals(numOfEntries, responseBody.getCount());
		
		verify(planDBService, times(1)).getPlanEntriesResponse(anyInt(), anyInt(), any(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlansWithSpecifiedPageButNullSizeParameter() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(PLAN_MGMT_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		Assert.assertEquals(400, responseBody.getErrorCode());
		Assert.assertEquals("Defined page or size could not be with undefined size or page.", responseBody.getErrorMessage());
		
		verify(planDBService, never()).getPlanEntriesResponse(anyInt(), anyInt(), any(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlansWithInvalidSortDirection() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(PLAN_MGMT_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		Assert.assertEquals(400, responseBody.getErrorCode());
		Assert.assertEquals("Invalid sort direction flag", responseBody.getErrorMessage());
		
		verify(planDBService, never()).getPlanEntriesResponse(anyInt(), anyInt(), any(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlanByIdWithInvalidId() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(PLAN_MGMT_URI + "/-1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		Assert.assertEquals(400, responseBody.getErrorCode());
		Assert.assertEquals("ID must be greater than 0.", responseBody.getErrorMessage());
		
		verify(planDBService, never()).getPlanByIdResponse(anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlanByIdWithValidId() throws Exception {
		final ChoreographerPlanResponseDTO dto = new ChoreographerPlanResponseDTO();
		dto.setId(1);
		dto.setName("plan");
		
		when(planDBService.getPlanByIdResponse(1)).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(PLAN_MGMT_URI + "/1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerPlanResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerPlanResponseDTO.class);
		
		Assert.assertEquals("plan", responseBody.getName());
		
		verify(planDBService, times(1)).getPlanByIdResponse(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemovePlanByIdWithInvalidId() throws Exception {
		final MvcResult response = this.mockMvc.perform(delete(PLAN_MGMT_URI + "/-1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		Assert.assertEquals(400, responseBody.getErrorCode());
		Assert.assertEquals("ID must be greater than 0.", responseBody.getErrorMessage());
		
		verify(planDBService, never()).removePlanEntryById(anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemovePlanByIdWithValidId() throws Exception {
		final ChoreographerPlanResponseDTO dto = new ChoreographerPlanResponseDTO();
		dto.setId(1);
		dto.setName("plan");
		
		doNothing().when(planDBService).removePlanEntryById(1);
		
		this.mockMvc.perform(delete(PLAN_MGMT_URI + "/1")
		 		    .accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
		
		verify(planDBService, times(1)).removePlanEntryById(1);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterPlanInvalidInput() throws Exception {
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		
		when(planValidator.validatePlan(any(ChoreographerPlanRequestDTO.class), anyString())).thenThrow(new InvalidParameterException("Plan name is null or blank."));
		
		final MvcResult response = this.mockMvc.perform(post(PLAN_MGMT_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isBadRequest())
											   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.INVALID_PARAMETER, responseBody.getExceptionType());
		Assert.assertEquals(400, responseBody.getErrorCode());
		Assert.assertEquals("Plan name is null or blank.", responseBody.getErrorMessage());
		
		verify(planValidator, times(1)).validatePlan(any(ChoreographerPlanRequestDTO.class), anyString());
		verify(planDBService, never()).createPlanResponse(any(ChoreographerPlanRequestDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterPlanValidInput() throws Exception {
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		final ChoreographerPlanResponseDTO responseDTO = new ChoreographerPlanResponseDTO();
		responseDTO.setId(1);
		responseDTO.setName("plan");
		
		when(planValidator.validatePlan(any(ChoreographerPlanRequestDTO.class), anyString())).thenReturn(request);
		when(planDBService.createPlanResponse(any(ChoreographerPlanRequestDTO.class))).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(PLAN_MGMT_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isCreated())
											   .andReturn();
		
		final ChoreographerPlanResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerPlanResponseDTO.class);
		
		Assert.assertEquals("plan", responseBody.getName());
		
		verify(planValidator, times(1)).validatePlan(any(ChoreographerPlanRequestDTO.class), anyString());
		verify(planDBService, times(1)).createPlanResponse(any(ChoreographerPlanRequestDTO.class));
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerPlanListResponseDTO createMockPlanDTOList(int numOfEntries) {
		final List<ChoreographerPlanResponseDTO> list = new ArrayList<>(numOfEntries);
		for (int i = 0; i < numOfEntries; ++i) {
			list.add(new ChoreographerPlanResponseDTO());
		}
 		
		return new ChoreographerPlanListResponseDTO(list, numOfEntries);	
	}
}
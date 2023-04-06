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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.internal.ChoreographerExecutorListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.core.choreographer.service.ChoreographerExecutorService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChoreographerMain.class)
@ContextConfiguration (classes = { ChoreographerServiceTestContext.class })
public class ChoreographerExecutorControllerTest {
	
	//=================================================================================================
	// members
	
	private static final String ADD_EXECUTOR_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_URI;
	private static final String REMOVE_EXECUTOR_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_BY_ID_URI;
	private static final String GET_EXECUTORS_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_URI;
	private static final String GET_EXECUTOR_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_BY_ID_URI;
	private static final String REGISTER_EXECUTOR_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_REGISTER;
	private static final String UNREGISTER_EXECUTOR_URI = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private ChoreographerExecutorService executorService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddExecutor() throws JsonProcessingException, Exception {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setServiceDefinitionName("test-name");
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(5);
		
		final ArgumentCaptor<ChoreographerExecutorRequestDTO> requestCaptor = ArgumentCaptor.forClass(ChoreographerExecutorRequestDTO.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		when(executorService.addExecutorSystem(requestCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(post(ADD_EXECUTOR_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isCreated())
											   .andReturn();
		
		final ChoreographerExecutorResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerExecutorResponseDTO.class);
		
		assertEquals(request.getServiceDefinitionName(), requestCaptor.getValue().getServiceDefinitionName());
		assertEquals(ADD_EXECUTOR_URI, stringCaptor.getValue());
		assertEquals(returnValue.getId(), result.getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveExecutor() throws JsonProcessingException, Exception {
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		doNothing().when(executorService).removeExecutorSystem(longCaptor.capture(), stringCaptor.capture());
		
		this.mockMvc.perform(delete(REMOVE_EXECUTOR_URI.replace("{id}", "5"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
		
		assertEquals(5, longCaptor.getValue().longValue());
		assertEquals(REMOVE_EXECUTOR_URI, stringCaptor.getValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutors_1() throws JsonProcessingException, Exception {
		final ChoreographerExecutorResponseDTO executor = new ChoreographerExecutorResponseDTO();
		executor.setId(5);
		final ChoreographerExecutorListResponseDTO returnValue = new ChoreographerExecutorListResponseDTO();
		returnValue.setCount(1);
		returnValue.setData(List.of(executor));
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		when(executorService.getExecutors(intCaptor.capture(), intCaptor.capture(), stringCaptor.capture(), stringCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_EXECUTORS_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE,"20")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerExecutorListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerExecutorListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(20, intCaptor.getAllValues().get(1).intValue());
		assertEquals(CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE, stringCaptor.getAllValues().get(0));
		assertEquals(CoreCommonConstants.COMMON_FIELD_NAME_ID, stringCaptor.getAllValues().get(1));
		assertEquals(GET_EXECUTORS_URI, stringCaptor.getAllValues().get(2));
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(executor.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutors_2() throws JsonProcessingException, Exception {
		final ChoreographerExecutorResponseDTO executor = new ChoreographerExecutorResponseDTO();
		executor.setId(5);
		final ChoreographerExecutorListResponseDTO returnValue = new ChoreographerExecutorListResponseDTO();
		returnValue.setCount(1);
		returnValue.setData(List.of(executor));
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		when(executorService.getExecutors(intCaptor.capture(), intCaptor.capture(), stringCaptor.capture(), stringCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_EXECUTORS_URI)
											   .param(CoreCommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "20")
											   .param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "DESC")
											   .param(CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, "name")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final ChoreographerExecutorListResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerExecutorListResponseDTO.class);
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(20, intCaptor.getAllValues().get(1).intValue());
		assertEquals("DESC", stringCaptor.getAllValues().get(0));
		assertEquals("name", stringCaptor.getAllValues().get(1));
		assertEquals(GET_EXECUTORS_URI, stringCaptor.getAllValues().get(2));
		assertEquals(returnValue.getCount(), result.getData().size());
		assertEquals(executor.getId(), result.getData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetExecutorById() throws JsonProcessingException, Exception {
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(52L);
		returnValue.setPort(5000);
		
		final ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		when(executorService.getExecutorById(longCaptor.capture(), stringCaptor.capture())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(get(GET_EXECUTOR_URI.replace("{id}", String.valueOf(returnValue.getId())))
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isOk())
												.andReturn();
		
		final ChoreographerExecutorResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerExecutorResponseDTO.class);
		
		assertEquals(returnValue.getId(), longCaptor.getValue().longValue());
		assertEquals(GET_EXECUTOR_URI, stringCaptor.getValue());
		assertEquals(returnValue.getId(), result.getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterExecutor() throws JsonProcessingException, Exception {
		final ChoreographerExecutorRequestDTO request = new ChoreographerExecutorRequestDTO();
		request.setServiceDefinitionName("test-name");
		
		final ChoreographerExecutorResponseDTO returnValue = new ChoreographerExecutorResponseDTO();
		returnValue.setId(5);
		
		final ArgumentCaptor<ChoreographerExecutorRequestDTO> requestCaptor = ArgumentCaptor.forClass(ChoreographerExecutorRequestDTO.class);
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		when(executorService.registerExecutorSystem(requestCaptor.capture(), stringCaptor.capture(), any())).thenReturn(returnValue);
		
		final MvcResult response = this.mockMvc.perform(post(REGISTER_EXECUTOR_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isCreated())
											   .andReturn();
		
		final ChoreographerExecutorResponseDTO result = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ChoreographerExecutorResponseDTO.class);
		
		assertEquals(request.getServiceDefinitionName(), requestCaptor.getValue().getServiceDefinitionName());
		assertEquals(REGISTER_EXECUTOR_URI, stringCaptor.getValue());
		assertEquals(returnValue.getId(), result.getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterExecutor() throws JsonProcessingException, Exception {
		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		doNothing().when(executorService).unregisterExecutorSystem(stringCaptor.capture(), stringCaptor.capture());
		
		this.mockMvc.perform(delete(UNREGISTER_EXECUTOR_URI)
					.param(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_NAME, "test-name")
					.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isOk())
				   	.andReturn();
		
		assertEquals("test-name", stringCaptor.getAllValues().get(0));
		assertEquals(UNREGISTER_EXECUTOR_URI, stringCaptor.getAllValues().get(1));
	}
}

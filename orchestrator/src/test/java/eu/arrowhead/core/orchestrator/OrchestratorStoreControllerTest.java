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

package eu.arrowhead.core.orchestrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.internal.SystemDescriberDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreFlexibleDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrchestratorMain.class)
@ContextConfiguration(classes = { OrchestratorServiceTestContext.class })
public class OrchestratorStoreControllerTest {

	//=================================================================================================
	// members

	private static final String ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER = CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_by_consumer";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private OrchestratorStoreController controller;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockOrchestratorStoreDBService") 
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	@MockBean
	private OrchestratorStoreFlexibleDBService orchestratorStoreFlexibleDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//=================================================================================================
	// Test of getOrchestratorStoreById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreResponseDTO dto = getOrchestratorStoreResponseDTOForTest();
		when(orchestratorStoreDBService.getOrchestratorStoreByIdResponse(anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdWithInvalidIdTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of getOrchestratorStores
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresInvalidDirectionParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresNullPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresNullItemPerPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.param(CoreCommonConstants.REQUEST_PARAM_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresNullItemPerPageParamAndNullPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//=================================================================================================
	// Test of getAllTopPriorityOrchestratorStores
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresInvalidDirectionParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
					.param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresNullPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
					.param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresNullItemPerPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
					.param(CoreCommonConstants.REQUEST_PARAM_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresNullItemPerPageParamAndNullPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}

	
	//=================================================================================================
	// Test of getOrchestratorStoresByConsumer
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), any(), any())).thenReturn(dto);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getLocalOrchestratorStoreRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getLocalOrchestratorStoreRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerInvalidDirectionParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), any(), any())).thenReturn(dto);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.param(CoreCommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.param(CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullItemPerPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.param(CoreCommonConstants.REQUEST_PARAM_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullItemPerPageParamAndNullPageParamTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), any(), any())).thenReturn(dto);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getLocalOrchestratorStoreRequestDTOForTest()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullIRequestTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), any(), any())).thenReturn(dto);
		
		final OrchestratorStoreRequestDTO request = null;
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullConsumerSystemIdTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), any(), any())).thenReturn(dto);
		
		final OrchestratorStoreRequestDTO request = getLocalOrchestratorStoreRequestDTOForTest();
		request.setConsumerSystemId(null);
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullerviceDefinitionIdTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), any(), any())).thenReturn(dto);
		
		final OrchestratorStoreRequestDTO request = getLocalOrchestratorStoreRequestDTOForTest();
		request.setServiceDefinitionName(null);
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// Test of addOrchestratorStoreEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.createOrchestratorStoresResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestDTO> request =  getLocalOrchestratorStoreRequestDTOListForTest(3);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final List<OrchestratorStoreRequestDTO> request =  getLocalOrchestratorStoreRequestDTOListForTest(3);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesNullRequestTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.createOrchestratorStoresResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestDTO> request =  null;
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesNullPiorityTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.createOrchestratorStoresResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestDTO> request =  getLocalOrchestratorStoreRequestDTOListForNullPriorityTest(3);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesNullServiceIntrfaceTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.createOrchestratorStoresResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestDTO> request =  getOrchestratorStoreRequestDTOListForNullServiceInterfaceIdTest(3);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesListElementsNullTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		
		when(orchestratorStoreDBService.createOrchestratorStoresResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestDTO> request =  getOrchestratorStoreRequestDTOListForListElementsNullTest(3);
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of removeOrchestratorStore
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreInvalidIdTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of modifyPriorities
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreModifyPriorityRequestDTO request = getOrchestratorStoreModifyPriorityRequestDTO(3);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreModifyPriorityRequestDTO request = getOrchestratorStoreModifyPriorityRequestDTO(3);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesNullRequestTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreModifyPriorityRequestDTO request = null;
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesEmptyMapTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreModifyPriorityRequestDTO request = getOrchestratorStoreModifyPriorityRequestDTOWithEmptyMap();
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of getOrchestratorFlexibleStoreRules
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorFlexibleStoreRulesTestWithoutParameters() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> dirCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		when(orchestratorStoreFlexibleDBService.getOrchestratorStoreFlexibleEntriesResponse(intCaptor.capture(), intCaptor.capture(), dirCaptor.capture(), strCaptor.capture())).thenReturn(new OrchestratorStoreFlexibleListResponseDTO());
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
					.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isOk());
		
		assertEquals(0, intCaptor.getAllValues().get(0).intValue());
		assertEquals(Integer.MAX_VALUE, intCaptor.getAllValues().get(1).intValue());
		assertEquals("asc", dirCaptor.getValue().toString().toLowerCase());
		assertEquals("id", strCaptor.getValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorFlexibleStoreRulesTestWithParameters() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final int page = 0;
		final int size = 25;
		final String direction = "asc";
		final String sortField = "id";
		
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Direction> dirCaptor = ArgumentCaptor.forClass(Direction.class);
		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		when(orchestratorStoreFlexibleDBService.getOrchestratorStoreFlexibleEntriesResponse(intCaptor.capture(), intCaptor.capture(), dirCaptor.capture(), strCaptor.capture())).thenReturn(new OrchestratorStoreFlexibleListResponseDTO());
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
					.param("page", String.valueOf(page))
				   	.param("item_per_page", String.valueOf(size))
				   	.param("direction", direction)
				   	.param("sort_field", sortField)
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isOk());
		
		assertEquals(page, intCaptor.getAllValues().get(0).intValue());
		assertEquals(size, intCaptor.getAllValues().get(1).intValue());
		assertEquals(direction, dirCaptor.getValue().toString().toLowerCase());
		assertEquals(sortField, strCaptor.getValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorFlexibleStoreRulesTestWithFlagFalse() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
					.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).getOrchestratorStoreFlexibleEntriesResponse(anyInt(), anyInt(), any(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorFlexibleStoreRulesTestWithInvalidDirection() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
					.param("page", "0")
				   	.param("item_per_page", "5")
				   	.param("direction", "invalid")
				   	.param("sort_field", "id")
					.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).getOrchestratorStoreFlexibleEntriesResponse(anyInt(), anyInt(), any(), anyString());
	}
	
	//=================================================================================================
	// Test of addOrchestratorFlexibleStoreRulesWithSysNamesTest
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void addOrchestratorFlexibleStoreRulesWithSysNamesTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO("consumer", null),
																									   new SystemDescriberDTO("provider", null),
																									   "service",
																									   "HTTP-SECURE-JSON",
																									   null,
																									   10);
		
		final ArgumentCaptor<List<OrchestratorStoreFlexibleRequestDTO>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleDBService.createOrchestratorStoreFlexibleResponse(captor.capture())).thenReturn(new OrchestratorStoreFlexibleListResponseDTO(List.of(new OrchestratorStoreFlexibleResponseDTO()), 1));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isCreated());
		
		final OrchestratorStoreFlexibleRequestDTO captured = captor.getValue().get(0);
		assertEquals(requestDTO.getConsumerSystem().getSystemName(), captured.getConsumerSystem().getSystemName());
		assertEquals(requestDTO.getProviderSystem().getSystemName(), captured.getProviderSystem().getSystemName());
		assertEquals(requestDTO.getServiceDefinitionName(), captured.getServiceDefinitionName());
		assertEquals(requestDTO.getServiceInterfaceName(), captured.getServiceInterfaceName());
		assertEquals(requestDTO.getPriority().intValue(), captured.getPriority().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void addOrchestratorFlexibleStoreRulesOkWithSysMetaTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		final ArgumentCaptor<List<OrchestratorStoreFlexibleRequestDTO>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleDBService.createOrchestratorStoreFlexibleResponse(captor.capture())).thenReturn(new OrchestratorStoreFlexibleListResponseDTO(List.of(new OrchestratorStoreFlexibleResponseDTO()), 1));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isCreated());
		
		final OrchestratorStoreFlexibleRequestDTO captured = captor.getValue().get(0);
		assertTrue(captured.getConsumerSystem().getMetadata().entrySet().containsAll(requestDTO.getConsumerSystem().getMetadata().entrySet()));
		assertTrue(captured.getProviderSystem().getMetadata().entrySet().containsAll(requestDTO.getProviderSystem().getMetadata().entrySet()));
		assertEquals(requestDTO.getServiceDefinitionName(), captured.getServiceDefinitionName());
		assertTrue(captured.getServiceMetadata().entrySet().containsAll(requestDTO.getServiceMetadata().entrySet()));
		assertEquals(requestDTO.getPriority().intValue(), captured.getPriority().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorFlexibleStoreRulesWithFlagFalseTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorFlexibleStoreRulesWithInvalidConsumerDescriptorTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, null),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorFlexibleStoreRulesWithInvalidProviderDescriptorTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, null),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorFlexibleStoreRulesWithInvalidConsumerNameTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO("-invalid", Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorFlexibleStoreRulesWithInvalidProviderNameTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO("-invalid", Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorFlexibleStoreRulesWithInvalidInterfaceNameTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   "invalid",
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_MGMT_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorFlexibleStoreRuleByIdOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		doNothing().when(orchestratorStoreFlexibleDBService).deleteOrchestratorStoreFlexibleById(captor.capture());
		
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_BY_ID_MGMT_URI.replace("{id}", "5"))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());
		
		assertEquals(5, captor.getValue().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorFlexibleStoreRuleByIdFlagFalseTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		doNothing().when(orchestratorStoreFlexibleDBService).deleteOrchestratorStoreFlexibleById(captor.capture());
		
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.ORCHESTRATOR_STORE_FLEXIBLE_BY_ID_MGMT_URI.replace("{id}", "5"))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).deleteOrchestratorStoreFlexibleById(anyInt());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestDTO getLocalOrchestratorStoreRequestDTOForTest() {
		final String serviceDefinitionName = "serviceDefinitionNameForTest";
		final Long consumerSystemId = 1L;
		final SystemRequestDTO providerSystemDTO = getProviderSystemRequestDTOForTest();
		final CloudRequestDTO cloudDTO = getLocalProviderCloudRequestDTOForTest();
		final String serviceInterfaceName = "serviceIntrfaceNameForTest";
		final Integer priority = 1;	
		final Map<String,String> attribute = new HashMap<>();
		
		return new OrchestratorStoreRequestDTO(serviceDefinitionName, consumerSystemId,	providerSystemDTO, cloudDTO, serviceInterfaceName, priority, attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getLocalProviderCloudRequestDTOForTest() {
		final String operator = "operatorForTest";
		final String name = "cloudName";
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(operator);
		cloudRequestDTO.setName(name);
		
		return cloudRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getProviderSystemRequestDTOForTest() {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setAddress("localhost");
		systemRequestDTO.setSystemName("systemNameForTest");
		systemRequestDTO.setPort(12345);
		
		return systemRequestDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestDTO> getLocalOrchestratorStoreRequestDTOListForTest(final int listSize) {
		final List<OrchestratorStoreRequestDTO> orchestratorStoreRequestDTOList = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; ++i) {
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO = getLocalOrchestratorStoreRequestDTOForTest();
			orchestratorStoreRequestDTO.setProviderSystem(getProviderSystemRequestDTOForTest());
			orchestratorStoreRequestDTO.setPriority(i + 1);
			orchestratorStoreRequestDTOList.add(orchestratorStoreRequestDTO);
		}
		
		return orchestratorStoreRequestDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestDTO> getLocalOrchestratorStoreRequestDTOListForNullPriorityTest(final int listSize) {
		final List<OrchestratorStoreRequestDTO> orchestratorStoreRequestDTOList = new ArrayList<OrchestratorStoreRequestDTO>(listSize);
		for (int i = 0; i < listSize; ++i) {
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO = getLocalOrchestratorStoreRequestDTOForTest();
			orchestratorStoreRequestDTO.setPriority(null);
			orchestratorStoreRequestDTOList.add(orchestratorStoreRequestDTO);
		}
		
		return orchestratorStoreRequestDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestDTO> getOrchestratorStoreRequestDTOListForNullServiceInterfaceIdTest(final int listSize) {
		final List<OrchestratorStoreRequestDTO> orchestratorStoreRequestDTOList = new ArrayList<OrchestratorStoreRequestDTO>(listSize);
		for (int i = 0; i < listSize; ++i) {
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO = getLocalOrchestratorStoreRequestDTOForTest();
			orchestratorStoreRequestDTO.setServiceInterfaceName(null);
			orchestratorStoreRequestDTOList.add(orchestratorStoreRequestDTO);
		}
		
		return orchestratorStoreRequestDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestDTO> getOrchestratorStoreRequestDTOListForListElementsNullTest(final int listSize) {
		final List<OrchestratorStoreRequestDTO> orchestratorStoreRequestDTOList = new ArrayList<OrchestratorStoreRequestDTO>(listSize);
		for (int i = 0; i < listSize; ++i) {
			orchestratorStoreRequestDTOList.add(null);
		}
		
		return orchestratorStoreRequestDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreResponseDTO getOrchestratorStoreResponseDTOForTest() {
		return new OrchestratorStoreResponseDTO(getIdForTest(), getServiceDefinitionResponseDTOForTest(), getConsumerSystemResponseDTOForTest(), false, getSystemResponseDTOForTest(),
												getCloudResponseDTOForTest(), getServiceInterfaceResponseDTO(), getPriorityForTest(), getAttributeForTest(), getCreatedAtForTest(),
												getUpdatedAtForTest());
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceInterfaceResponseDTO getServiceInterfaceResponseDTO() {
		return new ServiceInterfaceResponseDTO(getIdForTest(), "interfaceNameForTest", getCreatedAtForTest(), getUpdatedAtForTest());
	}

	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreResponseDTO> getOrchestratorStoreResponseDTOListForTest(final int size) {
		final List<OrchestratorStoreResponseDTO> orchestratorStoreResponseDTOList = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			final OrchestratorStoreResponseDTO orchestratorStoreResponseDTO = getOrchestratorStoreResponseDTOForTest();
			orchestratorStoreResponseDTO.setId(i + 1L);
			orchestratorStoreResponseDTO.getProviderSystem();
			orchestratorStoreResponseDTO.setPriority(i + 1);
			
			orchestratorStoreResponseDTOList.add(orchestratorStoreResponseDTO);
		}
		
		return orchestratorStoreResponseDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreListResponseDTO getOrchestratorStoreListResponseDTOForTest(final int size) {
		final int count = size;
		final List<OrchestratorStoreResponseDTO> data = getOrchestratorStoreResponseDTOListForTest(size);
		
		return new OrchestratorStoreListResponseDTO(data, count);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreModifyPriorityRequestDTO getOrchestratorStoreModifyPriorityRequestDTO(final int size) {
		final OrchestratorStoreModifyPriorityRequestDTO request = new OrchestratorStoreModifyPriorityRequestDTO();
		
		final Map<Long,Integer> priorityMap = new HashMap<>(size); 
		for (int i = 0; i < size; ++i) {
			priorityMap.put(i + 1L, i + 1);
		}		
		
		request.setPriorityMap(priorityMap);
		
		return request;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreModifyPriorityRequestDTO getOrchestratorStoreModifyPriorityRequestDTOWithEmptyMap() {
		final OrchestratorStoreModifyPriorityRequestDTO request = new OrchestratorStoreModifyPriorityRequestDTO();
		request.setPriorityMap(Map.of());
		
		return request;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getUpdatedAtForTest() { return "2019-07-04 14:43:19"; }
	
	//-------------------------------------------------------------------------------------------------
	private String getCreatedAtForTest() { return "2019-07-04 14:43:19"; } 
	
	//-------------------------------------------------------------------------------------------------
	private Map<String,String> getAttributeForTest() {
		return new HashMap<>();
	}
	
	//-------------------------------------------------------------------------------------------------
	private Integer getPriorityForTest() { return 1; }
	
	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO getCloudResponseDTOForTest() {
		return new CloudResponseDTO(getIdForTest(), "operator", "name", true,	true, false, null, "2019-07-04 14:43:19", "2019-07-04 14:43:19");

	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {
		return new SystemResponseDTO(getIdForTest(), "systemName", "address", 12345, "authenticationInfo", null, "2019-07-04 14:43:19", "2019-07-04 14:43:19");
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getConsumerSystemResponseDTOForTest() {
		return getSystemResponseDTOForTest();
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinitionResponseDTO getServiceDefinitionResponseDTOForTest() {
		return new ServiceDefinitionResponseDTO(getIdForTest(), "serviceDefinition", "2019-07-04 14:43:19", "2019-07-04 14:43:19");
	}

	//-------------------------------------------------------------------------------------------------
	private long getIdForTest() { return 1L; }
}
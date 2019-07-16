package eu.arrowhead.core.orchestrator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestByIdDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = OrchestratorMain.class)
@ContextConfiguration (classes = { OrchestratorStoreDBServiceTestContext.class })
public class OrchestratorStoreControllerTest {

	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockOrchestratorStoreDBService") 
	OrchestratorStoreDBService orchestratorStoreDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of getOrchestratorStoreById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdOkTest() throws Exception {
		
		OrchestratorStoreResponseDTO dto = getOrchestratorStoreResponseDTOForTest();
		when(orchestratorStoreDBService.getOrchestratorStoreById(anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdWithInvalidIdTest() throws Exception {
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of getOrchestratorStores
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresOkTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresInvalidDirectionParamTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresNullPageParamTest() throws Exception {
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresNullItemPerPageParamTest() throws Exception {

		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_PAGE, "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresNullItemPerPageParamAndNullPageParamTest() throws Exception {

		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of getAllTopPriorityOrchestratorStores
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresOkTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresInvalidDirectionParamTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
				.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresNullPageParamTest() throws Exception {
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresNullItemPerPageParamTest() throws Exception {

		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
				.param(CommonConstants.REQUEST_PARAM_PAGE, "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoresNullItemPerPageParamAndNullPageParamTest() throws Exception {

		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	
	//-------------------------------------------------------------------------------------------------
	// Test of getOrchestratorStoresByConsumer
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerOkTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getOrchestratorStoreRequestByIdDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerInvalidDirectionParamTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullPageParamTest() throws Exception {
		
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullItemPerPageParamTest() throws Exception {

		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_PAGE, "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullItemPerPageParamAndNullPageParamTest() throws Exception {

		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getOrchestratorStoreRequestByIdDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullIRequestTest() throws Exception {

		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), anyLong())).thenReturn(dto);
		
		final OrchestratorStoreRequestByIdDTO request = null;
	
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullConsumerSystemIdTest() throws Exception {

		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), anyLong())).thenReturn(dto);
		
		final OrchestratorStoreRequestByIdDTO request = getOrchestratorStoreRequestByIdDTOForTest();
		request.setConsumerSystemId(null);
	
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerNullerviceDefinitionIdTest() throws Exception {

		final OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(anyInt(), anyInt(), any(), anyString(), anyLong(), anyLong())).thenReturn(dto);
		
		final OrchestratorStoreRequestByIdDTO request = getOrchestratorStoreRequestByIdDTOForTest();
		request.setServiceDefinitionId(null);
	
		this.mockMvc.perform(put(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}

	//-------------------------------------------------------------------------------------------------
	// Test of addOrchestratorStoreEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesOkTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.createOrchestratorStoresByIdResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestByIdDTO> request =  getOrchestratorStoreRequestByIdDTOListForTest(3);
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesNullRequestTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.createOrchestratorStoresByIdResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestByIdDTO> request =  null;
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesNullPiorityTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.createOrchestratorStoresByIdResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestByIdDTO> request =  getOrchestratorStoreRequestByIdDTOListForNullPriorityTest(3);
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addOrchestratorStoreEntriesListElementsNullTest() throws Exception {
		
		OrchestratorStoreListResponseDTO dto = getOrchestratorStoreListResponseDTOForTest(3);
		when(orchestratorStoreDBService.createOrchestratorStoresByIdResponse(any())).thenReturn(dto);
		
		final List<OrchestratorStoreRequestByIdDTO> request =  getOrchestratorStoreRequestByIdDTOListForListElementsNullTest(3);
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of removeOrchestratorStore
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreOkTest() throws Exception {
	
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreInvalidIdTest() throws Exception {
	
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of modifyPriorities
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesOkTest() throws Exception {
		
		final OrchestratorStoreModifyPriorityRequestDTO request = getOrchestratorStoreModifyPriorityRequestDTO(3);
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesNullRequestTest() throws Exception {
		
		final OrchestratorStoreModifyPriorityRequestDTO request = null;
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyPrioritiesEmptyMapTest() throws Exception {
		
		final OrchestratorStoreModifyPriorityRequestDTO request = getOrchestratorStoreModifyPriorityRequestDTOWithEmptyMap();
	
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOListForTest(final int listSize) {
		
		final List<OrchestratorStoreRequestByIdDTO> orchestratorStoreRequestByIdDTOList = new ArrayList<OrchestratorStoreRequestByIdDTO>(listSize);
		
		for (int i = 0; i < listSize; i++) {
			
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO = getOrchestratorStoreRequestByIdDTOForTest();
			orchestratorStoreRequestByIdDTO.setProviderSystemId(i + 1L);
			orchestratorStoreRequestByIdDTO.setPriority(i + 1);
			orchestratorStoreRequestByIdDTOList.add(orchestratorStoreRequestByIdDTO);
		}
		return orchestratorStoreRequestByIdDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOListForNullPriorityTest(final int listSize) {
		
		final List<OrchestratorStoreRequestByIdDTO> orchestratorStoreRequestByIdDTOList = new ArrayList<OrchestratorStoreRequestByIdDTO>(listSize);
		
		for (int i = 0; i < listSize; i++) {
			
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO = getOrchestratorStoreRequestByIdDTOForTest();
			orchestratorStoreRequestByIdDTO.setProviderSystemId(i + 1L);
			orchestratorStoreRequestByIdDTO.setPriority(null);
			orchestratorStoreRequestByIdDTOList.add(orchestratorStoreRequestByIdDTO);
		}
		return orchestratorStoreRequestByIdDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOListForListElementsNullTest(final int listSize) {
		
		final List<OrchestratorStoreRequestByIdDTO> orchestratorStoreRequestByIdDTOList = new ArrayList<OrchestratorStoreRequestByIdDTO>(listSize);
		
		for (int i = 0; i < listSize; i++) {
			
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO = null;
			orchestratorStoreRequestByIdDTOList.add(orchestratorStoreRequestByIdDTO);
		}
		
		return orchestratorStoreRequestByIdDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreResponseDTO getOrchestratorStoreResponseDTOForTest() {
		
		return new OrchestratorStoreResponseDTO(
				getIdForTest(),
				getServiceDefinitionResponseDTOForTest(),
				getConsumerSystemResponseDTOForTest(),
				getProviderSystemResponseDTOForTest(),
				getProviderCloudResponseDTOForTest(),
				getPriorityForTest(),
				getAttributeForTest(),
				getCreatedAtForTest(),
				getUpdatedAtForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreResponseDTO> getOrchestratorStoreResponseDTOListForTest(final int size) {
		
		final List<OrchestratorStoreResponseDTO> orchestratorStoreResponseDTOList = new ArrayList<>(size);
		
		for (int i = 0; i < size; i++) {
			final OrchestratorStoreResponseDTO orchestratorStoreResponseDTO = getOrchestratorStoreResponseDTOForTest();
			orchestratorStoreResponseDTO.setId(i + 1L);
			orchestratorStoreResponseDTO.getProviderSystem().setId(i + 1L);
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
		
		final Map<Long, Integer> priorityMap = new HashMap<Long, Integer>(size); 
		for (int i = 0; i < size; i++) {
			priorityMap.put(i + 1L, i + 1);
		}		
		request.setPriorityMap(priorityMap);
		
		return request;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreModifyPriorityRequestDTO getOrchestratorStoreModifyPriorityRequestDTOWithEmptyMap() {
		final OrchestratorStoreModifyPriorityRequestDTO request = new OrchestratorStoreModifyPriorityRequestDTO();
		
		final Map<Long, Integer> priorityMap = new HashMap<Long, Integer>(0); 
		
		request.setPriorityMap(priorityMap);
		
		return request;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getUpdatedAtForTest() {
			
		return "2019-07-04 14:43:19";
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getCreatedAtForTest() {
			
		return "2019-07-04 14:43:19";
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<String,String> getAttributeForTest() {
			
		return new HashMap<String, String>();
	}
	
	//-------------------------------------------------------------------------------------------------
	private Integer getPriorityForTest() {
			
		return 1;
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO getCloudResponseDTOForTest() {
			
		return new CloudResponseDTO(
				getIdForTest(), 
				"operator", 
				"name", 
				"address", 
				12345, 
				"gateKeeperServiceUri", 
				true, 
				true, 
				true, 
				"2019-07-04 14:43:19", 
				"2019-07-04 14:43:19");
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO getProviderCloudResponseDTOForTest() {
			
		return getCloudResponseDTOForTest();
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {
		return new SystemResponseDTO(
				getIdForTest(), 
				"systemName", 
				"address", 
				12345, 
				"authenticationInfo", 
				"2019-07-04 14:43:19", 
				"2019-07-04 14:43:19");
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getProviderSystemResponseDTOForTest() {

		return getSystemResponseDTOForTest();
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getConsumerSystemResponseDTOForTest() {
			
			return getSystemResponseDTOForTest();
		}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinitionResponseDTO getServiceDefinitionResponseDTOForTest() {
			
		return new ServiceDefinitionResponseDTO(
				getIdForTest(),
				"serviceDefinition",
				"2019-07-04 14:43:19",
				"2019-07-04 14:43:19");
	}

	//-------------------------------------------------------------------------------------------------
	private long getIdForTest() {
		
		return 1L;
	}
}

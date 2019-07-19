package eu.arrowhead.core.authorization;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.AuthorizationIntraCloudCheckRequestDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudRequestDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.IdIdListDTO;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@ContextConfiguration(classes = AuthorizationDBServiceTestContext.class)
public class AuthorizationControllerIntraCloudTest {
	
	//=================================================================================================
	// members
	
	private static final String AUTHORIZATION_INTRA_CLOUD_MGMT_URI = "/authorization/mgmt/intracloud";
	private static final String AUTHORIZATION_INTRA_CLOUD_CHECK_URI = "/authorization/intracloud/check";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockAuthorizationDBService")
	private AuthorizationDBService authorizationDBService;
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//=================================================================================================
	// Tests of getAuthorizationIntraClouds
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationIntraCloudsWithoutParameters() throws Exception {
		final int numOfEntries = 4;
		final AuthorizationIntraCloudListResponseDTO dto = DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(
																																			createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getAuthorizationIntraCloudEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationIntraCloudListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationIntraCloudListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationIntraCloudsWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 4;
		final AuthorizationIntraCloudListResponseDTO dto = DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(
																																			createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getAuthorizationIntraCloudEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
											   .param(CommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(numOfEntries))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationIntraCloudListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationIntraCloudListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationIntraCloudsWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(5))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationIntraCloudsWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.param(CommonConstants.REQUEST_PARAM_PAGE, "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationIntraCloudsWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationIntraCloudsWithExistingId() throws Exception {
		final AuthorizationIntraCloudResponseDTO dto = DTOConverter.convertAuthorizationIntraCloudToAuthorizationIntraCloudResponseDTO(createPageForMockingAuthorizationDBService(1).getContent().
																																	   get(0));
		when(authorizationDBService.getAuthorizationIntraCloudEntryByIdResponse(anyLong())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI + "/1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationIntraCloudResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationIntraCloudResponseDTO.class);
		assertEquals("Consumer", responseBody.getConsumerSystem().getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationIntraCloudsWithInvalidId() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTRA_CLOUD_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of removeAuthorizationIntraCloudById
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRemoveAuthorizationIntraCloudByIdWithExistingId() throws Exception {
		this.mockMvc.perform(delete(AUTHORIZATION_INTRA_CLOUD_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRemoveAuthorizationIntraCloudByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(delete(AUTHORIZATION_INTRA_CLOUD_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of registerAuthorizationIntraCloud
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithInvalidConsumerId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(0L, createIdList(1, 1), createIdList(1, 2), createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithEmptyProviderIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, new ArrayList<>(), createIdList(1, 2), createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithNullProviderIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, null, createIdList(1, 2), createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithEmptyServiceDefinitionIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, createIdList(1, 2), new ArrayList<>(), createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithNullServiceDefinitionIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, createIdList(1, 2), null, createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithEmptyInterfaceIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, createIdList(1, 2), createIdList(1, 1), new ArrayList<>())))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithNullInterfaceIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, createIdList(1, 2), createIdList(1, 1), null)))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithMultipleElementsInProviderIdAndServiceDefinitionIdLists() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, createIdList(1, 2), createIdList(1, 2), createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationIntraCloudWithMultipleElementsInServiceDefinitionIdAndInterfaceIdLists() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO(1L, createIdList(1, 1), createIdList(1, 2), createIdList(1, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterAuthorizationIntraCloudDBCall() throws Exception {
		final Page<AuthorizationIntraCloud> entries = createPageForMockingAuthorizationDBService(1);
		when(authorizationDBService.createBulkAuthorizationIntraCloudResponse(anyLong(), any(), any(), any())).thenReturn(
																									DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(entries));
		
		final MvcResult response = this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudRequestDTO((long) 1, createIdList(1, 1), createIdList(1, 1), createIdList(1, 1))))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isCreated())
											   .andReturn();
		
		final AuthorizationIntraCloudListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationIntraCloudListResponseDTO.class);
		assertEquals("Consumer", responseBody.getData().get(0).getConsumerSystem().getSystemName());
		assertEquals(1, responseBody.getData().size());
		assertEquals(1, responseBody.getCount());
	}
	
	//=================================================================================================
	// Test of checkAuthorizationIntraCloudRequest
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationIntraCloudRequestWithInvalidConsumerId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudCheckRequestDTO(0L, 1L, createListOfIdIdLists(2, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationIntraCloudRequestWithInvalidServiceDefinitionId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudCheckRequestDTO(1L, null, createListOfIdIdLists(2, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationIntraCloudRequestWithEmptyProviderIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudCheckRequestDTO(1L, 2L, new ArrayList<>())))
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckAuthorizationIntraCloudRequestDBCall() throws Exception {
		final Long consumerId = 1L;
		final Long serviceDefinitionId = 3L;
		final int numberOfProviders = 2;
		final int numberOfInterfaces = 4;
		final List<IdIdListDTO> authorizedProviderIdsWithInterfaceLitsts =createListOfIdIdLists(numberOfProviders, numberOfInterfaces);
		when(authorizationDBService.checkAuthorizationIntraCloudRequest(anyLong(), anyLong(), any())).thenReturn(new AuthorizationIntraCloudCheckResponseDTO(consumerId, serviceDefinitionId,
																																							 authorizedProviderIdsWithInterfaceLitsts));
		
		final MvcResult response = this.mockMvc.perform(post(AUTHORIZATION_INTRA_CLOUD_CHECK_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(new AuthorizationIntraCloudCheckRequestDTO(consumerId, serviceDefinitionId, authorizedProviderIdsWithInterfaceLitsts)))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationIntraCloudCheckResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationIntraCloudCheckResponseDTO.class);
		assertEquals(numberOfProviders, responseBody.getAuthorizedProviderIdsWithInterfaceIds().size());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Page<AuthorizationIntraCloud> createPageForMockingAuthorizationDBService(final int numberOfRequestedEntry) {
		final List<AuthorizationIntraCloud> entries = new ArrayList<>(numberOfRequestedEntry);
		final System consumer = new System("Consumer", "0.0.0.0.", 1000, null);
		consumer.setId(1);
		
		for (int i = 1; i <= numberOfRequestedEntry; ++i) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService" + i);
			serviceDefinition.setId(i);
			final System provider = new System("Provider" + i, i + "." + i + "." + i + "." + i, i * 1000, null);
			provider.setId(i);
			final AuthorizationIntraCloud entry = new AuthorizationIntraCloud(consumer, provider, serviceDefinition);
			entry.setId(i);
			entries.add(entry);
		}
		
		return new PageImpl<AuthorizationIntraCloud>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Long> createIdList(final int firstNum, final int lastNum) {
		final List<Long> idList = new ArrayList<>(lastNum);
		for (int i = firstNum; i <= lastNum; ++i) {
			idList.add((long) i);
		}
		
		return idList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<IdIdListDTO> createListOfIdIdLists(final int numberOfIds, final int sizeOfIdLists) {
		final List<IdIdListDTO> ret = new ArrayList<>();
		for (long id = 1; id <= numberOfIds; ++id) {
			final List<Long> idList = new ArrayList<>();
			for (long j = 1; j <= sizeOfIdLists; ++j) {
				idList.add(j);
			}
			ret.add(new IdIdListDTO(id, idList));
		}
		return ret;
	}
}

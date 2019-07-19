package eu.arrowhead.core.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.AuthorizationInterCloud;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckRequestDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudListResponseDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudRequestDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.IdIdListDTO;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@ContextConfiguration(classes = AuthorizationDBServiceTestContext.class)
public class AuthorizationControllerInterCloudTest {
	
	//=================================================================================================
	// members
	
	private static final String AUTHORIZATION_INTER_CLOUD_MGMT_URI = "/authorization/mgmt/intercloud";
	private static final String AUTHORIZATION_INTER_CLOUD_CHECK_URI = "/authorization/intercloud/check";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockAuthorizationDBService")
	private AuthorizationDBService authorizationDBService;
	
	private static final ZonedDateTime zdTime = Utilities.parseUTCStringToLocalZonedDateTime("2222-12-12 12:00:00");
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//=================================================================================================
	// Tests of getAuthorizationInterClouds
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationInterCloudsWithoutParameters() throws Exception {
		final int numOfEntries = 4;
		final AuthorizationInterCloudListResponseDTO dto = DTOConverter.convertAuthorizationInterCloudListToAuthorizationInterCloudListResponseDTO(
																																			createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getAuthorizationInterCloudEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationInterCloudListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationInterCloudListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationInterCloudsWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 4;
		final AuthorizationInterCloudListResponseDTO dto = DTOConverter.convertAuthorizationInterCloudListToAuthorizationInterCloudListResponseDTO(
																																			createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getAuthorizationInterCloudEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
											   .param(CommonConstants.REQUEST_PARAM_PAGE, "0")
											   .param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(numOfEntries))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationInterCloudListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationInterCloudListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationInterCloudsWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
					.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(5))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationInterCloudsWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
					.param(CommonConstants.REQUEST_PARAM_PAGE, "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationInterCloudsWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
					.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationInterCloudsWithExistingId() throws Exception {
		final AuthorizationInterCloudResponseDTO dto = DTOConverter.convertAuthorizationInterCloudToAuthorizationInterCloudResponseDTO(createPageForMockingAuthorizationDBService(1).getContent().
																																	   get(0));
		when(authorizationDBService.getAuthorizationInterCloudEntryByIdResponse(anyLong())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI + "/1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationInterCloudResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationInterCloudResponseDTO.class);
		assertEquals("testCloudName", responseBody.getCloud().getName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testGetAuthorizationInterCloudsWithInvalidId() throws Exception {
		this.mockMvc.perform(get(AUTHORIZATION_INTER_CLOUD_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of removeAuthorizationInterCloudById
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRemoveAuthorizationInterCloudByIdWithExistingId() throws Exception {
		this.mockMvc.perform(delete(AUTHORIZATION_INTER_CLOUD_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRemoveAuthorizationInterCloudByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(delete(AUTHORIZATION_INTER_CLOUD_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of registerAuthorizationInterCloud
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationInterCloudWithInvalidCloudId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudRequestDTO(0L, createIdList(1, 2), createIdList(1, 1), createIdList(1, 1))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
		@SuppressWarnings("squid:S2699")
		@Test
		public void testRegisterAuthorizationInterCloudWithEmptyProviderIdList() throws Exception {
			this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudRequestDTO(1L, null, createIdList(1, 2), createIdList(1, 1))))
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());
		}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationInterCloudWithEmptyServiceDefinitionIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudRequestDTO(1L, createIdList(1, 2), null, createIdList(1, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testRegisterAuthorizationInterCloudWithEmptyInterfaceIdList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudRequestDTO(1L, createIdList(1, 1), createIdList(1, 1), null)))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterAuthorizationInterCloudDBCall() throws Exception {
		final Page<AuthorizationInterCloud> entries = createPageForMockingAuthorizationDBService(1);
		when(authorizationDBService.createBulkAuthorizationInterCloudResponse(anyLong(), any(), any(), any())).thenReturn(DTOConverter.convertAuthorizationInterCloudListToAuthorizationInterCloudListResponseDTO(entries));
		
		final MvcResult response = this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_MGMT_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudRequestDTO(1L, createIdList(1, 1), createIdList(1, 1), createIdList(1, 1))))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isCreated())
											   .andReturn();
		
		final AuthorizationInterCloudListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationInterCloudListResponseDTO.class);
		assertEquals("testCloudName", responseBody.getData().get(0).getCloud() .getName());
		assertEquals(1, responseBody.getData().size());
		assertEquals(1, responseBody.getCount());
	}
	
	//=================================================================================================
	// Test of checkAuthorizationInterCloudRequest
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationInterCloudRequestWithInvalidCloudId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(0L, 1L, createListOfIdIdLists(1, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	
	public void testCheckAuthorizationInterCloudRequestWithNullCloudId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(null, 1L, createListOfIdIdLists(1, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationInterCloudRequestWithInvalidServiceDefinitionId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(1L, 0L, createListOfIdIdLists(1, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationInterCloudRequestWithNullServiceDefinitionId() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(1L, null, createListOfIdIdLists(1, 2))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationInterCloudRequestWithInvalidProviderInterfacesList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(1L, 1L, createListOfIdIdLists(0, 0))))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testCheckAuthorizationInterCloudRequestWithNullProviderInterfacesList() throws Exception {
		this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(1L, 1L, null)))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckAuthorizationInterCloudRequestDBCall() throws Exception {
		final long cloudId = 1L;
		final long serviceDefinitionId =  3L;
		final long providerId = 1;
		when(authorizationDBService.checkAuthorizationInterCloudResponse(anyLong(), anyLong(), any())).thenReturn(new AuthorizationInterCloudCheckResponseDTO(cloudId, serviceDefinitionId, createListOfIdIdLists((int) providerId, 2)));
		
		final MvcResult response = this.mockMvc.perform(post(AUTHORIZATION_INTER_CLOUD_CHECK_URI)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(new AuthorizationInterCloudCheckRequestDTO(cloudId, serviceDefinitionId, createListOfIdIdLists((int) providerId, 2))))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final AuthorizationInterCloudCheckResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), AuthorizationInterCloudCheckResponseDTO.class);
		assertEquals(providerId, (long) responseBody.getAuthorizedProviderIdsWithInterfaceIds().get(0).getId());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<Long> createIdList(final int firstNum, final int lastNum) {
		final List<Long> idList = new ArrayList<>(lastNum);
		for (int i = firstNum; i <= lastNum; ++i) {
			idList.add((long) i);
		}
		
		return idList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Page<AuthorizationInterCloud> createPageForMockingAuthorizationDBService(final int numberOfRequestedEntry) {
		final List<AuthorizationInterCloud> entries = new ArrayList<>(numberOfRequestedEntry);
		final Cloud cloud = getValidTestCloud();
		final System provider = new System("testSystem", "testAddr", 2000, "TOKEN");
		for (int i = 1; i <= numberOfRequestedEntry; ++i) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService"+i);
			serviceDefinition.setId(i);
			serviceDefinition.setCreatedAt(zdTime);
			serviceDefinition.setUpdatedAt(zdTime);
			final AuthorizationInterCloud entry = new AuthorizationInterCloud(cloud, provider, serviceDefinition);
			entry.setId(i);
			entry.setInterfaceConnections(new HashSet<>());
			entry.setCreatedAt(zdTime);
			entry.setUpdatedAt(zdTime);
			entries.add(entry);
		}
		
		return new PageImpl<>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Cloud getValidTestCloud() {
		final int port = 1234;
		final boolean secure = true;
		final boolean neighbor = false;
		final boolean ownCloud = true;
		
		final Cloud cloud = new Cloud("testOperator", "testCloudName", "testcloudAddress", port, "testGatekeeperServiceUri", "testAuthenticationInfo", secure, neighbor, ownCloud);
		cloud.setId(1);
		cloud.setCreatedAt(zdTime);
		cloud.setUpdatedAt(zdTime);

		return cloud;
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
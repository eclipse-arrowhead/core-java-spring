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

import java.time.ZonedDateTime;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.InterCloudAuthorization;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.InterCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.InterCloudAuthorizationRequestDTO;
import eu.arrowhead.common.dto.InterCloudAuthorizationResponseDTO;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@ContextConfiguration(classes = AuthorizationDBServiceTestContext.class)
public class AuthorizationControllerInterCloudTest {
	
	//=================================================================================================
	// members
	
	private static final String INTER_CLOUD_AUTHORIZATION_MGMT_URI = "/authorization/mgmt/intercloud";
	
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
	
	//-------------------------------------------------------------------------------------------------
	// Tests of getInterCloudAuthorizations
	
	@Test
	public void testGetInterCloudAuthorizationsWithoutParameters() throws Exception {
		final int numOfEntries = 4;
		final InterCloudAuthorizationListResponseDTO dto = DTOConverter.convertInterCloudAuthorizationListToInterCloudAuthorizationListResponseDTO(createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getInterCloudAuthorizationEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final InterCloudAuthorizationListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), InterCloudAuthorizationListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterCloudAuthorizationsWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 4;
		final InterCloudAuthorizationListResponseDTO dto = DTOConverter.convertInterCloudAuthorizationListToInterCloudAuthorizationListResponseDTO(createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getInterCloudAuthorizationEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_PAGE, "0")
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(numOfEntries))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final InterCloudAuthorizationListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), InterCloudAuthorizationListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterCloudAuthorizationsWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(5))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterCloudAuthorizationsWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_PAGE, "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterCloudAuthorizationsWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of getInterCloudAuthorizations
	
	@Test
	public void testGetInterCloudAuthorizationsWithExistingId() throws Exception {
		final InterCloudAuthorizationResponseDTO dto = DTOConverter.convertInterCloudAuthorizationToInterCloudAuthorizationResponseDTO(createPageForMockingAuthorizationDBService(1).getContent().get(0));
		when(authorizationDBService.getInterCloudAuthorizationEntryByIdResponse(anyLong())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI + "/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final InterCloudAuthorizationResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), InterCloudAuthorizationResponseDTO.class);
		assertEquals(responseBody.getCloud().getName(), "testCloudName");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterCloudAuthorizationsWithInvalidId() throws Exception {
		this.mockMvc.perform(get(INTER_CLOUD_AUTHORIZATION_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of removeInterCloudAuthorizationById
	
	@Test
	public void testRemoveInterCloudAuthorizationByIdWithExistingId() throws Exception {
		this.mockMvc.perform(delete(INTER_CLOUD_AUTHORIZATION_MGMT_URI + "/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveInterCloudAuthorizationByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(delete(INTER_CLOUD_AUTHORIZATION_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of registerInterCloudAuthorization
	
	@Test
	public void testRegisterInterCloudAuthorizationWithInvalidCloudId() throws Exception {
		this.mockMvc.perform(post(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsBytes(new InterCloudAuthorizationRequestDTO(0L, createIdList(1, 2))))
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterInterCloudAuthorizationWithEmptyServiceDefinitionIdList() throws Exception {
		this.mockMvc.perform(post(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsBytes(new InterCloudAuthorizationRequestDTO(1L, null)))
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterInterCloudAuthorizationDBCall() throws Exception {
		final Page<InterCloudAuthorization> entries = createPageForMockingAuthorizationDBService(1);
		when(authorizationDBService.createInterCloudAuthorizationResponse(anyLong(),any())).thenReturn(DTOConverter.convertInterCloudAuthorizationListToInterCloudAuthorizationListResponseDTO(entries));
		
		final MvcResult response = this.mockMvc.perform(post(INTER_CLOUD_AUTHORIZATION_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new InterCloudAuthorizationRequestDTO(1L,createIdList(1, 1))))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn();
		
		final InterCloudAuthorizationListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), InterCloudAuthorizationListResponseDTO.class);
		assertEquals("testCloudName", responseBody.getData().get(0).getCloud() .getName());
		assertEquals(1, responseBody.getData().size());
		assertEquals(1, responseBody.getCount());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<Long> createIdList(final int firstNum, final int lastNum) {
		final List<Long> idList = new ArrayList<>(lastNum);
		for (int i = firstNum; i <= lastNum; i++) {
			idList.add((long) i);
		}
		return idList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Page<InterCloudAuthorization> createPageForMockingAuthorizationDBService(final int numberOfRequestedEntry) {
		final List<InterCloudAuthorization> entries = new ArrayList<>(numberOfRequestedEntry);
		final Cloud cloud = getValidTestCloud();
		for (int i = 1; i <= numberOfRequestedEntry; i++ ) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService"+i);
			serviceDefinition.setId(i);
			serviceDefinition.setCreatedAt(zdTime);
			serviceDefinition.setUpdatedAt(zdTime);
			final InterCloudAuthorization entry = new InterCloudAuthorization(cloud, serviceDefinition);
			entry.setId(i);
			entry.setCreatedAt(zdTime);
			entry.setUpdatedAt(zdTime);
			entries.add(entry);
		}
		return new PageImpl<InterCloudAuthorization>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Cloud getValidTestCloud() {
		
		final int port = 1234;
		final boolean secure = true;
		final boolean neighbor = true;
		final boolean ownCloud = true;
		
		final Cloud cloud = new Cloud(
				"testOperator",
				"testCloudName",
				"testcloudAddress",
				port,
				"testGatekeeperServiceUri",
				"testAuthenticationInfo", 
				 secure,
				 neighbor,
				 ownCloud
				);
		
		cloud.setId(1);
		cloud.setCreatedAt(zdTime);
		cloud.setUpdatedAt(zdTime);

		return cloud;
	}	

}

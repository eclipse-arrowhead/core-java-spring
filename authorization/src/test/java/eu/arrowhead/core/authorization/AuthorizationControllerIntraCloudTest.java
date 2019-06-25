package eu.arrowhead.core.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
import eu.arrowhead.common.database.entity.IntraCloudAuthorization;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationResponseDTO;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationMain.class)
@ContextConfiguration(classes = AuthorizationDBServiceTestContext.class)
public class AuthorizationControllerIntraCloudTest {
	
	//=================================================================================================
	// members
	
	private static final String INTRA_CLOUD_AUTHORIZATION_MGMT_URI = "/authorization/mgmt/intracloud";
	private static final String INTRA_CLOUD_AUTHORIZATION_CHECK_URI = "/authorization/intracloud/check";
	
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
	
	//-------------------------------------------------------------------------------------------------
	// Tests of getIntraCloudAuthorizations
	
	@Test
	public void testGetIntraCloudAuthorizationsWithoutParameters() throws Exception {
		final int numOfEntries = 4;
		final IntraCloudAuthorizationListResponseDTO dto = DTOConverter.convertIntraCloudAuthorizationListToIntraCloudAuthorizationListResponseDTO(createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getIntraCloudAuthorizationEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final IntraCloudAuthorizationListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), IntraCloudAuthorizationListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraCloudAuthorizationsWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 4;
		final IntraCloudAuthorizationListResponseDTO dto = DTOConverter.convertIntraCloudAuthorizationListToIntraCloudAuthorizationListResponseDTO(createPageForMockingAuthorizationDBService(numOfEntries));
		when(authorizationDBService.getIntraCloudAuthorizationEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_PAGE, "0")
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(numOfEntries))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final IntraCloudAuthorizationListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), IntraCloudAuthorizationListResponseDTO.class);
		assertEquals(numOfEntries, responseBody.getData().size());
		assertEquals(numOfEntries, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraCloudAuthorizationsWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, String.valueOf(5))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraCloudAuthorizationsWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_PAGE, "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraCloudAuthorizationsWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
				.param(CommonConstants.REQUEST_PARAM_DIRECTION, "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of getIntraCloudAuthorizations
	
	@Test
	public void testGetIntraCloudAuthorizationsWithExistingId() throws Exception {
		final IntraCloudAuthorizationResponseDTO dto = DTOConverter.convertIntraCloudAuthorizationToIntraCloudAuthorizationResponseDTO(createPageForMockingAuthorizationDBService(1).getContent().get(0));
		when(authorizationDBService.getIntraCloudAuthorizationEntryByIdResponse(anyLong())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI + "/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final IntraCloudAuthorizationResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), IntraCloudAuthorizationResponseDTO.class);
		assertEquals(responseBody.getConsumerSystem().getSystemName(), "Consumer");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraCloudAuthorizationsWithInvalidId() throws Exception {
		this.mockMvc.perform(get(INTRA_CLOUD_AUTHORIZATION_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of removeIntraCloudAuthorizationById
	
	@Test
	public void testRemoveIntraCloudAuthorizationByIdWithExistingId() throws Exception {
		this.mockMvc.perform(delete(INTRA_CLOUD_AUTHORIZATION_MGMT_URI + "/1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveIntraCloudAuthorizationByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(delete(INTRA_CLOUD_AUTHORIZATION_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of registerIntraCloudAuthorization
	
	@Test
	public void testRegisterIntraCloudAuthorizationWithInvalidConsumerId() throws Exception {
		this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationRequestDTO((long) 0, createIdList(1, 2), createIdList(1, 2))))
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterIntraCloudAuthorizationWithEmptyProviderIdList() throws Exception {
		this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationRequestDTO((long) 1, new ArrayList<>(), createIdList(1, 2))))
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterIntraCloudAuthorizationWithEmptyServiceDefinitionIdList() throws Exception {
		this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationRequestDTO((long) 1, createIdList(1, 2), null)))
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterIntraCloudAuthorizationDBCall() throws Exception {
		final Page<IntraCloudAuthorization> entries = createPageForMockingAuthorizationDBService(1);
		when(authorizationDBService.createBulkIntraCloudAuthorizationResponse(anyLong(), any(), any())).thenReturn(DTOConverter.convertIntraCloudAuthorizationListToIntraCloudAuthorizationListResponseDTO(entries));
		
		final MvcResult response = this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationRequestDTO((long) 1, createIdList(1, 1), createIdList(1, 1))))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn();
		
		final IntraCloudAuthorizationListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), IntraCloudAuthorizationListResponseDTO.class);
		assertEquals("Consumer", responseBody.getData().get(0).getConsumerSystem().getSystemName());
		assertEquals(1, responseBody.getData().size());
		assertEquals(1, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test of checkIntraCloudAuthorizationRequest
	
	@Test
	public void testCheckIntraCloudAuthorizationRequestWithInvalidConsumerId() throws Exception {
		this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_CHECK_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationCheckRequestDTO((long) 0, (long) 1, createIdList(1, 2))))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckIntraCloudAuthorizationRequestWithInvalidServiceDefinitionId() throws Exception {
		this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_CHECK_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationCheckRequestDTO((long) 1, null, createIdList(1, 2))))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckIntraCloudAuthorizationRequestWithEmptyProviderIdList() throws Exception {
		this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_CHECK_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationCheckRequestDTO((long) 1, (long) 2, new ArrayList<>())))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckIntraCloudAuthorizationRequestDBCall() throws Exception {
		final Long consumerId = (long) 1;
		final Long serviceDefinitionId = (long) 3;
		final int providerIdA = 4;
		final int providerIdB = 5;
		final Map<Long, Boolean> providerIdAuthorizationState = new HashMap<>();
		providerIdAuthorizationState.put((long) providerIdA, true);
		providerIdAuthorizationState.put((long) providerIdB, false);
		when(authorizationDBService.checkIntraCloudAuthorizationRequestResponse(anyLong(), anyLong(), any()))
			.thenReturn(new IntraCloudAuthorizationCheckResponseDTO(consumerId, serviceDefinitionId, providerIdAuthorizationState));
		
		final MvcResult response = this.mockMvc.perform(post(INTRA_CLOUD_AUTHORIZATION_CHECK_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new IntraCloudAuthorizationCheckRequestDTO(consumerId, serviceDefinitionId, createIdList(providerIdA, providerIdB))))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final IntraCloudAuthorizationCheckResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), IntraCloudAuthorizationCheckResponseDTO.class);
		assertTrue(responseBody.getProviderIdAuthorizationState().get((long) providerIdA));
		assertFalse(responseBody.getProviderIdAuthorizationState().get((long) providerIdB));
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Page<IntraCloudAuthorization> createPageForMockingAuthorizationDBService(final int numberOfRequestedEntry) {
		final List<IntraCloudAuthorization> entries = new ArrayList<>(numberOfRequestedEntry);
		final System consumer = new System("Consumer", "0.0.0.0.", 1000, null);
		consumer.setId(1);
		for (int i = 1; i <= numberOfRequestedEntry; i++ ) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService" + i);
			serviceDefinition.setId(i);
			final System provider = new System("Provider" + i, i + "." + i +"." + i + "." + i, i * 1000, null);
			provider.setId(i);
			final IntraCloudAuthorization entry = new IntraCloudAuthorization(consumer, provider, serviceDefinition);
			entry.setId(i);
			entries.add(entry);
		}
		return new PageImpl<IntraCloudAuthorization>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Long> createIdList(final int firstNum, final int lastNum) {
		final List<Long> idList = new ArrayList<>(lastNum);
		for (int i = firstNum; i <= lastNum; i++) {
			idList.add((long) i);
		}
		return idList;
	}
}

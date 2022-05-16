/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudRelayFormDTO;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.QoSBestRelayRequestDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.RelayTestService;
import eu.arrowhead.core.qos.service.event.EventWatcherService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QoSMonitorMain.class)
@ContextConfiguration(classes = { QoSMonitorTestContext.class })
public class QoSMonitorControllerTest {

	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI =  CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT;
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI = QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_URI = CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/{" + PATH_VARIABLE_ID + "}";
	private static final String QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT;
	private static final String QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT + "/pair_results";
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT;
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT + "/pair_results";
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT + "/best_relay";
	private static final String QOSMONITOR_PUBLIC_KEY_URI = CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_KEY_URI;
	private static final String QOSMONITOR_INIT_RELAY_TEST_URI = CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INIT_RELAY_TEST_URI;
	private static final String QOSMONITOR_JOIN_RELAY_TEST_URI = CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_JOIN_RELAY_TEST_URI;

	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	private static final String PAGE_OR_SIZE_ERROR_MESSAGE = "Defined page or size could not be with undefined size or page.";

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockQoSDBService") 
	private QoSDBService qoSDBService;
	
	@MockBean(name = "mockPingService")
	private PingService pingService;
	
	@MockBean(name = "mockRelayTestService")
	private RelayTestService relayTestService;

	@MockBean(name = "mockEventWatcherService")
	private EventWatcherService eventWatcherService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void echoTest() throws Exception {
		this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.ECHO_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}

	//=================================================================================================
	// Test of getIntraPingMeasurements

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementsWithoutParametersTest() throws Exception {
		final QoSIntraPingMeasurementListResponseDTO pingMeasurementListResponseDTO = getIntraPingMeasurementListResponseDTOForTest();

		when(qoSDBService.getIntraPingMeasurementResponse( anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final QoSIntraPingMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementListResponseDTO.class);
		assertEquals(pingMeasurementListResponseDTO.getData().size(), responseBody.getData().size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementsWithPageAndSizeParametersTest() throws Exception {
		final int responseSize = 3;
		final QoSIntraPingMeasurementListResponseDTO pingMeasurementListResponseDTO = getIntraPingMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getIntraPingMeasurementResponse( anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI)
				.param("page", "0")
				.param("item_per_page", String.valueOf(responseSize))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final QoSIntraPingMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementListResponseDTO.class);
		assertEquals(pingMeasurementListResponseDTO.getData().size(), responseBody.getData().size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementsWithNullPageButDefinedSizeParameterTest() throws Exception {
		final int responseSize = 3;
		final QoSIntraPingMeasurementListResponseDTO pingMeasurementListResponseDTO = getIntraPingMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getIntraPingMeasurementResponse( anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI)
				.param("item_per_page", String.valueOf(responseSize))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertTrue(responseBody.getErrorMessage().contains(PAGE_OR_SIZE_ERROR_MESSAGE));
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI, responseBody.getOrigin());
	}

	//=================================================================================================
	// Test of getManagementIntraPingMeasurementBySystemId

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementIntraPingMeasurementBySystemIdTest() throws Exception {
		final int requestedId = 1;
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = getIntraPingMeasurementResponseDTOForTest();

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final QoSIntraPingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementResponseDTO.class);
		assertEquals(requestedId, responseBody.getMeasurement().getSystem().getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementIntraPingMeasurementBySystemIdInvalidSystemIdTest() throws Exception {
		final int requestedId = 0;
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = getIntraPingMeasurementResponseDTOForTest();

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(ID_NOT_VALID_ERROR_MESSAGE, responseBody.getErrorMessage());
		assertEquals(CommonConstants.QOSMONITOR_URI + GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI, responseBody.getOrigin());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementIntraPingMeasurementBySystemIdDBExceptionTest() throws Exception {
		final int requestedId = 1;

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenThrow(new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.ARROWHEAD, responseBody.getExceptionType());
		assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, responseBody.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementIntraPingMeasurementBySystemIdNoMeasurementInDBTest() throws Exception {
		final int requestedId = 1;
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = new QoSIntraPingMeasurementResponseDTO();
		 pingMeasurementResponseDTO.setId( null );

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final QoSIntraPingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementResponseDTO.class);
		assertNull(responseBody.getId());
	}

	//=================================================================================================
	// Test of getIntraPingMeasurementBySystemIdResponse

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementBySystemIdTest() throws Exception {
		final int requestedId = 1;
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = getIntraPingMeasurementResponseDTOForTest();

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final QoSIntraPingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementResponseDTO.class);
		assertEquals(requestedId, responseBody.getMeasurement().getSystem().getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementBySystemIdInvalidSystemIdTest() throws Exception {
		final int requestedId = 0;
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = getIntraPingMeasurementResponseDTOForTest();

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(ID_NOT_VALID_ERROR_MESSAGE, responseBody.getErrorMessage());
		assertEquals(CommonConstants.QOSMONITOR_URI + GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_URI, responseBody.getOrigin());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementBySystemIdDBExceptionTest() throws Exception {
		final int requestedId = 1;

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenThrow(new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.ARROWHEAD, responseBody.getExceptionType());
		assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, responseBody.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMeasurementBySystemIdNoMeasurementInDBTest() throws Exception {
		final int requestedId = 1;
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO = new QoSIntraPingMeasurementResponseDTO();
		 pingMeasurementResponseDTO.setId( null );

		when(qoSDBService.getIntraPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final QoSIntraPingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementResponseDTO.class);
		assertNull(responseBody.getId());
	}
	
	//=================================================================================================
	// Test of getIntraPingMedianMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getIntraPingMedianMeasurementTest() throws Exception {
		final QoSIntraPingMeasurementResponseDTO responseDTO = getIntraPingMeasurementResponseDTOForTest();		
		when(pingService.getMedianIntraPingMeasurement(eq(QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT))).thenReturn(responseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT.replace("{attribute}", QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT.name()))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final QoSIntraPingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSIntraPingMeasurementResponseDTO.class);
		
		assertEquals(responseDTO.getId(), responseBody.getId());
	}
	
	//=================================================================================================
	// Test of getMgmtInterDirectPingMeasurements

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementsWithoutParametersTest() throws Exception {
		final QoSInterDirectPingMeasurementListResponseDTO pingMeasurementListResponseDTO = getInterDirectPingMeasurementListResponseDTOForTest(3);

		when(qoSDBService.getInterDirectPingMeasurementsPageResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();

		final QoSInterDirectPingMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterDirectPingMeasurementListResponseDTO.class);
		assertEquals(pingMeasurementListResponseDTO.getData().size(), responseBody.getData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementsWithPageAndSizeParametersTest() throws Exception {
		final int responseSize = 3;
		final QoSInterDirectPingMeasurementListResponseDTO pingMeasurementListResponseDTO = getInterDirectPingMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getInterDirectPingMeasurementsPageResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI)
											   .param("page", "0")
											   .param("item_per_page", String.valueOf(responseSize))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();

		final QoSInterDirectPingMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterDirectPingMeasurementListResponseDTO.class);
		assertEquals(pingMeasurementListResponseDTO.getData().size(), responseBody.getData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementsWithNullPageButDefinedSizeParameterTest() throws Exception {
		final int responseSize = 3;
		final QoSInterDirectPingMeasurementListResponseDTO pingMeasurementListResponseDTO = getInterDirectPingMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getInterDirectPingMeasurementsPageResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI)
									   .param("item_per_page", String.valueOf(responseSize))
									   .accept(MediaType.APPLICATION_JSON))
									   .andExpect(status().isBadRequest())
									   .andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertTrue(responseBody.getErrorMessage().contains(PAGE_OR_SIZE_ERROR_MESSAGE));
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI, responseBody.getOrigin());
	}
	
	//=================================================================================================
	// Test of getMgmtInterDirectPingMeasurementByCloudAndSystem
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementByCloudAndSystemTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final SystemResponseDTO system = new SystemResponseDTO(1L, "test-sys", "1.1.1.1", 10000, "dlaswefg", null, null, null);
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO(cloud, system);
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = getInterDirectPingMeasurementResponseDTOForTest();
		when(qoSDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isOk())
									    	   .andReturn();
		
		final QoSInterDirectPingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterDirectPingMeasurementResponseDTO.class);
		assertEquals(responseDTO.getId(), responseBody.getId());
		assertEquals(responseDTO.getMaxResponseTime(), responseBody.getMaxResponseTime());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementByCloudAndSystemWithNullCloudTest() throws Exception {
		final SystemResponseDTO system = new SystemResponseDTO(1L, "test-sys", "1.1.1.1", 10000, "dlaswefg", null, null, null);
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO(null, system);
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = getInterDirectPingMeasurementResponseDTOForTest();
		when(qoSDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementByCloudAndSystemWithNullSystemTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO(cloud, null);
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = getInterDirectPingMeasurementResponseDTOForTest();
		when(qoSDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementByCloudAndSystemWithNullCloudOperatorTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, null, "test-n", true, true, false, "fddddbvf", null, null);
		final SystemResponseDTO system = new SystemResponseDTO(1L, "test-sys", "1.1.1.1", 10000, "dlaswefg", null, null, null);
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO(cloud, system);
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = getInterDirectPingMeasurementResponseDTOForTest();
		when(qoSDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementByCloudAndSystemWithNullCloudNameTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", null, true, true, false, "fddddbvf", null, null);
		final SystemResponseDTO system = new SystemResponseDTO(1L, "test-sys", "1.1.1.1", 10000, "dlaswefg", null, null, null);
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO(cloud, system);
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = getInterDirectPingMeasurementResponseDTOForTest();
		when(qoSDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterDirectPingMeasurementByCloudAndSystemWithNullSystemAddressTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final SystemResponseDTO system = new SystemResponseDTO(1L, "test-sys", null, 10000, "dlaswefg", null, null, null);
		final CloudSystemFormDTO requestDTO = new CloudSystemFormDTO(cloud, system);
		
		final QoSInterDirectPingMeasurementResponseDTO responseDTO = getInterDirectPingMeasurementResponseDTOForTest();
		when(qoSDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_BY_CLOUD_AND_SYSTEM_MGMT_URI, responseBody.getOrigin());		
	}
	
	// getInterDirectPingMeasurementByCloudAndSystem use the same methods as getMgmtInterDirectPingMeasurementByCloudAndSystem so we skip it
	
	//=================================================================================================
	// Test of getMgmtInterRelayEchoMeasurements
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementsWithoutParametersTest() throws Exception {
		final QoSInterRelayEchoMeasurementListResponseDTO responseDTO = getInterRelayEchoMeasurementListResponseDTOForTest(3);

		when(qoSDBService.getInterRelayEchoMeasurementsResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(responseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();

		final QoSInterRelayEchoMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterRelayEchoMeasurementListResponseDTO.class);
		assertEquals(responseDTO.getData().size(), responseBody.getData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementsWithPageAndSizeParametersTest() throws Exception {
		final int responseSize = 3;
		final QoSInterRelayEchoMeasurementListResponseDTO responseDTO = getInterRelayEchoMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getInterRelayEchoMeasurementsResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(responseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI)
											   .param("page", "0")
											   .param("item_per_page", String.valueOf(responseSize))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();

		final QoSInterRelayEchoMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterRelayEchoMeasurementListResponseDTO.class);
		assertEquals(responseDTO.getData().size(), responseBody.getData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementsWithNullPageButDefinedSizeParameterTest() throws Exception {
		final int responseSize = 3;
		final QoSInterRelayEchoMeasurementListResponseDTO responseDTO = getInterRelayEchoMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getInterRelayEchoMeasurementsResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(responseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI)
									   .param("item_per_page", String.valueOf(responseSize))
									   .accept(MediaType.APPLICATION_JSON))
									   .andExpect(status().isBadRequest())
									   .andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertTrue(responseBody.getErrorMessage().contains(PAGE_OR_SIZE_ERROR_MESSAGE));
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI, responseBody.getOrigin());
	}
	
	//=================================================================================================
	// Test of queryMgmtInterRelayEchoMeasurementByCloudAndRelay
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementByCloudAndRelayTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final RelayResponseDTO relay = new RelayResponseDTO(1L, "2.2.2.2", 20000, null, true, false, RelayType.GATEWAY_RELAY, null, null);
		final CloudRelayFormDTO requestDTO = new CloudRelayFormDTO(cloud, relay);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isOk())
									    	   .andReturn();
		
		final QoSInterRelayEchoMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterRelayEchoMeasurementResponseDTO.class);
		assertEquals(responseDTO.getId(), responseBody.getId());
		assertEquals(responseDTO.getMaxResponseTime(), responseBody.getMaxResponseTime());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementByCloudAndRelayWithNullCloudTest() throws Exception {
		final RelayResponseDTO relay = new RelayResponseDTO(1L, "2.2.2.2", 20000, null, true, false, RelayType.GATEWAY_RELAY, null, null);
		final CloudRelayFormDTO requestDTO = new CloudRelayFormDTO(null, relay);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementByCloudAndRelayWithNullRelayTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final CloudRelayFormDTO requestDTO = new CloudRelayFormDTO(cloud, null);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementByCloudAndRelayWithNullCloudOperatorTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, null, "test-n", true, true, false, "fddddbvf", null, null);
		final RelayResponseDTO relay = new RelayResponseDTO(1L, "2.2.2.2", 20000, null, true, false, RelayType.GATEWAY_RELAY, null, null);
		final CloudRelayFormDTO requestDTO = new CloudRelayFormDTO(cloud, relay);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementByCloudAndRelayWithNullCloudNameTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", null, true, true, false, "fddddbvf", null, null);
		final RelayResponseDTO relay = new RelayResponseDTO(1L, "2.2.2.2", 20000, null, true, false, RelayType.GATEWAY_RELAY, null, null);
		final CloudRelayFormDTO requestDTO = new CloudRelayFormDTO(cloud, relay);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI, responseBody.getOrigin());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getMgmtInterRelayEchoMeasurementByCloudAndRelayWithNullRelayAddressTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final RelayResponseDTO relay = new RelayResponseDTO(1L, null, 20000, null, true, false, RelayType.GATEWAY_RELAY, null, null);
		final CloudRelayFormDTO requestDTO = new CloudRelayFormDTO(cloud, relay);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_URI, responseBody.getOrigin());		
	}
	
	//=================================================================================================
	// Test of queryMgmtBestInterRelayEchoMeasurementByCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryMgmtBestInterRelayEchoMeasurementByCloudTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final QoSBestRelayRequestDTO requestDTO = new QoSBestRelayRequestDTO(cloud, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT.name());
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getBestInterRelayEchoMeasurementByCloudAndAttributeResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isOk())
									    	   .andReturn();
		
		final QoSInterRelayEchoMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterRelayEchoMeasurementResponseDTO.class);
		assertEquals(responseDTO.getId(), responseBody.getId());
		assertEquals(responseDTO.getMaxResponseTime(), responseBody.getMaxResponseTime());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryMgmtBestInterRelayEchoMeasurementByCloudWithNullCloudTest() throws Exception {
		final QoSBestRelayRequestDTO requestDTO = new QoSBestRelayRequestDTO(null, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT.name());
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getBestInterRelayEchoMeasurementByCloudAndAttributeResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI, responseBody.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryMgmtBestInterRelayEchoMeasurementByCloudWithNullCloudOperatorTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, null, "test-n", true, true, false, "fddddbvf", null, null);
		final QoSBestRelayRequestDTO requestDTO = new QoSBestRelayRequestDTO(cloud, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT.name());
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getBestInterRelayEchoMeasurementByCloudAndAttributeResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI, responseBody.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryMgmtBestInterRelayEchoMeasurementByCloudWithNullCloudNameTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", null, true, true, false, "fddddbvf", null, null);
		final QoSBestRelayRequestDTO requestDTO = new QoSBestRelayRequestDTO(cloud, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT.name());
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getBestInterRelayEchoMeasurementByCloudAndAttributeResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI, responseBody.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryMgmtBestInterRelayEchoMeasurementByCloudWithNullAttributeTest() throws Exception {
		final CloudResponseDTO cloud = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null);
		final QoSBestRelayRequestDTO requestDTO = new QoSBestRelayRequestDTO(cloud, null);
		
		final QoSInterRelayEchoMeasurementResponseDTO responseDTO = getInterRelayEchoMeasurementResponseDTOForTest();
		when(qoSDBService.getBestInterRelayEchoMeasurementByCloudAndAttributeResponse(any(), any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_URI, responseBody.getOrigin());
	}
	
	//=================================================================================================
	// Test of queryInterRelayEchoMeasurementByCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryInterRelayEchoMeasurementByCloudTest() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setOperator("test-op");
		requestDTO.setName("test-n");
		
		final QoSInterRelayEchoMeasurementListResponseDTO responseDTO = getInterRelayEchoMeasurementListResponseDTOForTest(3);
		when(relayTestService.getInterRelayEchoMeasurements(any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isOk())
									    	   .andReturn();
		
		final QoSInterRelayEchoMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), QoSInterRelayEchoMeasurementListResponseDTO.class);
		assertEquals(responseDTO.getData().size(), responseBody.getData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryInterRelayEchoMeasurementByCloudWithNullCloudOperatorTest() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setOperator(null);
		requestDTO.setName("test-n");
		
		final QoSInterRelayEchoMeasurementListResponseDTO responseDTO = getInterRelayEchoMeasurementListResponseDTOForTest(3);
		when(relayTestService.getInterRelayEchoMeasurements(any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT, responseBody.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void queryInterRelayEchoMeasurementByCloudWithNullCloudNameTest() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setOperator("test-op");
		requestDTO.setName(null);
		
		final QoSInterRelayEchoMeasurementListResponseDTO responseDTO = getInterRelayEchoMeasurementListResponseDTOForTest(3);
		when(relayTestService.getInterRelayEchoMeasurements(any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT)
									     	   .contentType(MediaType.APPLICATION_JSON)
									     	   .content(objectMapper.writeValueAsBytes(requestDTO))
									     	   .accept(MediaType.APPLICATION_JSON))
									    	   .andExpect(status().isBadRequest())
									    	   .andReturn();
		
		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT, responseBody.getOrigin());
	}
	
	//=================================================================================================
	// Test of getPublicKey
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPublicKeyNotSecure() throws Exception {
		assumeFalse(secure);
		
		final MvcResult result = getPublicKey(status().isInternalServerError());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.ARROWHEAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_PUBLIC_KEY_URI, error.getOrigin());
		Assert.assertEquals("QoS Monitor core service runs in insecure mode.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPublicKeyNotAvailable() throws Exception {
		assumeTrue(secure);

		final Object publicKey = arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		try {
			arrowheadContext.remove(CommonConstants.SERVER_PUBLIC_KEY);
			final MvcResult result = getPublicKey(status().isInternalServerError());
			final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
			
			Assert.assertEquals(ExceptionType.ARROWHEAD, error.getExceptionType());
			Assert.assertEquals(QOSMONITOR_PUBLIC_KEY_URI, error.getOrigin());
			Assert.assertEquals("Public key is not available.", error.getErrorMessage());
		} finally {
			arrowheadContext.put(CommonConstants.SERVER_PUBLIC_KEY, publicKey);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testGetPublicKeyOk() throws Exception {
		assumeTrue(secure);
		
		getPublicKey(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudNull() throws Exception {
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Cloud is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudOperatorNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudOperatorEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudNameNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestTargetCloudNameEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or empty", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayAddressNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayAddressBlank() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayPortNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay port is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayPortTooLow() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(-2);
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertTrue(error.getErrorMessage().contains("Relay port must be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayPortTooHigh() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(200000);
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertTrue(error.getErrorMessage().contains("Relay port must be between"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayTypeNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayTypeBlank() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay type is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayTypeInvalid() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("invalid");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay type is invalid", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestRelayTypeGatekeeper() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEKEEPER_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Relay type is invalid", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestQueueIdNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Queue id is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestQueueIdEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId(" ");
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Queue id is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestPeerNameNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Peer name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestPeerNameEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName(" ");
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Peer name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestReceiverPublicKeyNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Receiver QoS Monitor's public key is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestReceiverPublicKeyBlank() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey(" ");
		
		final MvcResult result = postInitTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_INIT_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Receiver QoS Monitor's public key is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestOk() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		doNothing().when(relayTestService).initRelayTest(any(QoSMonitorSenderConnectionRequestDTO.class));
		
		postInitTestRelayTest(request, status().isCreated());

		verify(relayTestService, times(1)).initRelayTest(any(QoSMonitorSenderConnectionRequestDTO.class));
	}
	
	// skip cloud test because joinRelayTest() uses the same method for that than initRelayTest() does
	// skip relay test because joinRelayTest() uses the same method for that than initRelayTest() does
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testJoinRelayTestSenderPublicKeyNull() throws Exception {
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		
		final MvcResult result = postJoinTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);

		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_JOIN_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Sender QoS Monitor's public key is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testJoinRelayTestSenderPublicKeyEmpty() throws Exception {
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("");
		
		final MvcResult result = postJoinTestRelayTest(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);

		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(QOSMONITOR_JOIN_RELAY_TEST_URI, error.getOrigin());
		Assert.assertEquals("Sender QoS Monitor's public key is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testJoinRelayTestOk() throws Exception {
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid key");
		
		when(relayTestService.joinRelayTest(any(QoSRelayTestProposalRequestDTO.class))).thenReturn(new QoSRelayTestProposalResponseDTO("queueId", "peerName", "receiverKey"));
		
		final MvcResult result = postJoinTestRelayTest(request, status().isCreated());
		final QoSRelayTestProposalResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), QoSRelayTestProposalResponseDTO.class);
		
		verify(relayTestService, times(1)).joinRelayTest(any(QoSRelayTestProposalRequestDTO.class));
		Assert.assertEquals("queueId", response.getQueueId());
		Assert.assertEquals("peerName", response.getPeerName());
		Assert.assertEquals("receiverKey", response.getReceiverQoSMonitorPublicKey());
	}


	//=================================================================================================
	// Test of pingMonitorNotification

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationTestOk() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getValidEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isAccepted())
				.andReturn();

		verify(eventWatcherService, times(1)).putEventToQueue(any());
		Assert.assertNotNull("pingMonitorNotificationTest result is null.", result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationNullEventTypeTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getNullEventTypeEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationEmptyEventTypeTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getEmptyEventTypeEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationInvalidEventTypeTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getInValidEventTypeEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationNullPayloadTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getNullPayloadEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationEmptyPayloadTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getEmptyPayloadEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationNullTimeStampTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getNullTimeStampEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationEmptyTimeStampTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getEmptyTimeStampEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void pingMonitorNotificationInvalidTimeStampTest() throws Exception {

		doNothing().when(eventWatcherService).putEventToQueue(any());

		final MvcResult result = this.mockMvc.perform(post(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getInvalidTimeStampEventDTOForTest()))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		verify(eventWatcherService, never()).putEventToQueue(any());

		final ErrorMessageDTO responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private EventDTO getValidEventDTOForTest() {

		return getValidReceivedMeasurementRequestEventDTOForTest();
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getInvalidTimeStampEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp("12:22:34-12:12:12");

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getEmptyTimeStampEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp("");

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getNullTimeStampEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(null);

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getNullPayloadEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(null);
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getEmptyPayloadEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload("");
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getNullEventTypeEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(null);
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getEmptyEventTypeEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType("");
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getInValidEventTypeEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType("UNKNOWN_MEAUSREMENT_EVENT");
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getValidReceivedMeasurementRequestEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<String, String> getValidMeasuermentEventDTOMetadtaProcessIdForTest() {

		return Map.of(QosMonitorConstants.PROCESS_ID_KEY, UUID.randomUUID().toString());

	}

	//-------------------------------------------------------------------------------------------------
	private String getValidMeasuermentEventDTOEmptyPayloadForTest() {

		return "[]";

	}
	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementListResponseDTO getIntraPingMeasurementListResponseDTOForTest() {

		final int responseSize = 3;
		final List<QoSIntraPingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(3);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getIntraPingMeasurementResponseDTOForTest());
		}

		return new QoSIntraPingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementListResponseDTO getIntraPingMeasurementListResponseDTOForTest(final int responseSize) {

		final List<QoSIntraPingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(responseSize);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getIntraPingMeasurementResponseDTOForTest());
		}

		return new QoSIntraPingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementResponseDTO getIntraPingMeasurementResponseDTOForTest() {

		final QoSIntraMeasurementResponseDTO qoSIntraMeasurementResponseDTO = getQoSIntraMeasurementResponseDTOForTest();

		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO  = new QoSIntraPingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(1L);
		pingMeasurementResponseDTO.setMeasurement(qoSIntraMeasurementResponseDTO);
		pingMeasurementResponseDTO.setAvailable(true);
		pingMeasurementResponseDTO.setLastAccessAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setMinResponseTime(1);
		pingMeasurementResponseDTO.setMaxResponseTime(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurementResponseDTO.setJitterWithTimeout(0);
		pingMeasurementResponseDTO.setJitterWithoutTimeout(0);
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(0);
		pingMeasurementResponseDTO.setSent(35);
		pingMeasurementResponseDTO.setReceived(35);
		pingMeasurementResponseDTO.setCountStartedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setSentAll(35);
		pingMeasurementResponseDTO.setReceivedAll(35);
		pingMeasurementResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return pingMeasurementResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurementResponseDTO getQoSIntraMeasurementResponseDTOForTest() {

		final SystemResponseDTO system = getSystemResponseDTOForTest();

		return new QoSIntraMeasurementResponseDTO(
				1,//measurement.getId(), 
				system, 
				QoSMeasurementType.PING,//measurement.getMeasurementType(), 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),//measurement.getLastMeasurementAt(), 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),//measurement.getCreatedAt(), 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));//measurement.getUpdatedAt());
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurementListResponseDTO getInterDirectPingMeasurementListResponseDTOForTest(final int responseSize) {
		final List<QoSInterDirectPingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(responseSize);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getInterDirectPingMeasurementResponseDTOForTest());
		}

		return new QoSInterDirectPingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurementResponseDTO getInterDirectPingMeasurementResponseDTOForTest() {
		final QoSInterDirectMeasurementResponseDTO interDirectMeasurement = getQoSInterDirectMeasurementResponseDTOForTest(QoSMeasurementType.PING);

		final QoSInterDirectPingMeasurementResponseDTO pingMeasurementResponseDTO  = new QoSInterDirectPingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(1L);
		pingMeasurementResponseDTO.setMeasurement(interDirectMeasurement);
		pingMeasurementResponseDTO.setAvailable(true);
		pingMeasurementResponseDTO.setLastAccessAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setMinResponseTime(1);
		pingMeasurementResponseDTO.setMaxResponseTime(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurementResponseDTO.setJitterWithTimeout(0);
		pingMeasurementResponseDTO.setJitterWithoutTimeout(0);
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(0);
		pingMeasurementResponseDTO.setSent(35);
		pingMeasurementResponseDTO.setReceived(35);
		pingMeasurementResponseDTO.setCountStartedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setSentAll(35);
		pingMeasurementResponseDTO.setReceivedAll(35);
		pingMeasurementResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return pingMeasurementResponseDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectMeasurementResponseDTO getQoSInterDirectMeasurementResponseDTOForTest(final QoSMeasurementType type) {
		return new QoSInterDirectMeasurementResponseDTO(1L,
														new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null),
														"1.1.1.1",
														type,
														Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
														Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
														Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurementListResponseDTO getInterRelayEchoMeasurementListResponseDTOForTest(final int responseSize) {
		final List<QoSInterRelayEchoMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(responseSize);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getInterRelayEchoMeasurementResponseDTOForTest());
		}

		return new QoSInterRelayEchoMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurementResponseDTO getInterRelayEchoMeasurementResponseDTOForTest() {
		final QoSInterRelayMeasurementResponseDTO interRelayMeasurement = getQoSInterRelayMeasurementResponseDTOForTest(QoSMeasurementType.PING);

		final QoSInterRelayEchoMeasurementResponseDTO relayMeasurementResponseDTO  = new QoSInterRelayEchoMeasurementResponseDTO();
		relayMeasurementResponseDTO.setId(1L);
		relayMeasurementResponseDTO.setMeasurement(interRelayMeasurement);
		relayMeasurementResponseDTO.setLastAccessAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		relayMeasurementResponseDTO.setMinResponseTime(1);
		relayMeasurementResponseDTO.setMaxResponseTime(1);
		relayMeasurementResponseDTO.setMeanResponseTimeWithTimeout(1);
		relayMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(1);
		relayMeasurementResponseDTO.setJitterWithTimeout(0);
		relayMeasurementResponseDTO.setJitterWithoutTimeout(0);
		relayMeasurementResponseDTO.setLostPerMeasurementPercent(0);
		relayMeasurementResponseDTO.setSent(35);
		relayMeasurementResponseDTO.setReceived(35);
		relayMeasurementResponseDTO.setCountStartedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		relayMeasurementResponseDTO.setSentAll(35);
		relayMeasurementResponseDTO.setReceivedAll(35);
		relayMeasurementResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		relayMeasurementResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return relayMeasurementResponseDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayMeasurementResponseDTO getQoSInterRelayMeasurementResponseDTOForTest(final QoSMeasurementType type) {
		return new QoSInterRelayMeasurementResponseDTO(1L,
													   new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "fddddbvf", null, null),
													   new RelayResponseDTO(1L, "2.2.2.2", 20000, null, true, false, RelayType.GATEWAY_RELAY, null, null),
													   type,
													   QoSMeasurementStatus.FINISHED,
													   Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
													   Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
													   Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		return new SystemResponseDTO(
				1L, 
				"testSystemName",//system.getSystemName(), 
				"localhost",//system.getAddress(), 
				12345,//system.getPort(), 
				"authinfo",//system.getAuthenticationInfo(),
				Map.of(),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

	}
	
	//-------------------------------------------------------------------------------------------------	
	private MvcResult getPublicKey(final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(get(QOSMONITOR_PUBLIC_KEY_URI)
						   .accept(MediaType.TEXT_PLAIN))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postInitTestRelayTest(final QoSMonitorSenderConnectionRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(QOSMONITOR_INIT_RELAY_TEST_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request)))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postJoinTestRelayTest(final QoSRelayTestProposalRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(QOSMONITOR_JOIN_RELAY_TEST_URI)
						   .accept(MediaType.APPLICATION_JSON)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request)))
						   .andExpect(matcher)
						   .andReturn();
	}
}
package eu.arrowhead.core.qos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.PingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QoSMonitorMain.class)
@ContextConfiguration(classes = { QoSMonitorTestContext.class })
public class QoSMonitorControllerTest {

	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String PING_MEASUREMENTS = "/ping/measurements";
	private static final String QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI =  CoreCommonConstants.MGMT_URI + PING_MEASUREMENTS;
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI = QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_URI = CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/{" + PATH_VARIABLE_ID + "}";

	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	private static final String PAGE_OR_SIZE_ERROR_MESSAGE = "Defined page or size could not be with undefined size or page.";


	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockQoSDBService") 
	QoSDBService qoSDBService;

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
		this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.ECHO_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}

	//=================================================================================================
	// Test of getPingMeasurements

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementsWithoutParametersTest() throws Exception {
		final PingMeasurementListResponseDTO pingMeasurementListResponseDTO = getPingMeasurementListResponseDTOForTest();

		when(qoSDBService.getPingMeasurementResponse( anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final PingMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), PingMeasurementListResponseDTO.class);
		assertEquals(pingMeasurementListResponseDTO.getData().size(), responseBody.getData().size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementsWithPageAndSizeParametersTest() throws Exception {
		final int responseSize = 3;
		final PingMeasurementListResponseDTO pingMeasurementListResponseDTO = getPingMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getPingMeasurementResponse( anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI)
				.param("page", "0")
				.param("item_per_page", String.valueOf(responseSize))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final PingMeasurementListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), PingMeasurementListResponseDTO.class);
		assertEquals(pingMeasurementListResponseDTO.getData().size(), responseBody.getData().size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementsWithNullPageButDefinedSizeParameterTest() throws Exception {
		final int responseSize = 3;
		final PingMeasurementListResponseDTO pingMeasurementListResponseDTO = getPingMeasurementListResponseDTOForTest(responseSize);

		when(qoSDBService.getPingMeasurementResponse( anyInt(), anyInt(), any(), anyString())).thenReturn(pingMeasurementListResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI)
				.param("item_per_page", String.valueOf(responseSize))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertTrue(responseBody.getErrorMessage().contains(PAGE_OR_SIZE_ERROR_MESSAGE));
		assertEquals(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI, responseBody.getOrigin());
	}

	//=================================================================================================
	// Test of getManagementPingMeasurementBySystemId

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementPingMeasurementBySystemIdTest() throws Exception {
		final int requestedId = 1;
		final PingMeasurementResponseDTO pingMeasurementResponseDTO = getPingMeasurementResponseDTOForTest();

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final PingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), PingMeasurementResponseDTO.class);
		assertEquals(requestedId, responseBody.getMeasurement().getSystem().getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementPingMeasurementBySystemIdInvalidSystemIdTest() throws Exception {
		final int requestedId = 0;
		final PingMeasurementResponseDTO pingMeasurementResponseDTO = getPingMeasurementResponseDTOForTest();

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(ID_NOT_VALID_ERROR_MESSAGE, responseBody.getErrorMessage());
		assertEquals(CommonConstants.QOS_MONITOR_URI + GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI, responseBody.getOrigin());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementPingMeasurementBySystemIdDBExceptionTest() throws Exception {
		final int requestedId = 1;

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenThrow(new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.ARROWHEAD, responseBody.getExceptionType());
		assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, responseBody.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getManagementPingMeasurementBySystemIdNoMeasurementInDBTest() throws Exception {
		final int requestedId = 1;
		final PingMeasurementResponseDTO pingMeasurementResponseDTO = new PingMeasurementResponseDTO();
		 pingMeasurementResponseDTO.setId( null );

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final PingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), PingMeasurementResponseDTO.class);
		assertNull(responseBody.getId());
	}

	//=================================================================================================
	// Test of getPingMeasurementBySystemIdResponse

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementBySystemIdTest() throws Exception {
		final int requestedId = 1;
		final PingMeasurementResponseDTO pingMeasurementResponseDTO = getPingMeasurementResponseDTOForTest();

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final PingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), PingMeasurementResponseDTO.class);
		assertEquals(requestedId, responseBody.getMeasurement().getSystem().getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementBySystemIdInvalidSystemIdTest() throws Exception {
		final int requestedId = 0;
		final PingMeasurementResponseDTO pingMeasurementResponseDTO = getPingMeasurementResponseDTOForTest();

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.BAD_PAYLOAD, responseBody.getExceptionType());
		assertEquals(ID_NOT_VALID_ERROR_MESSAGE, responseBody.getErrorMessage());
		assertEquals(CommonConstants.QOS_MONITOR_URI + GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_URI, responseBody.getOrigin());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementBySystemIdDBExceptionTest() throws Exception {
		final int requestedId = 1;

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenThrow(new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andReturn();

		final ErrorMessageDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ErrorMessageDTO.class);
		assertEquals(ExceptionType.ARROWHEAD, responseBody.getExceptionType());
		assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, responseBody.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getPingMeasurementBySystemIdNoMeasurementInDBTest() throws Exception {
		final int requestedId = 1;
		final PingMeasurementResponseDTO pingMeasurementResponseDTO = new PingMeasurementResponseDTO();
		 pingMeasurementResponseDTO.setId( null );

		when(qoSDBService.getPingMeasurementBySystemIdResponse(anyLong())).thenReturn(pingMeasurementResponseDTO);

		final MvcResult response = this.mockMvc.perform(get(CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/" + requestedId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		final PingMeasurementResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), PingMeasurementResponseDTO.class);
		assertNull(responseBody.getId());
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementListResponseDTO getPingMeasurementListResponseDTOForTest() {

		final int responseSize = 3;
		final List<PingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(3);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getPingMeasurementResponseDTOForTest());
		}

		return new PingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementListResponseDTO getPingMeasurementListResponseDTOForTest(final int responseSize) {

		final List<PingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(responseSize);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getPingMeasurementResponseDTOForTest());
		}

		return new PingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementResponseDTO getPingMeasurementResponseDTOForTest() {

		final QoSIntraMeasurementResponseDTO qoSIntraMeasurementResponseDTO = getQoSIntraMeasurementResponseDTOForTest();

		final PingMeasurementResponseDTO pingMeasurementResponseDTO  = new PingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(1L);
		pingMeasurementResponseDTO.setMeasurement(qoSIntraMeasurementResponseDTO);
		pingMeasurementResponseDTO.setAvailable(true);
		pingMeasurementResponseDTO.setLastAccessAt(ZonedDateTime.now());
		pingMeasurementResponseDTO.setMinResponseTime(1);
		pingMeasurementResponseDTO.setMaxResponseTime(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurementResponseDTO.setJitterWithTimeout(0);
		pingMeasurementResponseDTO.setJitterWithoutTimeout(0);
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(0);
		pingMeasurementResponseDTO.setSent(35);
		pingMeasurementResponseDTO.setReceived(35);
		pingMeasurementResponseDTO.setCountStartedAt(ZonedDateTime.now());
		pingMeasurementResponseDTO.setSentAll(35);
		pingMeasurementResponseDTO.setReceivedAll(35);
		pingMeasurementResponseDTO.setCreatedAt(ZonedDateTime.now());
		pingMeasurementResponseDTO.setUpdatedAt(ZonedDateTime.now());

		return pingMeasurementResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurementResponseDTO getQoSIntraMeasurementResponseDTOForTest() {

		final SystemResponseDTO system = getSystemResponseDTOForTest();

		return new QoSIntraMeasurementResponseDTO(
				1,//measurement.getId(), 
				system, 
				QoSMeasurementType.PING,//measurement.getMeasurementType(), 
				ZonedDateTime.now(),//measurement.getLastMeasurementAt(), 
				ZonedDateTime.now(),//measurement.getCreatedAt(), 
				ZonedDateTime.now());//measurement.getUpdatedAt());
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		return new SystemResponseDTO(
				1L, 
				"testSystemName",//system.getSystemName(), 
				"localhost",//system.getAddress(), 
				12345,//system.getPort(), 
				"authinfo",//system.getAuthenticationInfo(),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

	}

}

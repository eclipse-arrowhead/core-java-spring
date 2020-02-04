package eu.arrowhead.core.qos;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

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
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

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
		assertEquals(requestedId, responseBody.getId());
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementResponseDTO getPingMeasurementResponseDTOForTest() {

		final QoSIntraMeasurementResponseDTO qoSIntraMeasurementResponseDTO = getQoSIntraMeasurementResponseDTOForTest();

		final PingMeasurementResponseDTO pingMeasurementResponseDTO  = new PingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(1);
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

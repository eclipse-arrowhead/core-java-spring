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

package eu.arrowhead.core.qos.quartz.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterDirectMeasurement;
import eu.arrowhead.common.database.entity.QoSInterDirectPingMeasurement;
import eu.arrowhead.common.database.entity.QoSInterDirectPingMeasurementLog;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.measurement.properties.InterPingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;

@RunWith(SpringRunner.class)
public class CloudPingTaskTest {

	//=================================================================================================
	// members
	@InjectMocks
	private final CloudPingTask pingTask = new CloudPingTask();

	@Mock
	private PingService pingService;

	@Mock
	private QoSDBService qoSDBService;

	@Mock
	private QoSMonitorDriver qoSMonitorDriver;

	@Mock
	private InterPingMeasurementProperties pingMeasurementProperties;

	@Mock
	private Map<String,Object> arrowheadContext;

	@Mock
	private JobExecutionContext jobExecutionContext;

	private Logger logger;

	final static int TIME_TO_REPEAT_PING = 3;
	
	private static final List<CoreSystemService> REQUIRED_CORE_SERVICES = List.of(CoreSystemService.GATEKEEPER_PULL_CLOUDS, CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES,
			  																	  CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		logger = mock(Logger.class);
		ReflectionTestUtils.setField(pingTask, "logger", logger);

		when(pingMeasurementProperties.getLogMeasurementsToDB()).thenReturn(true);
		when(pingMeasurementProperties.getLogMeasurementsDetailsToDB()).thenReturn(true);
	}

	//=================================================================================================
	// Tests of execute

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteMeasurementIsInDB() {

		final CloudWithRelaysAndPublicRelaysListResponseDTO cloudWithRelaysListResponseDTO = getCloudWithRelaysListResponseDTOForTest(10);
		final CloudAccessListResponseDTO cloudAccessListResponseDTO = getCloudAccessListResponseDTOForTest(10);
		final List<QoSInterDirectMeasurement> measurementList = getListOfQoSInterDirectMeasurementForTest(10);

		final SystemAddressSetRelayResponseDTO systemAddressSet = getSystemAddressSetRelayResponseDTOForTest(10);
		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSInterDirectMeasurement measurement = getQoSInterDirectMeasurementForTest();
		final QoSInterDirectPingMeasurement interPingMeasurement = getInterPingMeasurementForTest();
		final QoSInterDirectPingMeasurementLog measurementLogSaved = getInterPingMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		final ArgumentCaptor<CloudResponseDTO> cloudResponseDTOValueCaptor = ArgumentCaptor.forClass(CloudResponseDTO.class);
		final ArgumentCaptor<CloudRequestDTO> cloudRequestDTOValueCaptor = ArgumentCaptor.forClass(CloudRequestDTO.class);
		final ArgumentCaptor<List<CloudRequestDTO>> cloudRequestListDTOValueCaptor = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<String> addressValueCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<QoSInterDirectMeasurement> measurementValueCaptor = ArgumentCaptor.forClass(QoSInterDirectMeasurement.class);
		final ArgumentCaptor<QoSInterDirectPingMeasurement> interPingMeasurementValueCaptor = ArgumentCaptor.forClass(QoSInterDirectPingMeasurement.class);
		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsDTOValueCaptor = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);
		final ArgumentCaptor<ZonedDateTime> aroundNowValueCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		for (CoreSystemService coreSystemService : REQUIRED_CORE_SERVICES) {
			final String key = coreSystemService.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
			when(arrowheadContext.containsKey(eq(key))).thenReturn(true);
		}

		when(qoSMonitorDriver.queryGatekeeperAllCloud()).thenReturn(cloudWithRelaysListResponseDTO);
		when(qoSMonitorDriver.queryGatekeeperCloudAccessTypes(cloudRequestListDTOValueCaptor.capture())).thenReturn(cloudAccessListResponseDTO);
		//TEST it with empty List response too
		when(qoSDBService.getInterDirectMeasurementByCloud(cloudResponseDTOValueCaptor.capture(), any())).thenReturn(measurementList);

		when(qoSMonitorDriver.queryGatekeeperAllSystemAddresses(cloudRequestDTOValueCaptor.capture())).thenReturn(systemAddressSet);

		when(pingService.getPingResponseList(addressValueCaptor.capture())).thenReturn(responseList);
		when(qoSDBService.getOrCreateDirectInterMeasurement(addressValueCaptor.capture(), cloudResponseDTOValueCaptor.capture(), any())).thenReturn(measurement);

		//Handle InterPingMeasurement
		when(qoSDBService.getInterDirectPingMeasurementByMeasurement(measurementValueCaptor.capture())).thenReturn(Optional.of(interPingMeasurement));

		//If measurement is empty
		//doNothing().when(qoSDBService).createInterPingMeasurement( measurementValueCaptor.capture(), calculationsDTOValueCaptor.capture(), aroundNowValueCaptor.capture() );
		//If measurement is not empty
		doNothing().when(qoSDBService).updateInterDirectPingMeasurement( measurementValueCaptor.capture(), calculationsDTOValueCaptor.capture(), interPingMeasurementValueCaptor.capture(), aroundNowValueCaptor.capture() );

		when(qoSDBService.logInterDirectMeasurementToDB(addressValueCaptor.capture(), calculationsDTOValueCaptor.capture(), aroundNowValueCaptor.capture())).thenReturn(measurementLogSaved);
		doNothing().when(qoSDBService).logInterDirectMeasurementDetailsToDB(any(), any(), any());

		//Handle InterPingMeasurement over ...

		doNothing().when(qoSDBService).updateInterDirectMeasurement(any(), measurementValueCaptor.capture());

		//--------------------------------------------------------------------------

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(REQUIRED_CORE_SERVICES.size() + 1)).containsKey(anyString());
		verify(qoSMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qoSMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qoSDBService, times(1)).getInterDirectMeasurementByCloud(any(), any());
		verify(qoSMonitorDriver, times(1)).queryGatekeeperAllSystemAddresses(any());

		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateDirectInterMeasurement(any(), any(), any());

		verify(qoSDBService, atLeastOnce()).getInterDirectPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).updateInterDirectPingMeasurement(any(), any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logInterDirectMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logInterDirectMeasurementDetailsToDB(any(), any(), any());
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CloudAccessListResponseDTO getCloudAccessListResponseDTOForTest(final int count) {

		final List<CloudAccessResponseDTO> list = new ArrayList<CloudAccessResponseDTO>(count);
		for (int i = 0; i < count; i++) {
			list .add(getCloudAccessResponseDTOForTest());
		}

		return new CloudAccessListResponseDTO(list, count);
	}

	//-------------------------------------------------------------------------------------------------
	private CloudAccessResponseDTO getCloudAccessResponseDTOForTest() {

		return new CloudAccessResponseDTO(
				"testCloudxxx", 
				"sysop",
				true); //directAccess
	}

	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurement getInterPingMeasurementForTest() {

		final QoSInterDirectPingMeasurement pingMeasurement = new QoSInterDirectPingMeasurement();

		pingMeasurement.setMeasurement(getQoSInterDirectMeasurementForTest());
		pingMeasurement.setAvailable(true);
		pingMeasurement.setMaxResponseTime(1);
		pingMeasurement.setMinResponseTime(1);
		pingMeasurement.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurement.setMeanResponseTimeWithTimeout(1);
		pingMeasurement.setJitterWithoutTimeout(1);
		pingMeasurement.setJitterWithTimeout(1);
		pingMeasurement.setLostPerMeasurementPercent(0);
		pingMeasurement.setCountStartedAt(ZonedDateTime.now());
		pingMeasurement.setLastAccessAt(ZonedDateTime.now());
		pingMeasurement.setSent(35);
		pingMeasurement.setSentAll(35);
		pingMeasurement.setReceived(35);
		pingMeasurement.setReceivedAll(35);

		return pingMeasurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectMeasurement getQoSInterDirectMeasurementForTest() {

		return new QoSInterDirectMeasurement(
				getCloudForTest(),
				"address",
				QoSMeasurementType.PING,
				ZonedDateTime.now().minusMinutes(30));
	}

	//-------------------------------------------------------------------------------------------------
	private SystemAddressSetRelayResponseDTO getSystemAddressSetRelayResponseDTOForTest(final int count) {

		return new SystemAddressSetRelayResponseDTO(getSystemAddressSetForTest(count));
	}

	//-------------------------------------------------------------------------------------------------
	private Set<String> getSystemAddressSetForTest(final int count) {

		final Set<String> set = new HashSet<>(count);
		for (int i = 0; i < count; i++) {
			set.add(i+"address");
		}

		return set;
	}

	//-------------------------------------------------------------------------------------------------
	private Cloud getCloudForTest() {

		final Cloud cloud = new Cloud(
				"sysop", //operator,
				"testCloudxxx", //name,
				true, //secure,
				true, //neighbor,
				false, //ownCloud,
				"authenticationInfo");

		cloud.setId(1L);

		return cloud;
	}

	//-------------------------------------------------------------------------------------------------
	private List<QoSInterDirectMeasurement> getListOfQoSInterDirectMeasurementForTest(final int count) {

		final List<QoSInterDirectMeasurement> list = new ArrayList<QoSInterDirectMeasurement>(count);
		for (int i = 0; i < count; i++) {
			final QoSInterDirectMeasurement measurement = getQoSInterDirectMeasurementForTest();
			measurement.setId(i + 1L);
			measurement.setCreatedAt(measurement.getLastMeasurementAt());
			measurement.setUpdatedAt(measurement.getCreatedAt().plusMinutes(i));
			list .add(measurement);
		}

		return list;
	}

	//-------------------------------------------------------------------------------------------------
	private CloudWithRelaysAndPublicRelaysListResponseDTO getCloudWithRelaysListResponseDTOForTest(final int count) {

		return new CloudWithRelaysAndPublicRelaysListResponseDTO(getListOfCloudWithRelaysResponseDTOForTest(count), count);
	}

	//-------------------------------------------------------------------------------------------------
	private List<CloudWithRelaysAndPublicRelaysResponseDTO> getListOfCloudWithRelaysResponseDTOForTest(final int count) {

		final List<CloudWithRelaysAndPublicRelaysResponseDTO> list = new ArrayList<CloudWithRelaysAndPublicRelaysResponseDTO>(count);
		for (int i = 0; i < count; i++) {
			list .add(getCloudWithRelaysResponseDTOForTest());
		}

		return list;
	}

	//-------------------------------------------------------------------------------------------------
	private CloudWithRelaysAndPublicRelaysResponseDTO getCloudWithRelaysResponseDTOForTest() {

		return new CloudWithRelaysAndPublicRelaysResponseDTO(
				1L, //id,
				"sysop", //operator,
				"testCloudxxx", //name,
				true, //secure,
				true, //neighbor,
				false, //ownCloud,
				"authenticationInfo", //authenticationInfo,
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()), //createdAt,
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()), //updatedAt,
				null, //List.of(new RelayResponseDTO()),//gatekeeperRelays,
				null, //List.of(new RelayResponseDTO())); //gatewayRelays);
				null);
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getResponseListForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		for (int i = 0; i < TIME_TO_REPEAT_PING; i++) {

			if (i%2 == 0) {
				final IcmpPingResponse pingResponse = getIcmpPingResponse();
				pingResponse.setSuccessFlag(false);
				pingResponse.setTimeoutFlag(true);
				pingResponse.setRtt(0);
				pingResponse.setDuration(0);

				responseList.add(pingResponse);
			}else {
				responseList.add(getIcmpPingResponse());
			}
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private IcmpPingResponse getIcmpPingResponse() {

		final IcmpPingResponse pingResponse = new IcmpPingResponse();
		pingResponse.setSuccessFlag(true);
		pingResponse.setTimeoutFlag(false);
		pingResponse.setRtt(1);
		pingResponse.setDuration(1L);
		pingResponse.setTtl(64);
		pingResponse.setSize(32);
		pingResponse.setHost("localhost");

		return pingResponse;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSInterDirectPingMeasurementLog getInterPingMeasurementLogForTest() {

		final QoSInterDirectPingMeasurementLog measurementLog = new QoSInterDirectPingMeasurementLog();
		measurementLog.setAvailable(true);

		return measurementLog;
	}
}
package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;
import eu.arrowhead.core.qos.database.service.QoSReservationDBService;
import eu.arrowhead.core.qos.manager.QoSVerifier;

@RunWith(SpringRunner.class)
public class QoSManagerImplTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private QoSManagerImpl qosManager;
	
	@Mock
	private QoSReservationDBService qosReservationDBService;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private QoSVerifier verifier;
	
	@Mock
	private QoSVerifier verifier2;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		verifiers.add(verifier);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersResultListNull() {
		qosManager.filterReservedProviders(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersRequesterNull() {
		qosManager.filterReservedProviders(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersRequesterSystemNameNull() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		qosManager.filterReservedProviders(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersRequesterSystemNameEmpty() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName(" ");
		qosManager.filterReservedProviders(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersRequesterAddressNull() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		qosManager.filterReservedProviders(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersRequesterAddressEmpty() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress(" ");
		qosManager.filterReservedProviders(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterReservedProvidersRequesterPortNull() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		qosManager.filterReservedProviders(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterReservedProvidersResultListEmpty() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		requester.setPort(1234);
		final List<OrchestrationResultDTO> filtered = qosManager.filterReservedProviders(List.of(), requester);
		Assert.assertEquals(0, filtered.size());
		verify(qosReservationDBService, never()).getAllReservationsExceptMine(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterReservedProvidersNothingIsReserved() {
		when(qosReservationDBService.getAllReservationsExceptMine(anyString(), anyString(), anyInt())).thenReturn(List.of());
		
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		requester.setPort(1234);
		final List<OrchestrationResultDTO> filtered = qosManager.filterReservedProviders(getOrList(3), requester);
		Assert.assertEquals(3, filtered.size());
		verify(qosReservationDBService, times(1)).getAllReservationsExceptMine(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyResultListNull() {
		qosManager.reserveProvidersTemporarily(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyRequesterNull() {
		qosManager.reserveProvidersTemporarily(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyRequesterSystemNameNull() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		qosManager.reserveProvidersTemporarily(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyRequesterSystemNameEmpty() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName(" ");
		qosManager.reserveProvidersTemporarily(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyRequesterAddressNull() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		qosManager.reserveProvidersTemporarily(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyRequesterAddressEmpty() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress(" ");
		qosManager.reserveProvidersTemporarily(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReserveProvidersTemporarilyRequesterPortNull() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		qosManager.reserveProvidersTemporarily(List.of(), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReserveProvidersTemporarilyResultListEmpty() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		requester.setPort(1234);
		final List<OrchestrationResultDTO> reserved = qosManager.reserveProvidersTemporarily(List.of(), requester);
		Assert.assertEquals(0, reserved.size());
		verify(qosReservationDBService, never()).applyTemporaryLock(anyString(), anyString(), anyInt(), any(OrchestrationResultDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReserveProvidersTemporarilyLockFailed() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		requester.setPort(1234);
		doThrow(ArrowheadException.class).when(qosReservationDBService).applyTemporaryLock(anyString(), anyString(), anyInt(), any(OrchestrationResultDTO.class));
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setProvider(new SystemResponseDTO(1, "system", "localhost", 1234, null, null, null, null));
		result.setService(new ServiceDefinitionResponseDTO(1, "service", null, null));
		final List<OrchestrationResultDTO> reserved = qosManager.reserveProvidersTemporarily(List.of(result), requester);
		Assert.assertEquals(0, reserved.size());
		verify(qosReservationDBService, times(1)).applyTemporaryLock(anyString(), anyString(), anyInt(), any(OrchestrationResultDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReserveProvidersTemporarilyLockSuccess() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		requester.setPort(1234);
		doNothing().when(qosReservationDBService).applyTemporaryLock(anyString(), anyString(), anyInt(), any(OrchestrationResultDTO.class));
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setProvider(new SystemResponseDTO(1, "system", "localhost", 1234, null, null, null, null));
		result.setService(new ServiceDefinitionResponseDTO(1, "service", null, null));
		final List<OrchestrationResultDTO> reserved = qosManager.reserveProvidersTemporarily(List.of(result), requester);
		Assert.assertEquals(1, reserved.size());
		verify(qosReservationDBService, times(1)).applyTemporaryLock(anyString(), anyString(), anyInt(), any(OrchestrationResultDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationSelectedNull() {
		qosManager.confirmReservation(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationResultListNull() {
		qosManager.confirmReservation(new OrchestrationResultDTO(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationResultListEmpty() {
		qosManager.confirmReservation(new OrchestrationResultDTO(), List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationRequesterNull() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		qosManager.confirmReservation(result, List.of(result), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationRequesterSystemNameNull() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final SystemRequestDTO requester = new SystemRequestDTO();
		qosManager.confirmReservation(result, List.of(result), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationRequesterSystemNameEmpty() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName(" ");
		qosManager.confirmReservation(result, List.of(result), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationRequesterAddressNull() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		qosManager.confirmReservation(result, List.of(result), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationRequesterAddressEmpty() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress(" ");
		qosManager.confirmReservation(result, List.of(result), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConfirmReservationRequesterPortNull() {
		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		qosManager.confirmReservation(result, List.of(result), requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConfirmReservationOk() {
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("localhost");
		requester.setPort(1234);
		final List<OrchestrationResultDTO> orList = getOrList(3);
		doNothing().when(qosReservationDBService).extendReservation(any(OrchestrationResultDTO.class), any(SystemRequestDTO.class));
		doNothing().when(qosReservationDBService).removeTemporaryLock(any(OrchestrationResultDTO.class));
		
		qosManager.confirmReservation(orList.get(1), orList, requester);
		
		verify(qosReservationDBService, times(1)).extendReservation(any(OrchestrationResultDTO.class), any(SystemRequestDTO.class));
		verify(qosReservationDBService, times(2)).removeTemporaryLock(any(OrchestrationResultDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyServicesResultListNull() {
		qosManager.verifyIntraCloudServices(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyServicesRequestNull() {
		qosManager.verifyIntraCloudServices(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIntraCloudServicesTrue() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(true);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyIntraCloudServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIntraCloudServicesFalseNoExclusivity() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyIntraCloudServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIntraCloudServicesFalseWithExclusivity() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);
		doNothing().when(qosReservationDBService).removeTemporaryLock(any(OrchestrationResultDTO.class));

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "100"));
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyIntraCloudServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		verify(qosReservationDBService).removeTemporaryLock(any(OrchestrationResultDTO.class));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIntraCloudServicesNoVerifiers() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyIntraCloudServices(getOrList(1), request);
		verify(verifier, never()).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyIntraCloudServicesFirstVerifierFalse() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		verifiers.add(verifier);
		verifiers.add(verifier2);
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);
		when(verifier2.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(true);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyIntraCloudServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		verify(verifier2, never()).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(0, verifiedList.size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPreVerifyInterCloudServicesResultListNull() {
		qosManager.preVerifyInterCloudServices(null, new OrchestrationFormRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPreVerifyInterCloudServicesRequestNull() {
		qosManager.preVerifyInterCloudServices(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPreVerifyInterCloudServicesTrue() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE))).thenReturn(true);
		when(orchestratorDriver.getIntraPingMedianMeasurement(any(QoSMeasurementAttribute.class))).thenReturn(getQosIntraPingMeasurementForTest());
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<GSDPollResponseDTO> verifiedList = qosManager.preVerifyInterCloudServices(getGSDList(1), request);
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE));
		Assert.assertEquals(1, verifiedList.size());
		Assert.assertEquals(2, verifiedList.get(0).getAvailableInterfaces().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPreVerifyInterCloudServicesFalse() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE))).thenReturn(false);
		when(orchestratorDriver.getIntraPingMedianMeasurement(any(QoSMeasurementAttribute.class))).thenReturn(getQosIntraPingMeasurementForTest());
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<GSDPollResponseDTO> verifiedList = qosManager.preVerifyInterCloudServices(getGSDList(1), request);
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPreVerifyInterCloudServicesNoVerifiers() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE))).thenReturn(false);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<GSDPollResponseDTO> verifiedList = qosManager.preVerifyInterCloudServices(getGSDList(1), request);
		verify(verifier, never()).verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE));
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPreVerifyInterCloudServicesFirstVerifierFalse() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		verifiers.add(verifier);
		verifiers.add(verifier2);
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE))).thenReturn(false);
		when(verifier2.verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE))).thenReturn(true);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<GSDPollResponseDTO> verifiedList = qosManager.preVerifyInterCloudServices(getGSDList(1), request);;
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE));
		verify(verifier2, never()).verify(any(QoSVerificationParameters.class), eq(Boolean.TRUE));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyInterCloudServicesResultListNull() {
		qosManager.verifyInterCloudServices(new CloudResponseDTO(), null, new HashMap<>(), new HashMap<>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyInterCloudServicesQoSRequirementsNull() {
		qosManager.verifyInterCloudServices(new CloudResponseDTO(), List.of(), null, new HashMap<>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyInterCloudServicesCommandsNull() {
		qosManager.verifyInterCloudServices(new CloudResponseDTO(), List.of(), new HashMap<>(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void tesVerifyInterCloudServicesTrue() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(true);
		when(orchestratorDriver.getIntraPingMedianMeasurement(any(QoSMeasurementAttribute.class))).thenReturn(getQosIntraPingMeasurementForTest());
		
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyInterCloudServices(new CloudResponseDTO(), getOrList(1), new HashMap<>(), new HashMap<>());
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void tesVerifyInterCloudServicesFalse() {
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);
		when(orchestratorDriver.getIntraPingMedianMeasurement(any(QoSMeasurementAttribute.class))).thenReturn(getQosIntraPingMeasurementForTest());
		
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyInterCloudServices(new CloudResponseDTO(), getOrList(1), new HashMap<>(), new HashMap<>());
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyInterCloudServicesNoVerifiers() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);

		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyInterCloudServices(new CloudResponseDTO(), getOrList(1), new HashMap<>(), new HashMap<>());
		verify(verifier, never()).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyInterCloudServicesFirstVerifierFalse() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		verifiers.add(verifier);
		verifiers.add(verifier2);
		when(verifier.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(false);
		when(verifier2.verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE))).thenReturn(true);

		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyInterCloudServices(new CloudResponseDTO(), getOrList(1), new HashMap<>(), new HashMap<>());
		verify(verifier, times(1)).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		verify(verifier2, never()).verify(any(QoSVerificationParameters.class), eq(Boolean.FALSE));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestrationResultDTO> getOrList(final int num) {
		final List<OrchestrationResultDTO> orList = new ArrayList<>(num);
		for (int i = 0; i < num; ++i) {
			final SystemResponseDTO provider = new SystemResponseDTO();
			provider.setId(i);
			final ServiceDefinitionResponseDTO service = new ServiceDefinitionResponseDTO();
			service.setId(1);
			final OrchestrationResultDTO result = new OrchestrationResultDTO();
			result.setProvider(provider);
			result.setService(service);
			orList.add(result);
		}
		
		return orList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<GSDPollResponseDTO> getGSDList(final int num) {
		final List<GSDPollResponseDTO> gsdList = new ArrayList<>(num);
		for (int i = 0; i < num; ++i) {
			final CloudResponseDTO cloud = new CloudResponseDTO(i, "test-op-" + i, "test-n-" + i, true, true, false, "dsvcdsvi" + i, null, null);
			final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
			srEntry.setId(i);
			srEntry.setServiceDefinition(new ServiceDefinitionResponseDTO(i, "test-service", null, null));
			srEntry.setProvider(new SystemResponseDTO(i, "test-sys" + i, "1.1.1.1", 1000, "fgfh", null, null, null));
			srEntry.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1, "HTTP-SECURE-JSON", null, null), new ServiceInterfaceResponseDTO(1, "HTTP-SECURE-XML", null, null)));
			final QoSMeasurementAttributesFormDTO measurement = new QoSMeasurementAttributesFormDTO();
			measurement.setServiceRegistryEntry(srEntry);
			measurement.setProviderAvailable(true);
			final GSDPollResponseDTO gsd = new GSDPollResponseDTO(cloud, "test-service", List.of("HTTP-SECURE-JSON", "HTTP-SECURE-XML"), 1, List.of(measurement), new HashMap<>(), false);
			gsdList.add(gsd);
		}
		
		return gsdList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementResponseDTO getQosIntraPingMeasurementForTest() {

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();

		pingMeasurement.setMeasurement(measurement);
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

		return DTOConverter.convertQoSIntraPingMeasurementToPingMeasurementResponseDTO(pingMeasurement);
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurement getQoSIntraMeasurementForTest() {

		final System system = new System("test-sys", "1.1.1.1", AddressType.IPV4, 1000, "dfvldsfme", null);
		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(
				system, 
				QoSMeasurementType.PING, 
				ZonedDateTime.now());

		return measurement;
	}
}
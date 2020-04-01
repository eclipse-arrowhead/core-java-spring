package eu.arrowhead.core.qos.manager.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
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
		result.setProvider(new SystemResponseDTO(1, "system", "localhost", 1234, null, null, null));
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
		result.setProvider(new SystemResponseDTO(1, "system", "localhost", 1234, null, null, null));
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
		qosManager.verifyServices(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testVerifyServicesRequestNull() {
		qosManager.verifyServices(List.of(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyServicesTrue() {
		when(verifier.verify(any(OrchestrationResultDTO.class), anyMap(), anyMap())).thenReturn(true);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(OrchestrationResultDTO.class), anyMap(), anyMap());
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyServicesFalseNoExclusivity() {
		when(verifier.verify(any(OrchestrationResultDTO.class), anyMap(), anyMap())).thenReturn(false);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(OrchestrationResultDTO.class), anyMap(), anyMap());
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyServicesFalseWithExclusivity() {
		when(verifier.verify(any(OrchestrationResultDTO.class), anyMap(), anyMap())).thenReturn(false);
		doNothing().when(qosReservationDBService).removeTemporaryLock(any(OrchestrationResultDTO.class));

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "100"));
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(OrchestrationResultDTO.class), anyMap(), anyMap());
		verify(qosReservationDBService).removeTemporaryLock(any(OrchestrationResultDTO.class));
		Assert.assertEquals(0, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyServicesNoVerifiers() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		when(verifier.verify(any(OrchestrationResultDTO.class), anyMap(), anyMap())).thenReturn(false);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyServices(getOrList(1), request);
		verify(verifier, never()).verify(any(OrchestrationResultDTO.class), anyMap(), anyMap());
		Assert.assertEquals(1, verifiedList.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testVerifyServicesFirstVerifierFalse() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<QoSVerifier> verifiers	= (List) ReflectionTestUtils.getField(qosManager, "verifiers");
		verifiers.clear();
		verifiers.add(verifier);
		verifiers.add(verifier2);
		when(verifier.verify(any(OrchestrationResultDTO.class), anyMap(), anyMap())).thenReturn(false);
		when(verifier2.verify(any(OrchestrationResultDTO.class), anyMap(), anyMap())).thenReturn(true);

		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setCommands(Map.of());
		final List<OrchestrationResultDTO> verifiedList = qosManager.verifyServices(getOrList(1), request);
		verify(verifier, times(1)).verify(any(OrchestrationResultDTO.class), anyMap(), anyMap());
		verify(verifier2, never()).verify(any(OrchestrationResultDTO.class), anyMap(), anyMap());
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
}
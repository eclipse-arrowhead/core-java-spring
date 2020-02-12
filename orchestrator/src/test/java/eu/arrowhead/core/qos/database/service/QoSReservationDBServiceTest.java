package eu.arrowhead.core.qos.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.QoSReservation;
import eu.arrowhead.common.database.repository.QoSReservationRepository;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@RunWith(SpringRunner.class)
public class QoSReservationDBServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private QoSReservationDBService testingObject;

	@Mock
	private QoSReservationRepository qosReservationRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetAllReservationsExceptMineSystemNameNull() {
		testingObject.getAllReservationsExceptMine(null, null, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetAllReservationsExceptMineSystemNameEmpty() {
		testingObject.getAllReservationsExceptMine(" ", null, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetAllReservationsExceptMineAddressNull() {
		testingObject.getAllReservationsExceptMine("system", null, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetAllReservationsExceptMineAddressEmpty() {
		testingObject.getAllReservationsExceptMine("system", " ", 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAllReservationsExceptMineOk() {
		when(qosReservationRepository.findAllByConsumerSystemNameNotOrConsumerAddressNotOrConsumerPortNot(anyString(), anyString(), anyInt())).thenReturn(List.of());
		testingObject.getAllReservationsExceptMine("System", "Address", 1234);
		verify(qosReservationRepository, times(1)).findAllByConsumerSystemNameNotOrConsumerAddressNotOrConsumerPortNot("system", "address", 1234);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockSystemNameNull() {
		testingObject.applyTemporaryLock(null, null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockSystemNameEmpty() {
		testingObject.applyTemporaryLock("\t", null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockAddressNull() {
		testingObject.applyTemporaryLock("system", null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockAddressEmpty() {
		testingObject.applyTemporaryLock("system", "", 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockDTONull() {
		testingObject.applyTemporaryLock("system", "address", 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockProviderNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		testingObject.applyTemporaryLock("system", "address", 0, dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testApplyTemporaryLockServiceNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		testingObject.applyTemporaryLock("system", "address", 0, dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testApplyTemporaryLockOk() {
		when(qosReservationRepository.saveAndFlush(any(QoSReservation.class))).thenReturn(new QoSReservation());
		
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		testingObject.applyTemporaryLock("system", "address", 1234, dto);
		
		verify(qosReservationRepository, times(1)).saveAndFlush(any(QoSReservation.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTemporaryLockDTONull() {
		testingObject.removeTemporaryLock(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTemporaryLockProviderNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		testingObject.removeTemporaryLock(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTemporaryLockServiceNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		testingObject.removeTemporaryLock(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveTemporaryLockOkNotFound() {
		when(qosReservationRepository.findByReservedProviderIdAndReservedServiceIdAndTemporaryLockTrue(anyLong(), anyLong())).thenReturn(Optional.empty());
		
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		testingObject.removeTemporaryLock(dto);
		
		verify(qosReservationRepository, never()).delete(any(QoSReservation.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveTemporaryLockOkFound() {
		final QoSReservation reservation = new QoSReservation();
		when(qosReservationRepository.findByReservedProviderIdAndReservedServiceIdAndTemporaryLockTrue(anyLong(), anyLong())).thenReturn(Optional.of(reservation));
		doNothing().when(qosReservationRepository).delete(reservation);
		doNothing().when(qosReservationRepository).flush();
		
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		testingObject.removeTemporaryLock(dto);
		
		verify(qosReservationRepository, times(1)).delete(reservation);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationReservedNull() {
		testingObject.extendReservation(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationProviderNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		testingObject.extendReservation(dto, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationServiceNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		testingObject.extendReservation(dto, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationRequesterNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		testingObject.extendReservation(dto, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationRequesterSystemNameNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		final SystemRequestDTO requester = new SystemRequestDTO();
		testingObject.extendReservation(dto, requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationRequesterSystemNameEmpty() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName(" ");
		testingObject.extendReservation(dto, requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationRequesterAddressNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		testingObject.extendReservation(dto, requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationRequesterAddressEmpty() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("     ");
		testingObject.extendReservation(dto, requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExtendReservationRequesterPortNull() {
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("address");
		testingObject.extendReservation(dto, requester);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExtendReservationOk() {
		ReflectionTestUtils.setField(testingObject, "maxReservationDuration", 1000);
		final QoSReservation reservation = new QoSReservation();
		when(qosReservationRepository.findByReservedProviderIdAndReservedServiceId(anyLong(), anyLong())).thenReturn(Optional.of(reservation));
		when(qosReservationRepository.saveAndFlush(reservation)).thenReturn(reservation);
		
		final OrchestrationResultDTO dto = new OrchestrationResultDTO();
		dto.setProvider(new SystemResponseDTO());
		dto.setService(new ServiceDefinitionResponseDTO());
		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName("system");
		requester.setAddress("address");
		requester.setPort(1234);
		testingObject.extendReservation(dto, requester);

		verify(qosReservationRepository, times(1)).saveAndFlush(reservation);
	}
}
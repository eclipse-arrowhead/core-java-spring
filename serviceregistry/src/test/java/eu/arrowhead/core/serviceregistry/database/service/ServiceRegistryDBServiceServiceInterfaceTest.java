package eu.arrowhead.core.serviceregistry.database.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceServiceInterfaceTest {

	//=================================================================================================
	// members

	@InjectMocks
	private ServiceRegistryDBService serviceRegistryDBService; 

	@Mock
	private ServiceInterfaceRepository serviceInterfaceRepository;

	@Mock
	private ServiceInterfaceNameVerifier interfaceNameVerifier;

	//=================================================================================================
	// methods

	//=================================================================================================
	// Tests of getServiceInterfaceById

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getServiceInterfaceByIdTestWithNotExistingId() {
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

		serviceRegistryDBService.getServiceInterfaceById(1);
	}

	//=================================================================================================
	//Tests of getServiceInterfaceEntries

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getServiceInterfaceEntriesTestWithInvalidSortField() {
		serviceRegistryDBService.getServiceInterfaceEntries(0, 10, Direction.ASC, "notValid");
	}

	//=================================================================================================
	//Tests of createServiceInterface

	@Test(expected = InvalidParameterException.class)
	public void createServiceInterfaceTestWithNullInput() {
		serviceRegistryDBService.createServiceInterface(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceInterfaceTestWithBlankStringInput() {
		serviceRegistryDBService.createServiceInterface("       ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceInterfaceTestOfUniqueKeyViolation() {
		final String testInterface = "alreadyexiststest";
		final Optional<ServiceInterface> serviceInterfaceEntry = Optional.of(new ServiceInterface(testInterface));

		when(interfaceNameVerifier.isValid(eq(testInterface))).thenReturn(Boolean.TRUE);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry);

		serviceRegistryDBService.createServiceInterface(testInterface);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceInterfaceTestCaseInsensitivityOfUniqueKeyViolation() {
		final String testInterface = "alreadyexiststest";
		final Optional<ServiceInterface> serviceInterfaceEntry = Optional.of(new ServiceInterface(testInterface));

		when(interfaceNameVerifier.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry);

		serviceRegistryDBService.createServiceInterface(testInterface.toUpperCase());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceInterfaceTestLeadingTrailingSpaceSensitivityOfUniqueKeyViolation() {
		final String testInterface = "alreadyexiststest";
		final Optional<ServiceInterface> serviceInterfaceEntry = Optional.of(new ServiceInterface(testInterface));

		when(interfaceNameVerifier.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry);

		serviceRegistryDBService.createServiceInterface("  " + testInterface + "  ");
	}

	//=================================================================================================
	// Tests of updateServiceInterfaceById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceInterfaceByIdTestWithNullInput() {
		serviceRegistryDBService.updateServiceInterfaceById(1, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceInterfaceByIdTestWithBlankStringInput() {
		serviceRegistryDBService.updateServiceInterfaceById(1, "   ");
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceInterfaceByIdTestWithNotExistingId() {
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

		when(interfaceNameVerifier.isValid(anyString())).thenReturn(Boolean.TRUE);
		serviceRegistryDBService.updateServiceInterfaceById(1, "test");
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceInterfaceByIdTestOfUniqueKeyViolation() {		
		final String testInterface2 = "testinterface2";
		final long testId2 = 2;
		final ServiceInterface serviceInterface2 = new ServiceInterface(testInterface2);
		serviceInterface2.setId(testId2);
		final Optional<ServiceInterface> serviceInterfaceEntry2 = Optional.of(serviceInterface2);
		final String testInterface1 = "testinterface1";
		final long testId1 = 1;
		final ServiceInterface serviceInterface1 = new ServiceInterface(testInterface1);
		serviceInterface1.setId(testId1);
		final Optional<ServiceInterface> serviceInterfaceEntry1 = Optional.of(serviceInterface1);

		when(interfaceNameVerifier.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceInterfaceRepository.findById(eq(testId2))).thenReturn(serviceInterfaceEntry2);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry1);

		serviceRegistryDBService.updateServiceInterfaceById(testId2, testInterface1);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceInterfaceByIdTestCaseInsensitivityOfUniqueKeyViolation() {
		final String testInterface0 = "testinterface0";
		final long testId0 = 0;
		final ServiceInterface serviceInterface0 = new ServiceInterface(testInterface0);
		serviceInterface0.setId(testId0);
		final Optional<ServiceInterface> serviceInterfaceEntry0 = Optional.of(serviceInterface0);
		final String testInterface1 = "testinterface1";
		final long testId1 = 1;
		final ServiceInterface serviceInterface1 = new ServiceInterface(testInterface1);
		serviceInterface1.setId(testId1);
		final Optional<ServiceInterface> serviceInterfaceEntry1 = Optional.of(serviceInterface1);

		when(interfaceNameVerifier.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceInterfaceRepository.findById(eq(testId0))).thenReturn(serviceInterfaceEntry0);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry0);
		when(serviceInterfaceRepository.findById(eq(testId1))).thenReturn(serviceInterfaceEntry1);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry1);

		serviceRegistryDBService.updateServiceInterfaceById(testId0, testInterface1.toUpperCase());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceInterfaceByIdTestLeadingTrailingSpaceSensitivityOfUniqueKeyViolation() {
		final String testInterface0 = "testinterface0";
		final long testId0 = 0;
		final ServiceInterface serviceInterface0 = new ServiceInterface(testInterface0);
		serviceInterface0.setId(testId0);
		final Optional<ServiceInterface> serviceInterfaceEntry0 = Optional.of(serviceInterface0);
		final String testInterface1 = "testinterface1";
		final long testId1 = 1;
		final ServiceInterface serviceInterface1 = new ServiceInterface(testInterface1);
		serviceInterface1.setId(testId1);
		final Optional<ServiceInterface> serviceInterfaceEntry1 = Optional.of(serviceInterface1);

		when(interfaceNameVerifier.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceInterfaceRepository.findById(eq(testId0))).thenReturn(serviceInterfaceEntry0);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry0);
		when(serviceInterfaceRepository.findById(eq(testId1))).thenReturn(serviceInterfaceEntry1);
		when(serviceInterfaceRepository.findByInterfaceName(any())).thenReturn(serviceInterfaceEntry1);

		serviceRegistryDBService.updateServiceInterfaceById(testId0, "  " + testInterface1 + "  ");
	}

	//=================================================================================================
	// Tests of removeServiceInterfaceById

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void removeServiceInterfaceByIdTest() {

		when(serviceInterfaceRepository.existsById(anyLong())).thenReturn(false);

		try {

			serviceRegistryDBService.removeServiceInterfaceById(0);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("does not exist"));

			throw ex;
		}

	}
}
package eu.arrowhead.core.serviceregistry.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.serviceregistry.intf.ServiceInterfaceNameVerifier;

@RunWith(SpringRunner.class)
public class ServiceRegistryDBServiceServiceRegistryTest {
	
	//=================================================================================================
	// members
	@InjectMocks
	private ServiceRegistryDBService serviceRegistryDBService; 
	
	@Mock
	private ServiceRegistryRepository serviceRegistryRepository;
	
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Mock
	private SystemRepository systemRepository;
	
	@Mock
	private SSLProperties sslProperties;
	
	@Spy
	private ServiceInterfaceNameVerifier interfaceNameVerifier;
	
	private static final ServiceDefinition validTestServiceDefinition = new ServiceDefinition("validTestServiceDefinition");
	private static final System validTestProvider = new System("test_system", "localhost", 1234, null);
	private static final System validTestProviderWithAuthenticationInfo = new System("test_system", "localhost", 1234, "authenticationInfo");
	private static final String validTestMetadataStr = "key=value, key2=value2";
	private static final List<String> validTestInterfaces = Arrays.asList(new String[]{"HTTP-SECURE-JSON", "HTTP-SECURE-XML"});
	private static final List<String> inValidTestInterfaces = Arrays.asList(new String[]{"HTTP-NONSECURE-JSON", "HTTP-NONSECURE-XML"});
	private static final String validTestServiceUri = "testServiceUri";
	private static final ZonedDateTime validTestEndOFValidity = ZonedDateTime.parse("2112-06-30T12:30:40Z[UTC]");
	private static final long validId = 1;
	private static final long inValidId = -1;
	private static final long notPresentId = Long.MAX_VALUE;
	
	private static final ServiceRegistryRequestDTO SERVICE_REGISTRY_REQUEST_DTO = new ServiceRegistryRequestDTO();
	private static final ServiceRegistryRequestDTO SERVICE_REGISTRY_REQUEST_DTO_WITH_INVALID_ENDOFVALIDITY_FORMAT = new ServiceRegistryRequestDTO();
	private static final SystemRequestDTO SYSTEM_REQUEST_DTO = new SystemRequestDTO();
	static {
		SYSTEM_REQUEST_DTO.setSystemName("test_system");
		SYSTEM_REQUEST_DTO.setAddress("localhost");
		SYSTEM_REQUEST_DTO.setPort(1234);
		SYSTEM_REQUEST_DTO.setAuthenticationInfo(null);
	}
	static {
		SERVICE_REGISTRY_REQUEST_DTO.setServiceDefinition(validTestServiceDefinition.getServiceDefinition());
		SERVICE_REGISTRY_REQUEST_DTO.setProviderSystem(SYSTEM_REQUEST_DTO);
		SERVICE_REGISTRY_REQUEST_DTO.setEndOfValidity("21120620 12:00:00");
		
	}
	static {
		SERVICE_REGISTRY_REQUEST_DTO_WITH_INVALID_ENDOFVALIDITY_FORMAT.setServiceDefinition(validTestServiceDefinition.getServiceDefinition());
		SERVICE_REGISTRY_REQUEST_DTO_WITH_INVALID_ENDOFVALIDITY_FORMAT.setProviderSystem(SYSTEM_REQUEST_DTO);
		SERVICE_REGISTRY_REQUEST_DTO_WITH_INVALID_ENDOFVALIDITY_FORMAT.setEndOfValidity("2112-06-20T12:00:00.371+01:00[Europe/London]");
		
	}

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceRegistryEntryById
	
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceRegistryEntryByIdWithNotExistingId() {
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		serviceRegistryDBService.getServiceRegistryEntryById(-2);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceReqistryEntries
		
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceReqistryEntriesWithNotValidSortField() {
		serviceRegistryDBService.getServiceRegistryEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceReqistryEntriesByServiceDefintion
			
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceReqistryEntriesByServiceDefintionWithNotValidSortField() {
		serviceRegistryDBService.getServiceReqistryEntriesByServiceDefintion("testService", 0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceReqistryEntriesByServiceDefintionWithNotValidServiceDefinition() {
		when(serviceDefinitionRepository.findByServiceDefinition(any())).thenReturn(Optional.ofNullable(null));
		serviceRegistryDBService.getServiceReqistryEntriesByServiceDefintion("serviceNotExists", 0, 10, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
	}
			
	//-------------------------------------------------------------------------------------------------
	//Tests of removeServiceRegistryEntryById
		
	@Test(expected = InvalidParameterException.class)
	public void testRemoveServiceRegistryEntryByIdWithNotExistingId() {
		when(serviceRegistryRepository.existsById(anyLong())).thenReturn(false);
		serviceRegistryDBService.removeServiceRegistryEntryById(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of registerServiceResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseNullParam() {
		serviceRegistryDBService.registerServiceResponse(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseServiceDefinitionNull() {
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseServiceDefinitionEmpty() {
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition(" ");
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseProviderSystemNull() {
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition("service_definition");
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseProviderSystemNameNull() {
		final SystemRequestDTO sysDto = new SystemRequestDTO(); 
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition("service_definition");
		dto.setProviderSystem(sysDto);
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseProviderSystemNameEmpty() {
		final SystemRequestDTO sysDto = new SystemRequestDTO(); 
		sysDto.setSystemName(" ");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition("service_definition");
		dto.setProviderSystem(sysDto);
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseProviderSystemAddressNull() {
		final SystemRequestDTO sysDto = new SystemRequestDTO(); 
		sysDto.setSystemName("system");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition("service_definition");
		dto.setProviderSystem(sysDto);
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseProviderSystemAddressEmpty() {
		final SystemRequestDTO sysDto = new SystemRequestDTO(); 
		sysDto.setSystemName("system");
		sysDto.setAddress(" ");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition("service_definition");
		dto.setProviderSystem(sysDto);
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testRegisterServiceResponseProviderSystemPortNull() {
		final SystemRequestDTO sysDto = new SystemRequestDTO(); 
		sysDto.setSystemName("system");
		sysDto.setAddress("localhost");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition("service_definition");
		dto.setProviderSystem(sysDto);
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class) 
	public void testRegisterServiceResponseEndOfValidityInvalid() {
		final String systemName = "system";
		final String address = "localhost";
		final int port = 1111;
		final String serviceDefinitionStr = "service_definition";
		
		final SystemRequestDTO sysDto = new SystemRequestDTO(); 
		sysDto.setSystemName(systemName);
		sysDto.setAddress(address);
		sysDto.setPort(port);
		
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition(serviceDefinitionStr);
		dto.setProviderSystem(sysDto);
		dto.setEndOfValidity("not a ZoneDateTime");
		
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(new ServiceDefinition(serviceDefinitionStr)));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(new System(systemName, address, port, null)));
		
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of createServiceRegistry		
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateServiceRegistryServiceDefinitionNull() {
		serviceRegistryDBService.createServiceRegistry(null, null, null, null, null, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateServiceRegistryProviderNull() {
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), null, null, null, null, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateServiceRegistryUniqueConstraintViolation() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.of(new ServiceRegistry()));
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), new System(), null, null, null, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateServiceRegistrySecuredButAuthenticationInfoNotSpecified() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), new System(), null, null, ServiceSecurityType.CERTIFICATE, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateServiceRegistryTryingToRegisterSecuredServiceInInsecureMode() {
		final System provider = new System();
		provider.setAuthenticationInfo("abcd");
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		when(sslProperties.isSslEnabled()).thenReturn(false);
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), provider, null, null, ServiceSecurityType.CERTIFICATE, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateServiceRegistryInterfacesListNull() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), new System(), null, null, ServiceSecurityType.NOT_SECURE, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateServiceRegistryInterfacesListEmpty() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), new System(), null, null, ServiceSecurityType.NOT_SECURE, null, 1, Collections.<String>emptyList());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateServiceRegistryInvalidInterface() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.createServiceRegistry(new ServiceDefinition(), new System(), null, null, ServiceSecurityType.NOT_SECURE, null, 1, List.of("xml"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveServiceRegistryServiceDefinitionNull() {
		serviceRegistryDBService.removeServiceRegistry(null, null, null, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveServiceRegistryServiceDefinitionEmpty() {
		serviceRegistryDBService.removeServiceRegistry(" ", null, null, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveServiceRegistryProviderSystemNameNull() {
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", null, null, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveServiceRegistryProviderSystemNameEmpty() {
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", " ", null, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveServiceRegistryProviderSystemAddressNull() {
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", "System", null, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveServiceRegistryProviderSystemAddressEmpty() {
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", "System", " ", 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveServiceRegistryServiceDefinitionNotExists() {
		when(serviceDefinitionRepository.findByServiceDefinition("servicedefinition")).thenReturn(Optional.empty());
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", "System", "SystemAddress", 1); // also checks case insensitivity
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveServiceRegistryProviderSystemNotExists() {
		when(serviceDefinitionRepository.findByServiceDefinition("servicedefinition")).thenReturn(Optional.of(new ServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort("system", "systemaddress", 1)).thenReturn(Optional.empty());
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", "System", "SystemAddress", 1); // also checks case insensitivity
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveServiceRegistryEntryNotExists() {
		when(serviceDefinitionRepository.findByServiceDefinition("servicedefinition")).thenReturn(Optional.of(new ServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort("system", "systemaddress", 1)).thenReturn(Optional.of(new System()));
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.removeServiceRegistry("ServiceDefinition", "System", "SystemAddress", 1); // also checks case insensitivity
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryRegistryFormNull() {
		serviceRegistryDBService.queryRegistry(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryRegistryServiceDefinitionRequirementNull() {
		serviceRegistryDBService.queryRegistry(new ServiceQueryFormDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryRegistryServiceDefinitionRequirementEmpty() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("  ");
		
		serviceRegistryDBService.queryRegistry(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryServiceDefinitionRequirementNotFound() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.empty());
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(0, result.getUnfilteredHits());
		Assert.assertEquals(0, result.getServiceQueryData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryServiceDefinitionRequirementFoundButNoProvider() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(List.of());
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(0, result.getUnfilteredHits());
		Assert.assertEquals(0, result.getServiceQueryData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryNoAdditionalFilter() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(6, result.getUnfilteredHits());
		Assert.assertEquals(6, result.getServiceQueryData().size());
		for (int i = 0; i < result.getServiceQueryData().size(); ++i) {
			Assert.assertEquals(i + 1, result.getServiceQueryData().get(i).getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryInterfaceFilterRemovesOne() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		form.setInterfaceRequirements(List.of(" http-secure-json ")); // also tests normalizing interface requirements
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(6, result.getUnfilteredHits());
		Assert.assertEquals(5, result.getServiceQueryData().size());
		for (int i = 0; i < result.getServiceQueryData().size(); ++i) {
			Assert.assertEquals(i + 1, result.getServiceQueryData().get(i).getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistrySecurityTypeFilterRemovesAnother() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		form.setInterfaceRequirements(List.of(" http-secure-json ")); // also tests normalizing interface requirements
		form.setSecurityRequirements(List.of(ServiceSecurityType.NOT_SECURE));
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(6, result.getUnfilteredHits());
		Assert.assertEquals(4, result.getServiceQueryData().size());
		for (int i = 0; i < result.getServiceQueryData().size(); ++i) {
			Assert.assertEquals(i + 1, result.getServiceQueryData().get(i).getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryVersionExactFilterRemovesAnother() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		form.setInterfaceRequirements(List.of(" http-secure-json ")); // also tests normalizing interface requirements
		form.setSecurityRequirements(List.of(ServiceSecurityType.NOT_SECURE));
		form.setVersionRequirement(1);
		form.setMinVersionRequirement(4); // ignored because of exact version requirement
		form.setMaxVersionRequirement(10); // ignored because of exact version requirement
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(6, result.getUnfilteredHits());
		Assert.assertEquals(3, result.getServiceQueryData().size());
		for (int i = 0; i < result.getServiceQueryData().size(); ++i) {
			Assert.assertEquals(i + 1, result.getServiceQueryData().get(i).getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryVersionMinMaxFilterLeavesOnlyOne() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		form.setInterfaceRequirements(List.of(" http-secure-json ")); // also tests normalizing interface requirements
		form.setSecurityRequirements(List.of(ServiceSecurityType.NOT_SECURE));
		form.setMinVersionRequirement(4); // not ignored because there is no exact version requirement
		form.setMaxVersionRequirement(10); // not ignored because there is no exact version requirement
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(6, result.getUnfilteredHits());
		Assert.assertEquals(1, result.getServiceQueryData().size());
		Assert.assertEquals(4, result.getServiceQueryData().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testQueryRegistryInvalidMetadataFilter() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		form.setMetadataRequirements(Map.of("a", "1", " a ", "2"));
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		serviceRegistryDBService.queryRegistry(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryRegistryMetadataFilterRemovesAnother() {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testService");
		form.setInterfaceRequirements(List.of(" http-secure-json ")); // also tests normalizing interface requirements
		form.setSecurityRequirements(List.of(ServiceSecurityType.NOT_SECURE));
		form.setVersionRequirement(1);
		form.setMinVersionRequirement(4); // ignored because of exact version requirement
		form.setMaxVersionRequirement(10); // ignored because of exact version requirement
		form.setMetadataRequirements(Map.of(" key", "value   ")); // also test normalizing metadata requirements
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testservice");
		when(serviceDefinitionRepository.findByServiceDefinition("testservice")).thenReturn(Optional.of(serviceDefinition)); // also tests case insensitivity
		when(serviceRegistryRepository.findByServiceDefinition(any(ServiceDefinition.class))).thenReturn(getTestProviders(serviceDefinition));
		
		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		Assert.assertEquals(6, result.getUnfilteredHits());
		Assert.assertEquals(2, result.getServiceQueryData().size());
		for (int i = 0; i < result.getServiceQueryData().size(); ++i) {
			Assert.assertEquals(i + 1, result.getServiceQueryData().get(i).getId());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of updateServiceRegistry		
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryServiceDefinitionNull() {
		serviceRegistryDBService.updateServiceRegistry(
				getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				null,
				validTestProvider,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.NOT_SECURE,
				validTestMetadataStr,
				1,
				validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryProviderNull() {
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				validTestServiceDefinition,
				null,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.NOT_SECURE,
				validTestMetadataStr,
				1,
				validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateServiceRegistryUniqueConstraintViolation() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.of(new ServiceRegistry()));
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				validTestServiceDefinition,
				validTestProvider,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.NOT_SECURE,
				validTestMetadataStr,
				1,
				validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistrySecuredButAuthenticationInfoNotSpecified() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				validTestServiceDefinition,
				validTestProvider,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.CERTIFICATE,
				validTestMetadataStr,
				1,
				validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateServiceRegistryTryingToRegisterSecuredServiceInInsecureMode() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		when(sslProperties.isSslEnabled()).thenReturn(false);
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				validTestServiceDefinition,
				validTestProviderWithAuthenticationInfo,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.CERTIFICATE,
				validTestMetadataStr,
				1,
				validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryInterfacesListNull() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0), new ServiceDefinition(), new System(), null, null, ServiceSecurityType.NOT_SECURE, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryInterfacesListEmpty() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				validTestServiceDefinition,
				validTestProvider,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.NOT_SECURE,
				validTestMetadataStr,
				1,
				null);
		}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryInvalidInterface() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),
				validTestServiceDefinition,
				validTestProvider,
				validTestServiceUri,
				validTestEndOFValidity,
				ServiceSecurityType.NOT_SECURE,
				validTestMetadataStr,
				1,
				inValidTestInterfaces);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceByIdResponseInvalidId() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceRegistry()));
		serviceRegistryDBService.updateServiceByIdResponse(SERVICE_REGISTRY_REQUEST_DTO, inValidId);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected =  InvalidParameterException.class)
	public void testUpdateServiceByIdResponseNotPresentId() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.empty());
		serviceRegistryDBService.updateServiceByIdResponse(SERVICE_REGISTRY_REQUEST_DTO, notPresentId);
	}	
	
	//-------------------------------------------------------------------------------------------------
	//@Test(expected =  InvalidParameterException.class)
	//public void testUpdateServiceByIdResponseInvalidEndOfValidityFormat() {
	//	when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0)));
	//	serviceRegistryDBService.updateServiceRegistry(any(ServiceRegistry.class),
	//			any(ServiceDefinition.class),
	//			any(System.class),
	//			any(String.class),
	//			any(ZonedDateTime.class),
	//			any(ServiceSecurityType.class),
	//			any(String.class),
	//			anyInt(),
	//			any);
	//	serviceRegistryDBService.updateServiceByIdResponse( SERVICE_REGISTRY_REQUEST_DTO, validId);//SERVICE_REGISTRY_REQUEST_DTO_WITH_INVALID_ENDOFVALIDITY_FORMAT, validId);
	//}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistry> getTestProviders(final ServiceDefinition definition) {
		final List<ServiceRegistry> result = new ArrayList<ServiceRegistry>(6);
		
		final System provider = new System("test_system", "localhost", 1234, null);
		final String metadataStr = "key=value, key2=value2";
		
		final ServiceInterface jsonInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceInterface xmlInterface = new ServiceInterface("HTTP-SECURE-XML");
		
		final ServiceRegistry srEntry1 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry1.setId(1);
		final ServiceRegistryInterfaceConnection conn1 = new ServiceRegistryInterfaceConnection(srEntry1, jsonInterface); 
		srEntry1.setInterfaceConnections(Set.of(conn1));
		result.add(srEntry1);
		
		final ServiceRegistry srEntry2 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry2.setId(2);
		final ServiceRegistryInterfaceConnection conn2 = new ServiceRegistryInterfaceConnection(srEntry2, jsonInterface); 
		srEntry2.setInterfaceConnections(Set.of(conn2));
		result.add(srEntry2);
		
		final ServiceRegistry srEntry3 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, "abc=def", 1);
		srEntry3.setId(3);
		final ServiceRegistryInterfaceConnection conn3 = new ServiceRegistryInterfaceConnection(srEntry3, jsonInterface); 
		srEntry3.setInterfaceConnections(Set.of(conn3));
		result.add(srEntry3);
		
		final ServiceRegistry srEntry4 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 5);
		srEntry4.setId(4);
		final ServiceRegistryInterfaceConnection conn4 = new ServiceRegistryInterfaceConnection(srEntry4, jsonInterface); 
		srEntry4.setInterfaceConnections(Set.of(conn4));
		result.add(srEntry4);
		
		final ServiceRegistry srEntry5 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.CERTIFICATE, metadataStr, 1);
		srEntry5.setId(5);
		final ServiceRegistryInterfaceConnection conn5 = new ServiceRegistryInterfaceConnection(srEntry5, jsonInterface); 
		srEntry5.setInterfaceConnections(Set.of(conn5));
		result.add(srEntry5);
		
		final ServiceRegistry srEntry6 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry6.setId(6);
		final ServiceRegistryInterfaceConnection conn6 = new ServiceRegistryInterfaceConnection(srEntry6, xmlInterface); 
		srEntry6.setInterfaceConnections(Set.of(conn6));
		result.add(srEntry6);
		
		return result;
	}
}

/********************************************************************************
 * Copyright (c) 2019 AITIA
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

package eu.arrowhead.core.serviceregistry.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.intf.ServiceInterfaceNameVerifier;

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
	
	@Mock
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Mock
	private ServiceRegistryInterfaceConnectionRepository serviceRegistryInterfaceConnectionRepository;
	
	@Spy
	private ServiceInterfaceNameVerifier interfaceNameVerifier;

	private static final String validTestMetadataStr = "key=value, key2=value2";
	private static final String jsonInterFace = "HTTP-SECURE-JSON";
	private static final List<String> validTestInterfaces = Arrays.asList("HTTP-SECURE-JSON", "HTTP-SECURE-XML");
	private static final List<String> inValidTestInterfaces = Arrays.asList("HTTP-NONSECURE-JSON", "HTTP-NONSECURE-XML");
	private static final String validTestServiceUri = "testServiceUri";
	private static final ZonedDateTime validTestEndOFValidity = ZonedDateTime.parse("2112-06-30T12:30:40Z[UTC]");
	private static final String validTestEndOFValidityFormatForRequestDTO = "2112-06-20 12:00:00";
	private static final Map<String, String> validTestMetadataForRequestDTO = Map.of("meta1", "data1",
		    																		 "meta2", "data2");
	private static final long validId = 1;
	private static final long invalidId = -1;
	private static final long notPresentId = Long.MAX_VALUE;

	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of getServiceRegistryEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceRegistryEntryByIdWithNotExistingId() {
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		serviceRegistryDBService.getServiceRegistryEntryById(-2);
	}
	
	//=================================================================================================
	//Tests of getServiceRegistryEntries

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceRegistryEntriesWithNotValidSortField() {
		serviceRegistryDBService.getServiceRegistryEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//=================================================================================================
	// Tests of getServiceRegistryEntriesByServiceDefinition
			
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceRegistryEntriesByServiceDefinitionWithNotValidSortField() {
		serviceRegistryDBService.getServiceRegistryEntriesByServiceDefinition("testService", 0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetServiceRegistryEntriesByServiceDefinitionWithNotValidServiceDefinition() {
		when(serviceDefinitionRepository.findByServiceDefinition(any())).thenReturn(Optional.ofNullable(null));
		
		serviceRegistryDBService.getServiceRegistryEntriesByServiceDefinition("serviceNotExists", 0, 10, Direction.ASC, CoreCommonConstants.COMMON_FIELD_NAME_ID);
	}
			
	//=================================================================================================
	// Tests of removeServiceRegistryEntryById
		
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveServiceRegistryEntryByIdWithNotExistingId() {
		when(serviceRegistryRepository.existsById(anyLong())).thenReturn(false);
		
		serviceRegistryDBService.removeServiceRegistryEntryById(1);
	}
	
	//=================================================================================================
	// Tests of registerServiceResponse
	
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
	@Test(expected = InvalidParameterException.class) 
	public void testRegisterServiceResponseSecurityTypeInvalid() {
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
		dto.setSecure("invalidSecurityType");
		
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(new ServiceDefinition(serviceDefinitionStr)));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(new System(systemName, address, port, null)));
		
		serviceRegistryDBService.registerServiceResponse(dto);
	}
	
	//=================================================================================================
	// Tests of createServiceRegistry		
	
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
	
	//=================================================================================================
	// Tests of removeServiceRegistry

	
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
	
	//=================================================================================================
	// Tests of queryRegistry
	
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
	
	//=================================================================================================
	// Tests of updateServiceRegistry		
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryEntryNull() {
		serviceRegistryDBService.updateServiceRegistry(null, getValidTestServiceDefinition(), getValidTestProvider(), validTestServiceUri, validTestEndOFValidity, ServiceSecurityType.NOT_SECURE,
													   validTestMetadataStr, 1, validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryServiceDefinitionNull() {
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),	null, getValidTestProvider(), validTestServiceUri, 
													   validTestEndOFValidity, ServiceSecurityType.NOT_SECURE, validTestMetadataStr, 1, validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryProviderNull() {
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),	getValidTestServiceDefinition(), null, validTestServiceUri,
																		validTestEndOFValidity,	ServiceSecurityType.NOT_SECURE, validTestMetadataStr, 1, validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateServiceRegistryUniqueConstraintViolation() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.of(new ServiceRegistry()));
		
		serviceRegistryDBService.updateServiceRegistry(getTestProvidersWithIdsForUniqueConstrantCheck(new ServiceDefinition("testServiceDefinition")).get(0),
													   getTestProvidersWithIdsForUniqueConstrantCheck(new ServiceDefinition("testServiceDefinition")).get(0).getServiceDefinition(),
													   getValidTestProviderForUniqueConstraintCheck(), validTestServiceUri, validTestEndOFValidity, ServiceSecurityType.NOT_SECURE,
													   validTestMetadataStr, 1,	validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistrySecuredButAuthenticationInfoNotSpecified() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0), getValidTestServiceDefinition(), getValidTestProvider(),
													   validTestServiceUri, validTestEndOFValidity, ServiceSecurityType.CERTIFICATE, validTestMetadataStr, 1, validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateServiceRegistryTryingToRegisterSecuredServiceInInsecureMode() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		when(sslProperties.isSslEnabled()).thenReturn(false);
		
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0),	getValidTestServiceDefinition(),
													   getValidTestProviderWithAuthenticationInfo(), validTestServiceUri, validTestEndOFValidity, ServiceSecurityType.CERTIFICATE,
													   validTestMetadataStr, 1, validTestInterfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryInterfacesListNull() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0), new ServiceDefinition(), new System(), null, null,
													   ServiceSecurityType.NOT_SECURE, null, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryInterfacesListEmpty() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0), getValidTestServiceDefinition(), getValidTestProvider(),
													   validTestServiceUri,	validTestEndOFValidity,	ServiceSecurityType.NOT_SECURE,	validTestMetadataStr, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceRegistryInvalidInterface() {
		when(serviceRegistryRepository.findByServiceDefinitionAndSystem(any(ServiceDefinition.class), any(System.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.updateServiceRegistry(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0), getValidTestServiceDefinition(), getValidTestProvider(),
													   validTestServiceUri, validTestEndOFValidity, ServiceSecurityType.NOT_SECURE, validTestMetadataStr, 1, inValidTestInterfaces);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceByIdResponseInvalidId() {
		serviceRegistryDBService.updateServiceByIdResponse(invalidId, getValidServiceRegistryRequestDTO(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateServiceByIdResponseNullRequest() {
		serviceRegistryDBService.updateServiceByIdResponse(validId, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected =  InvalidParameterException.class)
	public void testUpdateServiceByIdResponseNotPresentId() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		serviceRegistryDBService.updateServiceByIdResponse(notPresentId, getValidServiceRegistryRequestDTO(new ServiceRegistryRequestDTO()));
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected =  InvalidParameterException.class)
	public void testUpdateServiceByIdResponseInvalidEndOfValidityFormat() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0)));

		serviceRegistryDBService.updateServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithInvalidEndOfValidityFormat(new ServiceRegistryRequestDTO()));
	}
	
	//=================================================================================================
	// Tests of mergeServiceByIdResponse

	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testMergeServiceByIdResponseInvalidId() {
		serviceRegistryDBService.mergeServiceByIdResponse(invalidId, getValidServiceRegistryRequestDTO(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testMergeServiceByIdResponseNullRequest() {
		serviceRegistryDBService.mergeServiceByIdResponse(validId, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected =  InvalidParameterException.class)
	public void testMergeServiceByIdResponseNotPresentId() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.empty());
		serviceRegistryDBService.mergeServiceByIdResponse(notPresentId, getValidServiceRegistryRequestDTO(new ServiceRegistryRequestDTO()));
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected =  InvalidParameterException.class)
	public void testMergeServiceByIdResponseInvalidEndOfValidityFormat() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProviders(new ServiceDefinition("testServiceDefinition")).get(0)));

		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithInvalidEndOfValidityFormat(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullServiceDefinition() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullServiceDefinition(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseEmptyServiceDefinition() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithEmptyServiceDefinition(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullProviderSystem() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullProviderSystem(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullEndOfValidity() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullEndOfValidity(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullInterfaces() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullInterfaces(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullMetadata() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullMetadata(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullServiceSecurityType() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullServiceSecurityType(new ServiceRegistryRequestDTO()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699")
	@Test
	public void testMergeServiceByIdResponseNullServiceUri() {
		when(serviceRegistryRepository.findById(anyLong())).thenReturn(Optional.of(getTestProvidersWithMofifyableCollections(new ServiceDefinition("testServiceDefinition")).get(0)));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.of(getValidTestServiceDefinition()));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(getValidTestProvider()));
		when(serviceRegistryRepository.saveAndFlush(any(ServiceRegistry.class))).thenReturn(getValidServiceRegistry(new ServiceRegistry()));
		when(serviceInterfaceRepository.findByInterfaceName(any(String.class))).thenReturn(Optional.empty());
		
		serviceRegistryDBService.mergeServiceByIdResponse(validId, getValidServiceRegistryRequestDTOWithNullServiceUri(new ServiceRegistryRequestDTO()));
	}
	
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
		srEntry1.setInterfaceConnections(new HashSet<>(Set.of(conn1)));
		result.add(srEntry1);
		
		final ServiceRegistry srEntry2 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry2.setId(2);
		final ServiceRegistryInterfaceConnection conn2 = new ServiceRegistryInterfaceConnection(srEntry2, jsonInterface); 
		srEntry2.setInterfaceConnections(new HashSet<>(Set.of(conn2)));
		result.add(srEntry2);
		
		final ServiceRegistry srEntry3 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, "abc=def", 1);
		srEntry3.setId(3);
		final ServiceRegistryInterfaceConnection conn3 = new ServiceRegistryInterfaceConnection(srEntry3, jsonInterface); 
		srEntry3.setInterfaceConnections(new HashSet<>(Set.of(conn3)));
		result.add(srEntry3);
		
		final ServiceRegistry srEntry4 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 5);
		srEntry4.setId(4);
		final ServiceRegistryInterfaceConnection conn4 = new ServiceRegistryInterfaceConnection(srEntry4, jsonInterface); 
		srEntry4.setInterfaceConnections(new HashSet<>(Set.of(conn4)));
		result.add(srEntry4);
		
		final ServiceRegistry srEntry5 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.CERTIFICATE, metadataStr, 1);
		srEntry5.setId(5);
		final ServiceRegistryInterfaceConnection conn5 = new ServiceRegistryInterfaceConnection(srEntry5, jsonInterface); 
		srEntry5.setInterfaceConnections(new HashSet<>(Set.of(conn5)));
		result.add(srEntry5);
		
		final ServiceRegistry srEntry6 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry6.setId(6);
		final ServiceRegistryInterfaceConnection conn6 = new ServiceRegistryInterfaceConnection(srEntry6, xmlInterface); 
		srEntry6.setInterfaceConnections(new HashSet<>(Set.of(conn6)));
		result.add(srEntry6);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistry> getTestProvidersWithIdsForUniqueConstrantCheck(final ServiceDefinition definition) {
		final List<ServiceRegistry> result = new ArrayList<ServiceRegistry>(6);
		
		definition.setId(1);
		final System provider = new System("test_system", "localhost", 1234, null);
		final String metadataStr = "key=value, key2=value2";
		
		
		final ServiceInterface jsonInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceInterface xmlInterface = new ServiceInterface("HTTP-SECURE-XML");
		
		provider.setId(1);
		final ServiceRegistry srEntry1 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry1.setId(1);
		final ServiceRegistryInterfaceConnection conn1 = new ServiceRegistryInterfaceConnection(srEntry1, jsonInterface); 
		srEntry1.setInterfaceConnections(Set.of(conn1));
		result.add(srEntry1);
		
		provider.setId(2);
		final ServiceRegistry srEntry2 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry2.setId(2);
		final ServiceRegistryInterfaceConnection conn2 = new ServiceRegistryInterfaceConnection(srEntry2, jsonInterface); 
		srEntry2.setInterfaceConnections(Set.of(conn2));
		result.add(srEntry2);
		
		provider.setId(3);
		final ServiceRegistry srEntry3 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, "abc=def", 1);
		srEntry3.setId(3);
		final ServiceRegistryInterfaceConnection conn3 = new ServiceRegistryInterfaceConnection(srEntry3, jsonInterface); 
		srEntry3.setInterfaceConnections(Set.of(conn3));
		result.add(srEntry3);
		
		provider.setId(4);
		final ServiceRegistry srEntry4 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 5);
		srEntry4.setId(4);
		final ServiceRegistryInterfaceConnection conn4 = new ServiceRegistryInterfaceConnection(srEntry4, jsonInterface); 
		srEntry4.setInterfaceConnections(Set.of(conn4));
		result.add(srEntry4);
		
		provider.setId(5);
		final ServiceRegistry srEntry5 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.CERTIFICATE, metadataStr, 1);
		srEntry5.setId(5);
		final ServiceRegistryInterfaceConnection conn5 = new ServiceRegistryInterfaceConnection(srEntry5, jsonInterface); 
		srEntry5.setInterfaceConnections(Set.of(conn5));
		result.add(srEntry5);
		
		provider.setId(6);
		final ServiceRegistry srEntry6 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry6.setId(6);
		final ServiceRegistryInterfaceConnection conn6 = new ServiceRegistryInterfaceConnection(srEntry6, xmlInterface); 
		srEntry6.setInterfaceConnections(Set.of(conn6));
		result.add(srEntry6);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistry> getTestProvidersWithMofifyableCollections(final ServiceDefinition definition) {
		final List<ServiceRegistry> result = new ArrayList<ServiceRegistry>(6);
		
		final System provider = new System("test_system", "localhost", 1234, null);
		final String metadataStr = "key=value, key2=value2";
		
		final ServiceInterface jsonInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceInterface xmlInterface = new ServiceInterface("HTTP-SECURE-XML");
		
		final ServiceRegistry srEntry1 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry1.setId(1);
		final ServiceRegistryInterfaceConnection conn1 = new ServiceRegistryInterfaceConnection(srEntry1, jsonInterface); 
		final Set<ServiceRegistryInterfaceConnection> connectionSet1 = new HashSet<>();
		connectionSet1.add(conn1);
		srEntry1.setInterfaceConnections(connectionSet1);
		result.add(srEntry1);
		
		final ServiceRegistry srEntry2 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry2.setId(2);
		final ServiceRegistryInterfaceConnection conn2 = new ServiceRegistryInterfaceConnection(srEntry2, jsonInterface); 
		final Set<ServiceRegistryInterfaceConnection> connectionSet2 = new HashSet<>();
		connectionSet2.add(conn2);
		srEntry2.setInterfaceConnections(connectionSet2);
		result.add(srEntry2);
		
		final ServiceRegistry srEntry3 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, "abc=def", 1);
		srEntry3.setId(3);
		final ServiceRegistryInterfaceConnection conn3 = new ServiceRegistryInterfaceConnection(srEntry3, jsonInterface); 
		final Set<ServiceRegistryInterfaceConnection> connectionSet3 = new HashSet<>();
		connectionSet3.add(conn3);
		srEntry3.setInterfaceConnections(connectionSet3);
		result.add(srEntry3);
		
		final ServiceRegistry srEntry4 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 5);
		srEntry4.setId(4);
		final ServiceRegistryInterfaceConnection conn4 = new ServiceRegistryInterfaceConnection(srEntry4, jsonInterface); 
		final Set<ServiceRegistryInterfaceConnection> connectionSet4 = new HashSet<>();
		connectionSet4.add(conn4);
		srEntry4.setInterfaceConnections(connectionSet4);
		result.add(srEntry4);
		
		final ServiceRegistry srEntry5 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.CERTIFICATE, metadataStr, 1);
		srEntry5.setId(5);
		final ServiceRegistryInterfaceConnection conn5 = new ServiceRegistryInterfaceConnection(srEntry5, jsonInterface);
		final Set<ServiceRegistryInterfaceConnection> connectionSet5 = new HashSet<>();
		connectionSet5.add(conn5);
		srEntry5.setInterfaceConnections(connectionSet5);
		result.add(srEntry5);
		
		final ServiceRegistry srEntry6 = new ServiceRegistry(definition, provider, null, null, ServiceSecurityType.NOT_SECURE, metadataStr, 1);
		srEntry6.setId(6);
		final ServiceRegistryInterfaceConnection conn6 = new ServiceRegistryInterfaceConnection(srEntry6, xmlInterface); 
		final Set<ServiceRegistryInterfaceConnection> connectionSet6 = new HashSet<>();
		connectionSet6.add(conn6);
		srEntry6.setInterfaceConnections(connectionSet6);
		result.add(srEntry6);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private static SystemRequestDTO getValidSystemReqestDTO(final SystemRequestDTO systemRequestDTO) {
		systemRequestDTO.setSystemName("test_system");
		systemRequestDTO.setAddress("localhost");
		systemRequestDTO.setPort(1234);
		systemRequestDTO.setAuthenticationInfo(null);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTO(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		final ServiceDefinition validTestServiceDefinition = new ServiceDefinition("validTestServiceDefinition");
		validTestServiceDefinition.setId(1);
		
		serviceRegistryRequestDTO.setServiceDefinition(validTestServiceDefinition.getServiceDefinition());
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithInvalidEndOfValidityFormat(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		final ServiceDefinition validTestServiceDefinition = new ServiceDefinition("validTestServiceDefinition");
		validTestServiceDefinition.setId(1);
		
		serviceRegistryRequestDTO.setServiceDefinition(validTestServiceDefinition.getServiceDefinition());
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity("2112-06-20T12:00:00.371+01:00[Europe/London]");
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullServiceDefinition(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition(null);
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullProviderSystem(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition("validTestServiceDefinition");
		serviceRegistryRequestDTO.setProviderSystem(null);
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullEndOfValidity(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition("validTestServiceDefinition");
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(null);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullInterfaces(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition("validTestServiceDefinition");
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(null);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullMetadata(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition("validTestServiceDefinition");
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(null);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullServiceSecurityType(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition("validTestServiceDefinition");
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(null);
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithNullServiceUri(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition("validTestServiceDefinition");
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(null);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistryRequestDTO getValidServiceRegistryRequestDTOWithEmptyServiceDefinition(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) {
		serviceRegistryRequestDTO.setServiceDefinition(" ");
		serviceRegistryRequestDTO.setProviderSystem(getValidSystemReqestDTO(new SystemRequestDTO()));
		serviceRegistryRequestDTO.setEndOfValidity(validTestEndOFValidityFormatForRequestDTO);
		serviceRegistryRequestDTO.setInterfaces(validTestInterfaces);	
		serviceRegistryRequestDTO.setMetadata(validTestMetadataForRequestDTO);
		serviceRegistryRequestDTO.setSecure(ServiceSecurityType.NOT_SECURE.name());
		serviceRegistryRequestDTO.setServiceUri(validTestServiceUri);
		serviceRegistryRequestDTO.setVersion(0);
		
		return serviceRegistryRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceRegistry getValidServiceRegistry(final ServiceRegistry serviceRegistry) {
		final ServiceRegistryInterfaceConnection validServiceRegistryInterfaceConnection = new ServiceRegistryInterfaceConnection( serviceRegistry, new ServiceInterface(jsonInterFace));

		final Set<ServiceRegistryInterfaceConnection> validTestIntefacesSet = new HashSet<>();
		validTestIntefacesSet.add(validServiceRegistryInterfaceConnection);	
		
		final ServiceDefinition validTestServiceDefinition = new ServiceDefinition("validTestServiceDefinition");
		validTestServiceDefinition.setId(1);
		
		serviceRegistry.setId(1);
		serviceRegistry.setServiceDefinition(validTestServiceDefinition);
		serviceRegistry.setSystem(getValidTestProvider());
		serviceRegistry.setEndOfValidity(validTestEndOFValidity);
		serviceRegistry.setInterfaceConnections(validTestIntefacesSet);	
		serviceRegistry.setMetadata(validTestMetadataStr);
		serviceRegistry.setSecure(ServiceSecurityType.NOT_SECURE);
		serviceRegistry.setServiceUri(validTestServiceUri);
		serviceRegistry.setVersion(0);
		
		return serviceRegistry;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceDefinition getValidTestServiceDefinition() {
		final ServiceDefinition validTestServiceDefinition = new ServiceDefinition("validTestServiceDefinition");
		validTestServiceDefinition.setId(1);
		
		return validTestServiceDefinition;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static System getValidTestProvider() {
		final System system = new System("test_system", "localhost", 1234, null);
		system.setId(1);
		
		return system;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static System getValidTestProviderForUniqueConstraintCheck() {
		final System system = new System("test_system", "localhost", 1234, null);
		system.setId(Integer.MAX_VALUE);
		
		return system;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static System getValidTestProviderWithAuthenticationInfo() {
		final System system = new System("test_system", "localhost", 1234, null);
		system.setId(1);
		system.setAuthenticationInfo("authenticationInfo");
		
		return system;
	}
}
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

package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.AutoCompleteDataResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.IdValueDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryGroupedResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.ServicesGroupedByServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.internal.ServicesGroupedBySystemsResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration(classes = { ServiceRegistryDBServiceTestContext.class })
public class ServiceRegistryControllerServiceRegistryTest {
	
	//=================================================================================================
	// members
	
	private static final String SERVICE_REGISTRY_REGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
	private static final String SERVICE_REGISTRY_UNREGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
	private static final String SERVICE_REGISTRY_QUERY_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI;
	private static final String SERVICE_REGISTRY_QUERY_SYSTEM_BY_ID_URI = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI;
	private static final String SERVICE_REGISTRY_QUERY_SYSTEM_BY_DTO_URI = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI;
	private static final String SERVICE_REGISTRY_QUERY_ALL_URI = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_ALL_SERVICE_URI;

	private static final String SERVICE_REGISTRY_MGMT_URI = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.MGMT_URI;
	private static final String SERVICE_REGISTRY_MGMT_SERVICEDEF_URI = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.MGMT_URI + "/servicedef";
	private static final String SERVICE_REGISTRY_MGMT_GROUPED_URI = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.MGMT_URI + "/grouped";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockServiceRegistryDBService") 
	private ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	//=================================================================================================
	// Test of getServiceRegistryEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesWithoutParameter() throws Exception {
		final int numOfServices = 4;
		final int numOfSystems = 3;		
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems, "JSON", "XML");
		final ServiceRegistryListResponseDTO serviceRegistryEntriesDTO = DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntries);

		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceRegistryEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final ServiceRegistryListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ServiceRegistryListResponseDTO.class);
		
		assertEquals(numOfServices * numOfSystems, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesWithPageAndSizeParameter() throws Exception {
		final int numOfServices = 4;
		final int numOfSystems = 3;		
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems, "JSON", "XML");
		final ServiceRegistryListResponseDTO serviceRegistryEntriesDTO = DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntries);
		
		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceRegistryEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI)
											   .param("page", "0")
											   .param("item_per_page", String.valueOf(numOfServices * numOfSystems))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final ServiceRegistryListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ServiceRegistryListResponseDTO.class);
		
		assertEquals(numOfServices * numOfSystems, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI)
					.param("item_per_page", "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testGetServiceRegistryEntriesWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI)
					.param("page", "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI)
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of getServiceRegistryEntriesById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByIdWithExistingId() throws Exception {
		final Page<ServiceRegistry> page = createServiceRegistryPageForDBMocking(1, 1, "JSON", "XML");
		final ServiceRegistryResponseDTO dto = DTOConverter.convertServiceRegistryToServiceRegistryResponseDTO(page.getContent().get(0));
		
		when(serviceRegistryDBService.getServiceRegistryEntryByIdResponse(anyLong())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI + "/1")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final ServiceRegistryResponseDTO readValue = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ServiceRegistryResponseDTO.class);
		
		assertEquals(1, readValue.getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of getServiceRegistryEntriesByServiceDefinition
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithOnlyServiceDefInput() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SERVICEDEF_URI + "/testDef")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithEmptyServiceDefInput() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SERVICEDEF_URI + "/ ")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithServiceDefInputAndNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SERVICEDEF_URI + "/testDef")
					.param("item_per_page", "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithServiceDefInputAndDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SERVICEDEF_URI + "/testDef")
					.param("page", "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithServiceDefInputAndInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SERVICEDEF_URI + "/testDef")
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of getServiceRegistryGroupedData
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryGroupedDataToCheckDTO() throws Exception {
		final int numOfServices = 4;
		final int numOfSystems = 2;		
		final String interface1 = "JSON";
		final String interface2 = "XML";
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems, interface1, interface2);
		final Map<Long,ServiceDefinition> serviceDefinitionEntries = new HashMap<>();
		final Map<Long,System> systemEntries = new HashMap<>();
		final Map<Long,ServiceInterface> interfaceEntries = new HashMap<>();
		for (final ServiceRegistry srEntry : serviceRegistryEntries) {
			serviceDefinitionEntries.putIfAbsent(srEntry.getServiceDefinition().getId(), srEntry.getServiceDefinition());
			systemEntries.putIfAbsent(srEntry.getSystem().getId(), srEntry.getSystem());
			for (final ServiceRegistryInterfaceConnection interfaceConn : srEntry.getInterfaceConnections()) {
				interfaceEntries.putIfAbsent(interfaceConn.getServiceInterface().getId(), interfaceConn.getServiceInterface());
			}
		}		
		
		final ServiceRegistryGroupedResponseDTO dto = DTOConverter.convertServiceRegistryDataToServiceRegistryGroupedResponseDTO(serviceDefinitionEntries.values(), systemEntries.values(),
																																 interfaceEntries.values(), serviceRegistryEntries);
		
		when(serviceRegistryDBService.getServiceRegistryDataForServiceRegistryGroupedResponse()).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_GROUPED_URI)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final ServiceRegistryGroupedResponseDTO readValue = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ServiceRegistryGroupedResponseDTO.class);
		
		final AutoCompleteDataResponseDTO autoCompleteData = readValue.getAutoCompleteData();
		final List<ServicesGroupedBySystemsResponseDTO> servicesGroupedBySystems = readValue.getServicesGroupedBySystems();
		final List<ServicesGroupedByServiceDefinitionResponseDTO> servicesGroupedByServiceDefinition = readValue.getServicesGroupedByServiceDefinition();
		assertNotNull(autoCompleteData);
		assertNotNull(servicesGroupedBySystems);
		assertNotNull(servicesGroupedByServiceDefinition);
		
		// Testing autoCompleteData object
		final List<IdValueDTO> interfaceList = autoCompleteData.getInterfaceList();
		assertEquals(2, interfaceList.size());
		assertEquals(numOfServices, autoCompleteData.getServiceList().size());
		assertEquals(numOfSystems, autoCompleteData.getSystemList().size());
		
		// Testing servicesGroupedBySystems object
		assertEquals(numOfSystems, servicesGroupedBySystems.size());
		assertEquals(numOfServices, servicesGroupedBySystems.get(0).getServices().size());
		final String oneOfTheInterfaces = servicesGroupedBySystems.get(0).getServices().get(0).getInterfaces().get(0).getInterfaceName();
		assertTrue(oneOfTheInterfaces.equals(interface1) || oneOfTheInterfaces.equals(interface2) ? true :false);
		
		// Testing servicesGroupedByServiceDefinition object
		assertEquals(numOfServices, servicesGroupedByServiceDefinition.size());
		assertEquals(numOfSystems, servicesGroupedByServiceDefinition.get(0).getProviderServices().size());
	}
	
	//=================================================================================================
	// Tests of removeServiceRegistryEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveServiceRegistryEntryByIdWithExistingId() throws Exception {
		this.mockMvc.perform(delete(SERVICE_REGISTRY_MGMT_URI + "/4")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveServiceRegistryEntryByIdWithNotExistingId() throws Exception {
		this.mockMvc.perform(delete(SERVICE_REGISTRY_MGMT_URI + "/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of registerService
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceServiceDefinitionNull() throws Exception {
		final MvcResult result = postRegisterService(new ServiceRegistryRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);

		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceServiceDefinitionEmpty() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition(" ");
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	// System request DTO validation is tested by an other test class
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceEndOfValidityInvalid() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("invalid date");
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("End of validity is specified in the wrong format. Please provide UTC time using " + Utilities.getDatetimePattern() + " pattern.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceSecuredButWithoutAuthenticationInfo() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setSecure(ServiceSecurityType.CERTIFICATE.name());
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Security type is in conflict with the availability of the authentication info.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceWithInvalidSecureValue() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setSecure("invalidSecurityTypeValue");
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Security type is not valid.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceInterfaceListNull() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Interfaces list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceInterfaceListEmpty() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(Collections.<String>emptyList());
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Interfaces list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceInterfaceInvalid() throws Exception {
		final String intf = "XML";
		
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of(intf));
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Specified interface name is not valid: " + intf, error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceEverythingIsOk() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		when(serviceRegistryDBService.registerServiceResponse(request)).thenReturn(new ServiceRegistryResponseDTO());
		
		postRegisterService(request, status().isCreated());
	}
	
	//=================================================================================================
	// Tests of unregisterService
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceNoParameter() throws Exception {
		deleteUnregisterService(null, status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceNoServiceDefinitionParameter() throws Exception {
		final String queryStr = createQueryStringForUnregister(null, "x", "a", 1);
		
		deleteUnregisterService(queryStr, status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceNoSystemNameParameter() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", null, "a", 1);
		
		deleteUnregisterService(queryStr, status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceNoAddressParameter() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", null, 1);
		
		deleteUnregisterService(queryStr, status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceNoPortParameter() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", "a", null);
		
		deleteUnregisterService(queryStr, status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceServiceDefinitionEmpty() throws Exception {
		final String queryStr = createQueryStringForUnregister("", "x", "a", 1);
		
		final MvcResult result = deleteUnregisterService(queryStr, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_UNREGISTER_URI, error.getOrigin());
		Assert.assertEquals("Service definition is blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceSystemNameEmpty() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "", "a", 1);
		
		final MvcResult result = deleteUnregisterService(queryStr, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_UNREGISTER_URI, error.getOrigin());
		Assert.assertEquals("Name of the provider system is blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceAddressEmpty() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", "", 1);
		
		final MvcResult result = deleteUnregisterService(queryStr, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_UNREGISTER_URI, error.getOrigin());
		Assert.assertEquals("Address of the provider system is blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServicePortNumberTooLow() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", "a", -1);
		
		final MvcResult result = deleteUnregisterService(queryStr, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_UNREGISTER_URI, error.getOrigin());
		Assert.assertEquals("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServicePortNumberTooHigh() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", "a", 66000);
		
		final MvcResult result = deleteUnregisterService(queryStr, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_UNREGISTER_URI, error.getOrigin());
		Assert.assertEquals("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceEverythingIsOk() throws Exception {
		final String queryStr = createQueryStringForUnregister("s", "x", "a", 1);
		
		doNothing().when(serviceRegistryDBService).removeServiceRegistry(any(String.class), any(String.class), any(String.class), anyInt());
		
		deleteUnregisterService(queryStr, status().isOk());
	}
	
	//=================================================================================================
	// Tests of queryService
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceServiceDefinitionRequirementNull() throws Exception {
		final MvcResult result = postQueryService(new ServiceQueryFormDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_URI, error.getOrigin());
		Assert.assertEquals("Service definition requirement is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceServiceDefinitionRequirementEmpty() throws Exception {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("  ");
		
		final MvcResult result = postQueryService(form, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_URI, error.getOrigin());
		Assert.assertEquals("Service definition requirement is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryServiceEverythingIsOk() throws Exception {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("testservice");
		
		when(serviceRegistryDBService.queryRegistry(any(ServiceQueryFormDTO.class))).thenReturn(new ServiceQueryResultDTO());
		
		postQueryService(form, status().isOk());
	}
	
	//=================================================================================================
	// Tests of queryRegistryBySystemId
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemIdOk() throws Exception {
		when(serviceRegistryDBService.getSystemById(anyLong())).thenReturn(new SystemResponseDTO());
		getQuerySystemsById(1l, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemIdInvalidID() throws Exception {
		when(serviceRegistryDBService.getSystemById(anyLong())).thenReturn(new SystemResponseDTO());
		final MvcResult result = getQuerySystemsById(-1l, status().isBadRequest());
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_SYSTEM_BY_ID_URI, error.getOrigin());
	}
	
	//=================================================================================================
	// Tests of queryRegistryBySystemDTO
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTOOk() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		postQuerySystemsByDTO(new SystemRequestDTO("name", "0.0.0.0", 45000, null), status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTONullSysName() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		final MvcResult result = postQuerySystemsByDTO(new SystemRequestDTO(null, "0.0.0.0", 45000, null), status().isBadRequest());
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_SYSTEM_BY_DTO_URI, error.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTONullAddress() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		final MvcResult result = postQuerySystemsByDTO(new SystemRequestDTO("name", null, 45000, null), status().isBadRequest());
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_SYSTEM_BY_DTO_URI, error.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTONullPort() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		final MvcResult result = postQuerySystemsByDTO(new SystemRequestDTO("name", "0.0.0.0", null, null), status().isBadRequest());
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_SYSTEM_BY_DTO_URI, error.getOrigin());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTOInvalidPort() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		final MvcResult result = postQuerySystemsByDTO(new SystemRequestDTO("name", "0.0.0.0", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, null), status().isBadRequest());
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_QUERY_SYSTEM_BY_DTO_URI, error.getOrigin());
	}
	
	//=================================================================================================
	// Tests of getServiceRegistryEntries
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testGetServiceRegistryEntriesOk() throws Exception {
		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(new ServiceRegistryListResponseDTO());
		getQueryAll(status().isOk());
	}
	
	//=================================================================================================
	// Tests of addService
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceServiceDefinitionNull() throws Exception {
		final MvcResult result = addServiceRegistry(new ServiceRegistryRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceServiceDefinitionEmpty() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition(" ");
		
		final MvcResult result = addServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	// System request DTO validation is tested by an other test class
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceEndOfValidityInvalid() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("invalid date");
		
		final MvcResult result = addServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("End of validity is specified in the wrong format. Please provide UTC time using " + Utilities.getDatetimePattern() + " pattern.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceSecuredButWithoutAuthenticationInfo() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setSecure(ServiceSecurityType.CERTIFICATE.name());
		
		final MvcResult result = addServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Security type is in conflict with the availability of the authentication info.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceInterfaceListNull() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		
		final MvcResult result = addServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Interfaces list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceInterfaceListEmpty() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(Collections.<String>emptyList());
		
		final MvcResult result = addServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Interfaces list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceInterfaceInvalid() throws Exception {
		final String intf = "XML";
		
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of(intf));
		
		final MvcResult result = addServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Specified interface name is not valid: " + intf, error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceEverythingIsOk() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		when(serviceRegistryDBService.registerServiceResponse(request)).thenReturn(new ServiceRegistryResponseDTO());
		
		addServiceRegistry(request, status().isCreated());
	}
	
	//=================================================================================================
	// Tests of updateService
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceInValidId() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		final MvcResult result = updateServiceRegistryWithInValidId(new ServiceRegistryRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Id must be greater than 0. ", error.getErrorMessage());
	}
	
	@Test
	public void testUpdateServiceServiceDefinitionNull() throws Exception {
		final MvcResult result = updateServiceRegistry(new ServiceRegistryRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceServiceDefinitionEmpty() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition(" ");
		
		final MvcResult result = updateServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	// System request DTO validation is tested by an other test class
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceEndOfValidityInvalid() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("invalid date");
		
		final MvcResult result = updateServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("End of validity is specified in the wrong format. Please provide UTC time using " + Utilities.getDatetimePattern() + " pattern.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceSecuredButWithoutAuthenticationInfo() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setSecure(ServiceSecurityType.CERTIFICATE.name());
		
		final MvcResult result = updateServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Security type is in conflict with the availability of the authentication info.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceInterfaceListNull() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		
		final MvcResult result = updateServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Interfaces list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceInterfaceListEmpty() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(Collections.<String>emptyList());
		
		final MvcResult result = updateServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Interfaces list is null or empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceInterfaceInvalid() throws Exception {
		final String intf = "XML";
		
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of(intf));
		
		final MvcResult result = updateServiceRegistry(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Specified interface name is not valid: " + intf, error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceEverythingIsOk() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		when(serviceRegistryDBService.updateServiceByIdResponse(anyLong(), any(ServiceRegistryRequestDTO.class))).thenReturn(new ServiceRegistryResponseDTO());
		
		updateServiceRegistry(request, status().isOk());
	}
	
	//=================================================================================================
	// Tests of mergeService
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMergeServiceInvalidId() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		final MvcResult result = mergeServiceRegistryWithInValidId(new ServiceRegistryRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Id must be greater than 0. ", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMergeServiceEmptyRequest() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition(" ");
		
		final SystemRequestDTO provider = getAValidSystemRequestDTO();
		provider.setSystemName(" ");
		provider.setAddress(" ");
		provider.setPort(null);
		provider.setAuthenticationInfo(null);
		
		request.setProviderSystem(provider);
		request.setEndOfValidity(null);
		request.setInterfaces(null);
		
		final MvcResult result = mergeServiceRegistry(new ServiceRegistryRequestDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CoreCommonConstants.MGMT_URI, error.getOrigin());
		Assert.assertEquals("Patch request is empty.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMergeServiceEverythingIsOk() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12 13:51:30");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setSystemName("testSystem");
		final ServiceRegistryResponseDTO response = new ServiceRegistryResponseDTO();
		response.setProvider(provider);
		
		when(serviceRegistryDBService.mergeServiceByIdResponse(anyLong(), any(ServiceRegistryRequestDTO.class))).thenReturn(response);
		
		mergeServiceRegistry(request, status().isOk());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private MvcResult postRegisterService(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICE_REGISTRY_REGISTER_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult addServiceRegistry(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICE_REGISTRY_MGMT_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult updateServiceRegistry(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		final long validServiceRegistryId = 1;
		
		return this.mockMvc.perform(put(SERVICE_REGISTRY_MGMT_URI + "/" + validServiceRegistryId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult updateServiceRegistryWithInValidId(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		final long inValidServiceRegistryId = -1;
		
		return this.mockMvc.perform(put(SERVICE_REGISTRY_MGMT_URI + "/" + inValidServiceRegistryId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult mergeServiceRegistryWithInValidId(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		final long inValidServiceRegistryId = -1;
		
		return this.mockMvc.perform(patch(SERVICE_REGISTRY_MGMT_URI + "/" + inValidServiceRegistryId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult mergeServiceRegistry(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		final long validServiceRegistryId = 1;
		
		return this.mockMvc.perform(patch(SERVICE_REGISTRY_MGMT_URI + "/" + validServiceRegistryId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult deleteUnregisterService(final String queryStr, final ResultMatcher matcher) throws Exception {
		final String validatedQueryStr = Utilities.isEmpty(queryStr) ? "" : "?" + queryStr.trim();
		return this.mockMvc.perform(delete(SERVICE_REGISTRY_UNREGISTER_URI + validatedQueryStr)
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postQueryService(final ServiceQueryFormDTO form, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICE_REGISTRY_QUERY_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(form))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult getQuerySystemsById(final long id, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(get(SERVICE_REGISTRY_QUERY_SYSTEM_BY_ID_URI.replace("{id}", String.valueOf(id)))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postQuerySystemsByDTO(final SystemRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICE_REGISTRY_QUERY_SYSTEM_BY_DTO_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult getQueryAll(final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(get(SERVICE_REGISTRY_QUERY_ALL_URI)
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getAValidSystemRequestDTO() {
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName("x");
		result.setAddress("localhost");
		result.setPort(1234);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Page<ServiceRegistry> createServiceRegistryPageForDBMocking(final int amountOfServiceDefinition, final int amountOfSystem, final String interface1Name, final String interface2Name) {
		final ZonedDateTime timeStamp = ZonedDateTime.now();
		
		final List<System> systemList = new ArrayList<>();
		final List<ServiceDefinition> serviceRegistryDefinitionList = new ArrayList<>();
		final List<ServiceRegistry> serviceRegistryList = new ArrayList<>();
		
		final ServiceInterface serviceInterface1 = new ServiceInterface(interface1Name);
		serviceInterface1.setId(1);
		serviceInterface1.setCreatedAt(timeStamp);
		serviceInterface1.setUpdatedAt(timeStamp);
		
		final ServiceInterface serviceInterface2 = new ServiceInterface(interface2Name);
		serviceInterface2.setId(2);
		serviceInterface2.setCreatedAt(timeStamp);
		serviceInterface2.setUpdatedAt(timeStamp);
		
		for (int i = 1; i <= amountOfServiceDefinition; ++i) {				
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService" + i);
			serviceDefinition.setId(i);
			serviceDefinition.setCreatedAt(timeStamp);
			serviceDefinition.setUpdatedAt(timeStamp);
			serviceRegistryDefinitionList.add(serviceDefinition);
		}
		
		for (int i = 1; i <= amountOfSystem; ++i) {										
			final System system = new System("testSystem" + i, "testAddress" + i, i * 1000, null);
			system.setId(i);
			system.setCreatedAt(timeStamp);
			system.setUpdatedAt(timeStamp);			
			systemList.add(system);
		}
		
		for (int i = 1; i <= amountOfServiceDefinition; ++i) {
			final ServiceDefinition serviceDefinition = serviceRegistryDefinitionList.get(i - 1);
			for (int j = 1; j <= amountOfSystem; ++j) {
				final System system = systemList.get(j - 1);
				final ServiceRegistry serviceRegistry = new ServiceRegistry(serviceDefinition, system, "testUri" + i, null, ServiceSecurityType.NOT_SECURE, "testMeta : testData", 0);
				serviceRegistry.setId(i);
				serviceRegistry.setCreatedAt(timeStamp);
				serviceRegistry.setUpdatedAt(timeStamp);
				serviceRegistry.setInterfaceConnections(new HashSet<>());
				serviceRegistry.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(serviceRegistry, serviceInterface1));
				serviceRegistry.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(serviceRegistry, serviceInterface2));
				serviceRegistryList.add(serviceRegistry);
			}
		}
		
		final Page<ServiceRegistry> entries = new PageImpl<ServiceRegistry>(serviceRegistryList);
		
		return entries;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String createQueryStringForUnregister(final String serviceDefinition, final String providerName, final String providerAddress, final Integer providerPort) {
		final StringBuilder sb = new StringBuilder();
		
		if (serviceDefinition != null) {
			sb.append(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION).append("=").append(serviceDefinition).append("&");
		}
		
		if (providerName != null) {
			sb.append(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME).append("=").append(providerName).append("&");
		}
		
		if (providerAddress != null) {
			sb.append(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS).append("=").append(providerAddress).append("&");
		}
		
		if (providerPort != null) {
			sb.append(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT).append("=").append(providerPort.intValue()).append("&");
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}
}
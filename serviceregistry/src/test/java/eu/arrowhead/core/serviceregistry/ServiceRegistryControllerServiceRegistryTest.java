package eu.arrowhead.core.serviceregistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.AutoCompleteDataResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.dto.IdValueDTO;
import eu.arrowhead.common.dto.ServiceRegistryGrouppedResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServicesGrouppedBySystemsResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration(classes = { ServiceRegistryDBSerrviceTestContext.class })
public class ServiceRegistryControllerServiceRegistryTest {
	
	//=================================================================================================
	// members
	
	private static final String SERVICE_REGISTRY_REGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
	private static final String SERVICE_REGISTRY_UNREGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;

	private static final String SERVICEREGISTRY_REGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
	private static final String SERVICEREGISTRY_REGISTER_MGMT_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.MGMT_URI;
	private static final String SERVICEREGISTRY_REGISTER_MGMT_SERVICEDEF_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.MGMT_URI + "/servicedef";
	private static final String SERVICEREGISTRY_REGISTER_MGMT_GROUPPED_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.MGMT_URI + "/groupped";
	
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
		Assert.assertEquals("End of validity is specified in the wrong format. See java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME for details.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceSecuredButWithoutAuthenticationInfo() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12T15:51:30+02:00[Europe/Budapest]");
		request.setSecure(ServiceSecurityType.CERTIFICATE);
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Security type is in conflict with the availability of the authentication info.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceNotSecuredButWithAuthenticationInfo() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.getProviderSystem().setAuthenticationInfo("1234");
		request.setEndOfValidity("2019-06-12T15:51:30+02:00[Europe/Budapest]");
		
		final MvcResult result = postRegisterService(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(SERVICE_REGISTRY_REGISTER_URI, error.getOrigin());
		Assert.assertEquals("Security type is in conflict with the availability of the authentication info.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceInterfaceListNull() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12T15:51:30+02:00[Europe/Budapest]");
		
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
		request.setEndOfValidity("2019-06-12T15:51:30+01:00[Europe/Budapest]");
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
		request.setEndOfValidity("2019-06-12T15:51:30+02:00[Europe/Budapest]");
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
		request.setEndOfValidity("2019-06-12T15:51:30+02:00[Europe/Budapest]");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		when(serviceRegistryDBService.registerServiceResponse(request)).thenReturn(new ServiceRegistryResponseDTO());
		
		postRegisterService(request, status().isCreated());
	}
	
	//=================================================================================================
	// Test of getServiceRegistryEntries
	
	@Test
	public void testGetServiceRegistryEntriesWithoutParameter() throws Exception {
		final int numOfServices = 4;
		final int numOfSystems = 3;		
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems, "JSON", "XML");
		final ServiceRegistryListResponseDTO serviceRegistryEntriesDTO = DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntries);
		when(serviceRegistryDBService.getServiceReqistryEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceRegistryEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_URI)
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
		when(serviceRegistryDBService.getServiceReqistryEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceRegistryEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_URI)
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
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_URI)
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testGetServiceRegistryEntriesWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_URI)
				.param("page", "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_URI)
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Test of getServiceRegistryEntriesByServiceDefinition
	
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithOnlyServiceDefInput() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_SERVICEDEF_URI)
				.param("service_definition", "test")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithoutServiceDefInput() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_SERVICEDEF_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithServiceDefInputAndNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_SERVICEDEF_URI)
				.param("service_definition", "test")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithServiceDefInputAndDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_SERVICEDEF_URI)
				.param("service_definition", "test")
				.param("page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesByServiceDefinitionWithServiceDefInputAndInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_SERVICEDEF_URI)
				.param("service_definition", "test")
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceRegistryGrouppedData
	
	@Test
	public void testGetServiceRegistryGrouppedDataToCheckDTO() throws Exception {
		final int numOfServices = 4;
		final int numOfSystems = 2;		
		final String interface1 = "JSON";
		final String interface2 = "XML";
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems, interface1, interface2);
		final ServiceRegistryGrouppedResponseDTO dto = DTOConverter.convertServiceRegistryEntriesToServiceRegistryGrouppedResponseDTO(serviceRegistryEntries);
		when(serviceRegistryDBService.getServiceReqistryEntriesForServiceRegistryGrouppedResponse()).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(SERVICEREGISTRY_REGISTER_MGMT_GROUPPED_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceRegistryGrouppedResponseDTO readValue = objectMapper.readValue(response.getResponse().getContentAsByteArray(), ServiceRegistryGrouppedResponseDTO.class);
		final AutoCompleteDataResponseDTO autoCompleteData = readValue.getAutoCompleteData();
		final List<ServicesGrouppedBySystemsResponseDTO> servicesGrouppedBySystems = readValue.getServicesGrouppedBySystems();
		final List<ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO> servicesGrouppedByServiceDefinitionAndInterface = readValue.getServicesGrouppedByServiceDefinitionAndInterface();
		
		assertNotNull(autoCompleteData);
		assertNotNull(servicesGrouppedBySystems);
		assertNotNull(servicesGrouppedByServiceDefinitionAndInterface);
		
		//Testing autoCompleteData object
		final List<IdValueDTO> interfaceList = autoCompleteData.getInterfaceList();
		assertEquals(2, interfaceList.size());
		assertTrue(interfaceList.get(0).getValue().equals(interface1) || interfaceList.get(1).getValue().equals(interface1) ? true : false);
		assertTrue(interfaceList.get(0).getValue().equals(interface2) || interfaceList.get(1).getValue().equals(interface2) ? true : false);		
		assertEquals(numOfServices, autoCompleteData.getServiceList().size());
		assertEquals(numOfSystems, autoCompleteData.getSystemList().size());
		
		//Testing servicesGrouppedBySystems object
		assertEquals(numOfSystems, servicesGrouppedBySystems.size());
		assertEquals(numOfServices, servicesGrouppedBySystems.get(0).getServices().size());
		final String oneOfTheInterfaces = servicesGrouppedBySystems.get(0).getServices().get(0).getInterfaces().get(0).getInterfaceName();
		assertTrue(oneOfTheInterfaces.equals(interface1) || oneOfTheInterfaces.equals(interface2) ? true :false);
		
		//Testing servicesGrouppedByServiceDefinitionAndInterface object
		assertEquals(numOfServices * 2, servicesGrouppedByServiceDefinitionAndInterface.size());
		assertEquals(numOfSystems, servicesGrouppedByServiceDefinitionAndInterface.get(0).getProviderServices().size());
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
	private MvcResult deleteUnregisterService(final String queryStr, final ResultMatcher matcher) throws Exception {
		final String validatedQueryStr = Utilities.isEmpty(queryStr) ? "" : "?" + queryStr.trim();
		return this.mockMvc.perform(delete(SERVICE_REGISTRY_UNREGISTER_URI + validatedQueryStr)
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
	private Page<ServiceRegistry> createServiceRegistryPageForDBMocking(final int amountOfServiceDefinition, final int amountOfSystem, final String interface1Name, final String imterface2Name) {
		final ZonedDateTime timeStamp = ZonedDateTime.now();
		
		final List<System> systemList = new ArrayList<>();
		final List<ServiceDefinition> serviceRegistryDefinitionList = new ArrayList<>();
		final List<ServiceRegistry> serviceRegistryList = new ArrayList<>();
		
		final ServiceInterface serviceInterface1 = new ServiceInterface(interface1Name);
		serviceInterface1.setId(1);
		serviceInterface1.setCreatedAt(timeStamp);
		serviceInterface1.setUpdatedAt(timeStamp);
		
		final ServiceInterface serviceInterface2 = new ServiceInterface(imterface2Name);
		serviceInterface2.setId(2);
		serviceInterface2.setCreatedAt(timeStamp);
		serviceInterface2.setUpdatedAt(timeStamp);
		
		for (int i = 1; i <= amountOfServiceDefinition; i++) {				
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService" + i);
			serviceDefinition.setId(i);
			serviceDefinition.setCreatedAt(timeStamp);
			serviceDefinition.setUpdatedAt(timeStamp);
			serviceRegistryDefinitionList.add(serviceDefinition);
		}
		for (int i = 1; i <= amountOfSystem; i++) {										
			final System system = new System("testSystem" + i, "testAddress" + i, i * 1000, null);
			system.setId(i);
			system.setCreatedAt(timeStamp);
			system.setUpdatedAt(timeStamp);			
			systemList.add(system);
		}
		for (int i = 1; i <= amountOfServiceDefinition; i++) {
			final ServiceDefinition serviceDefinition = serviceRegistryDefinitionList.get(i-1);
			for (int j = 1; j <= amountOfSystem; j++) {
				final System system = systemList.get(j-1);
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
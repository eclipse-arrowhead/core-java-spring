package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
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
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration(classes = { ServiceRegistryDBSerrviceTestContext.class })
public class ServiceRegistryControllerServiceRegistryTest {
	
	//=================================================================================================
	// members
	
	private static final String SERVICEREGISTRY_REGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;

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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		Assert.assertEquals(SERVICEREGISTRY_REGISTER_URI, error.getOrigin());
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
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems);
		final ServiceRegistryListResponseDTO serviceRegistryEntriesDTO = DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntries);
		when(serviceRegistryDBService.getServiceReqistryEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceRegistryEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt")
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
		final Page<ServiceRegistry> serviceRegistryEntries = createServiceRegistryPageForDBMocking(numOfServices, numOfSystems);
		final ServiceRegistryListResponseDTO serviceRegistryEntriesDTO = DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntries);
		when(serviceRegistryDBService.getServiceReqistryEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceRegistryEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt")
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
		this.mockMvc.perform(get("/serviceregistry/mgmt")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testGetServiceRegistryEntriesWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt")
				.param("page", "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetServiceRegistryEntriesWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt")
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private MvcResult postRegisterService(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICEREGISTRY_REGISTER_URI)
				    	   .contentType(MediaType.APPLICATION_JSON)
				    	   .content(objectMapper.writeValueAsBytes(request))
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
	private Page<ServiceRegistry> createServiceRegistryPageForDBMocking(final int amountOfServiceDefinition, final int amountOfSystem) {
		final ZonedDateTime timeStamp = ZonedDateTime.now();
		
		final List<System> systemList = new ArrayList<>();
		final List<ServiceDefinition> serviceRegistryDefinitionList = new ArrayList<>();
		final List<ServiceRegistry> serviceRegistryList = new ArrayList<>();
		
		final ServiceInterface serviceInterfaceJSON = new ServiceInterface("JSON");
		serviceInterfaceJSON.setId(1);
		serviceInterfaceJSON.setCreatedAt(timeStamp);
		serviceInterfaceJSON.setUpdatedAt(timeStamp);
		
		final ServiceInterface serviceInterfaceXML = new ServiceInterface("XML");
		serviceInterfaceXML.setId(2);
		serviceInterfaceXML.setCreatedAt(timeStamp);
		serviceInterfaceXML.setUpdatedAt(timeStamp);
		
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
				serviceRegistry.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(serviceRegistry, serviceInterfaceJSON));
				serviceRegistry.getInterfaceConnections().add(new ServiceRegistryInterfaceConnection(serviceRegistry, serviceInterfaceXML));
				serviceRegistryList.add(serviceRegistry);
			}
		}
		final Page<ServiceRegistry> entries = new PageImpl<ServiceRegistry>(serviceRegistryList);
		return entries;
	}
}
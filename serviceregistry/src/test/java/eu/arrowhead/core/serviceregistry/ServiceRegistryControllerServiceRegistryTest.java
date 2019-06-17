package eu.arrowhead.core.serviceregistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
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
	
	private static final String SERVICE_REGISTRY_REGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
	private static final String SERVICE_REGISTRY_UNREGISTER_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
	private static final String SERVICE_REGISTRY_QUERY_URI = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI;

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
	private MvcResult postQueryService(final ServiceQueryFormDTO form, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICE_REGISTRY_QUERY_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(form))
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
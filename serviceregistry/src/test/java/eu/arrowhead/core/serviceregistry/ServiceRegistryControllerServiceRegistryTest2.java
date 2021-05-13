/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration(classes = { ServiceRegistryDBServiceTestContext.class })
@ActiveProfiles("nonstrict")
public class ServiceRegistryControllerServiceRegistryTest2 {
	
	//=================================================================================================
	// members
	
	private static final String SERVICEREGISTRY_REGISTER_URI = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI;
	private static final String SERVICEREGISTRY_UNREGISTER_URI = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI;
	private static final String SERVICEREGISTRY_QUERY_URI = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_QUERY_URI;
	private static final String SERVICEREGISTRY_MGMT_URI = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.MGMT_URI;
	private static final String SERVICEREGISTRY_MULTI_QUERY_URI = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI;
	
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

	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceServiceDefinitionWrongFlagFalse() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s-1-");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12T13:51:30Z");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		when(serviceRegistryDBService.registerServiceResponse(request)).thenReturn(new ServiceRegistryResponseDTO());
		
		postRegisterService(request, status().isCreated());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAddServiceServiceDefinitionWrongFlagFalse() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s-1-");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12T13:51:30Z");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		when(serviceRegistryDBService.registerServiceResponse(request)).thenReturn(new ServiceRegistryResponseDTO());
		
		addServiceRegistry(request, status().isCreated());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateServiceServiceDefinitionWrongFlagFalse() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("s-1-");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12T13:51:30Z");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		when(serviceRegistryDBService.updateServiceByIdResponse(anyLong(), any(ServiceRegistryRequestDTO.class))).thenReturn(new ServiceRegistryResponseDTO());
		
		updateServiceRegistry(request, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMergeServiceServiceDefinitionWrongFlagFalse() throws Exception {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setServiceDefinition("1s");
		request.setProviderSystem(getAValidSystemRequestDTO());
		request.setEndOfValidity("2019-06-12T13:51:30Z");
		request.setInterfaces(List.of("HTTP-SECURE-XML"));
		
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setSystemName("1s");
		final ServiceRegistryResponseDTO response = new ServiceRegistryResponseDTO();
		response.setProvider(provider);
		
		when(serviceRegistryDBService.mergeServiceByIdResponse(anyLong(), any(ServiceRegistryRequestDTO.class))).thenReturn(response);
		
		mergeServiceRegistry(request, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterServiceServiceDefinitionWrongFlagFalse() throws Exception {
		final String queryStr = createQueryStringForUnregister("s-1-", "x", "a", 1, "/path");
		
		doNothing().when(serviceRegistryDBService).removeServiceRegistry(any(String.class), any(String.class), any(String.class), anyInt(), any(String.class));
		
		deleteUnregisterService(queryStr, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryServiceServiceDefinitionRequirementWrongFlagFalse() throws Exception {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("test_service");
		
		when(serviceRegistryDBService.queryRegistry(any(ServiceQueryFormDTO.class))).thenReturn(new ServiceQueryResultDTO());
		
		postQueryService(form, status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testMultiQueryServiceServiceRequirementWrongFlagFalse() throws Exception {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO();
		form.setServiceDefinitionRequirement("test_service");
		
		when(serviceRegistryDBService.multiQueryRegistry(any(ServiceQueryFormListDTO.class))).thenReturn(new ServiceQueryResultListDTO());
		
		postMultiQueryService(new ServiceQueryFormListDTO(List.of(form)), status().isOk());
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
	private MvcResult addServiceRegistry(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICEREGISTRY_MGMT_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult updateServiceRegistry(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		final long validServiceRegistryId = 1;
		
		return this.mockMvc.perform(put(SERVICEREGISTRY_MGMT_URI + "/" + validServiceRegistryId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult mergeServiceRegistry(final ServiceRegistryRequestDTO request, final ResultMatcher matcher) throws Exception {
		final long validServiceRegistryId = 1;
		
		return this.mockMvc.perform(patch(SERVICEREGISTRY_MGMT_URI + "/" + validServiceRegistryId)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult deleteUnregisterService(final String queryStr, final ResultMatcher matcher) throws Exception {
		final String validatedQueryStr = Utilities.isEmpty(queryStr) ? "" : "?" + queryStr.trim();
		return this.mockMvc.perform(delete(SERVICEREGISTRY_UNREGISTER_URI + validatedQueryStr)
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postQueryService(final ServiceQueryFormDTO form, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICEREGISTRY_QUERY_URI)
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
	private MvcResult postMultiQueryService(final ServiceQueryFormListDTO forms, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(SERVICEREGISTRY_MULTI_QUERY_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(forms))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private String createQueryStringForUnregister(final String serviceDefinition, final String providerName, final String providerAddress, final Integer providerPort, final String serviceUri) {
		final StringBuilder sb = new StringBuilder();
		
		if (serviceDefinition != null) {
			sb.append(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION).append("=").append(serviceDefinition).append("&");
		}
		
		if (providerName != null) {
			sb.append(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME).append("=").append(providerName).append("&");
		}
		
		if (providerAddress != null) {
			sb.append(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS).append("=").append(providerAddress).append("&");
		}
		
		if (providerPort != null) {
			sb.append(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT).append("=").append(providerPort.intValue()).append("&");
		}
		
		if (serviceUri != null) {
			sb.append(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_URI).append("=").append(URLEncoder.encode(serviceUri, StandardCharsets.UTF_8)).append("&");
		}
		
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}

}
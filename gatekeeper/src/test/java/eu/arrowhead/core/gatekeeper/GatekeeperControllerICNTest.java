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

package eu.arrowhead.core.gatekeeper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.http.HttpStatus;
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
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.gatekeeper.service.GatekeeperService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration(classes = { GatekeeperServiceTestContext.class })
public class GatekeeperControllerICNTest {

	//=================================================================================================
	// members
	
	private static final String INIT_ICN_URI = CommonConstants.GATEKEEPER_URI + "/init_icn";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockGatekeeperService") 
	private GatekeeperService gatekeeperService;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequestedServiceNull() throws Exception {
		final MvcResult result = postInitICN(new ICNRequestFormDTO(), status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("Requested service is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequestedServiceDefinitionNull() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		request.setRequestedService(new ServiceQueryFormDTO());
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("Requested service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequestedServiceDefinitionBlank() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("");
		request.setRequestedService(requestedService);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("Requested service definition is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationTargetCloudIdNull() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("Invalid id: null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationTargetCloudIdInvalid() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(-1L);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("Invalid id: -1", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemNull() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemNameNull() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemNameBlank() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("\t\r\n\t ");
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System name is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemAddressNull() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("system0");
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemAddressBlank() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("system0");
		system.setAddress(" ");
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System address is null or blank", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemPortNull() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("system0");
		system.setAddress("localhost");
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System port is null", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemPortTooLow() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("system0");
		system.setAddress("localhost");
		system.setPort(-100);
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationRequesterSystemPortTooHigh() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("system0");
		system.setAddress("localhost");
		system.setPort(98786);
		request.setRequesterSystem(system);
		
		final MvcResult result = postInitICN(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, error.getErrorCode());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	// we skip tests about preferred system validation because it uses the same method as requester system validation
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateInterCloudNegotiationEverythingOKNoGateway() throws Exception {
		final ICNRequestFormDTO request = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		request.setTargetCloudId(1L);
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("system0");
		system.setAddress("localhost");
		system.setPort(9878);
		request.setRequesterSystem(system);
		
		when(gatekeeperService.initICN(any(ICNRequestFormDTO.class))).thenReturn(new ICNResultDTO(List.of(new OrchestrationResultDTO(), new OrchestrationResultDTO())));
		final MvcResult result = postInitICN(request, status().isOk());
		final ICNResultDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ICNResultDTO.class);
		Assert.assertEquals(2, response.getResponse().size());
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postInitICN(final ICNRequestFormDTO request, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(INIT_ICN_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(request))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
}
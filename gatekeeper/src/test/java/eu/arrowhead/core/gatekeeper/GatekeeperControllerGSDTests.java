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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
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
import eu.arrowhead.common.dto.internal.GSDMultiQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryResultDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.gatekeeper.service.GatekeeperService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration(classes = { GatekeeperServiceTestContext.class })
public class GatekeeperControllerGSDTests {

	//=================================================================================================
	// members
	
	private static final String INIT_GLOBAL_SERVICE_DISCOVERY_URI = CommonConstants.GATEKEEPER_URI + "/init_gsd";
	private static final String INIT_MULTI_GLOBAL_SERVICE_DISCOVERY_URI = CommonConstants.GATEKEEPER_URI + "/init_multi_gsd";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockGatekeeperService") 
	private GatekeeperService gatekeeperService;

	@MockBean(name = "mockCnVerifier")
	private CommonNamePartVerifier cnVerifier;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//=================================================================================================
	// Tests of initiateGlobalServiceDiscovery
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryOk() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		when(gatekeeperService.initGSDPoll(any())).thenReturn(new GSDQueryResultDTO(List.of(new GSDPollResponseDTO()), 0));
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		
		final MvcResult response = this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
											   .content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final GSDQueryResultDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), GSDQueryResultDTO.class);
		assertEquals(0, responseBody.getUnsuccessfulRequests());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullRequestedService() throws Exception {
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(null);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullServiceDefinition() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement(null);
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithBlankServiceDefinition() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("  ");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullCloudInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(null);
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredClouds(preferredClouds);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullOperatorInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(null);
		cloudRequestDTO.setName("test-name");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		when(cnVerifier.isValid(anyString())).thenReturn(true);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithBlankOperatorInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("   ");
		cloudRequestDTO.setName("test-name");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		when(cnVerifier.isValid(anyString())).thenReturn(true);

		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullNameInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName(null);
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		when(cnVerifier.isValid(anyString())).thenReturn(true);

		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithBlankNameInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("   ");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		when(cnVerifier.isValid(anyString())).thenReturn(true);

		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of initiateMultiGlobalServiceDiscovery
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithNullRequestedServices() throws Exception {
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(new GSDMultiQueryFormDTO(), status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("RequestedServices list is null or empty", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithEmptyRequestedServices() throws Exception {
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of());
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("RequestedServices list is null or empty", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithNullServiceRequirement() throws Exception {
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(new ServiceQueryFormDTO()));
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("serviceDefinitionRequirement is empty", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithEmptyServiceRequirement() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement(" ");
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("serviceDefinitionRequirement is empty", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithNullPreferredCloud() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(null);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is empty", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithPreferredCloudOperatorNull() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setName("name");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(cloudDTO);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);
		
		when(cnVerifier.isValid("name")).thenReturn(true);
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is invalid due to the following reasons: operator is empty, operator is in wrong format", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithPreferredCloudOperatorEmpty() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator(" ");
		cloudDTO.setName("name");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(cloudDTO);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);

		when(cnVerifier.isValid("name")).thenReturn(true);

		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is invalid due to the following reasons: operator is empty, operator is in wrong format", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithPreferredCloudIllFormedOperator() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("wrong_operator");
		cloudDTO.setName("name");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(cloudDTO);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);
		
		when(cnVerifier.isValid("wrong_operator")).thenReturn(false);
		when(cnVerifier.isValid("name")).thenReturn(true);
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		verify(cnVerifier, times(1)).isValid("wrong_operator");
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is invalid due to the following reasons: operator is in wrong format", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithPreferredCloudNameNull() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("operator");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(cloudDTO);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);
		
		when(cnVerifier.isValid("operator")).thenReturn(true);
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		verify(cnVerifier, times(1)).isValid("operator");
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is invalid due to the following reasons: name is empty, name is in wrong format", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithPreferredCloudNameEmpty() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("operator");
		cloudDTO.setName("");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(cloudDTO);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);
		
		when(cnVerifier.isValid("operator")).thenReturn(true);
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		verify(cnVerifier, times(1)).isValid("operator");
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is invalid due to the following reasons: name is empty, name is in wrong format", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryWithPreferredCloudIllFormedName() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("operator");
		cloudDTO.setName("wrong_name");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(cloudDTO);
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));
		form.setPreferredClouds(preferredClouds);
		
		when(cnVerifier.isValid("operator")).thenReturn(true);
		when(cnVerifier.isValid("wrong_name")).thenReturn(false);
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isBadRequest());
		final ErrorMessageDTO errorMessageDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		verify(cnVerifier, times(1)).isValid("operator");
		verify(cnVerifier, times(1)).isValid("wrong_name");
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, errorMessageDTO.getExceptionType());
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, errorMessageDTO.getErrorCode());
		Assert.assertEquals("CloudRequestDTO is invalid due to the following reasons: name is in wrong format", errorMessageDTO.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateMultiGlobalServiceDiscoveryOk() throws Exception {
		final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO();
		serviceQueryForm.setServiceDefinitionRequirement("service");
		
		final GSDMultiQueryFormDTO form = new GSDMultiQueryFormDTO();
		form.setRequestedServices(List.of(serviceQueryForm));

		when(gatekeeperService.initMultiGSDPoll(any(GSDMultiQueryFormDTO.class))).thenReturn(new GSDMultiQueryResultDTO());
		
		final MvcResult result = postInitiateMultiGlobalServiceDiscovery(form, status().isOk());
		final GSDMultiQueryResultDTO dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), GSDMultiQueryResultDTO.class);
		
		verify(gatekeeperService, times(1)).initMultiGSDPoll(any(GSDMultiQueryFormDTO.class));
		
		Assert.assertNotNull(dto);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postInitiateMultiGlobalServiceDiscovery(final GSDMultiQueryFormDTO form, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(INIT_MULTI_GLOBAL_SERVICE_DISCOVERY_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(form))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
}
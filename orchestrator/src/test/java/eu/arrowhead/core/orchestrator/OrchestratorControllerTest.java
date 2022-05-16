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

package eu.arrowhead.core.orchestrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.internal.SystemDescriberDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreFlexibleDBService;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrchestratorMain.class)
@ContextConfiguration(classes = { OrchestratorServiceTestContext.class })
public class OrchestratorControllerTest {

	//=================================================================================================
	// members
	
	private static final String ORCHESTRATION_PROCESS_URI = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI; 
	private static final String ORCHESTRATION_PROCESS_BY_PROXY_URI = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_BY_PROXY_URI; 
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private OrchestratorController controller;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockOrchestratorService") 
	private OrchestratorService orchestratorService;
	
	@MockBean 
	private OrchestratorStoreFlexibleDBService orchestratorStoreFlexibleDBService;
	
	@Spy
	private CommonNamePartVerifier cnVerifier;
	
	@Spy
	private ServiceInterfaceNameVerifier interfaceNameVerifier; 

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessCrossValidationConstraint1() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.OVERRIDE_STORE, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("Requested service can not be null when \"" + Flag.OVERRIDE_STORE + "\" is TRUE", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessCrossValidationConstraint2() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.TRIGGER_INTER_CLOUD, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("Requested service can not be null when \"" + Flag.TRIGGER_INTER_CLOUD + "\" is TRUE", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessCrossValidationConstraint3() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_PREFERRED, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("There is no valid preferred provider, but \"" + Flag.ONLY_PREFERRED + "\" is set to true", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemNull() throws Exception {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System is null.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemNameNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank.", error.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemNameEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemAddressNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemAddressEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("\t");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemPortNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System port is null.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemPortTooLow() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(-1);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterSystemPortTooHigh() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(66000);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterCloudOperatorNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(new CloudRequestDTO())
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterCloudOperatorEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("\r");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(requesterCloud)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank.", error.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterCloudNameNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(requesterCloud)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessRequesterCloudNameEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(requesterCloud)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessServiceDefinitionNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(new ServiceQueryFormDTO())
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Requested service definition requirement is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessServiceDefinitionEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Requested service definition requirement is null or blank.", error.getErrorMessage());
	}
	
	// Controller uses the same checking on the content of the preferred providers than on the requester system (and cloud) before, so we skip the related tests
	

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessExternalServiceRequestWithoutGatekeeper() throws Exception {
		ReflectionTestUtils.setField(controller, "gatekeeperIsPresent", false);

		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.EXTERNAL_SERVICE_REQUEST, true)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("External service request can not be served. Orchestrator runs in NO GATEKEEPER mode.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessTriggerInterCloudWithoutGatekeeper() throws Exception {
		ReflectionTestUtils.setField(controller, "gatekeeperIsPresent", false);

		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.TRIGGER_INTER_CLOUD, true)
																				   .build();
		
		final MvcResult result = postOrchestrationProcess(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_URI, error.getOrigin());
		Assert.assertEquals("Forced inter cloud service request can not be served. Orchestrator runs in NO GATEKEEPER mode.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessExternalServiceRequestWasCalled() throws Exception {
		ReflectionTestUtils.setField(controller, "gatekeeperIsPresent", true);
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.EXTERNAL_SERVICE_REQUEST, true)
																				   .build();
		
		when(orchestratorService.externalServiceRequest(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO());
		when(orchestratorService.triggerInterCloud(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(orchestratorService.orchestrationFromOriginalStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(false))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																						  new OrchestrationResultDTO(),
																																						  new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcess(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(0, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessTriggerInterCloudWasCalled() throws Exception {
		ReflectionTestUtils.setField(controller, "gatekeeperIsPresent", true);
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.TRIGGER_INTER_CLOUD, true)
																				   .build();
		
		when(orchestratorService.externalServiceRequest(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO());
		when(orchestratorService.triggerInterCloud(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(orchestratorService.orchestrationFromOriginalStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(false))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																						  new OrchestrationResultDTO(),
																																						  new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcess(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(1, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessOrchestrationFromStoreWasCalled() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .build();
		
		when(orchestratorService.externalServiceRequest(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO());
		when(orchestratorService.triggerInterCloud(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(orchestratorService.orchestrationFromStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(false))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																						  new OrchestrationResultDTO(),
																																						  new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcess(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(2, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessDynamicOrchestrationWasCalled() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.OVERRIDE_STORE, true)
																				   .build();
		
		when(orchestratorService.externalServiceRequest(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO());
		when(orchestratorService.triggerInterCloud(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(orchestratorService.orchestrationFromOriginalStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(false))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																			   			  new OrchestrationResultDTO(),
																																			   			  new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcess(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(3, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getStoreOchestrationProcessResponseOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestrationResponseDTO dto =  new OrchestrationResponseDTO();
		when(orchestratorService.storeOchestrationProcessResponse(anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getStoreOchestrationProcessResponseFlexibleStoreEnabledTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final MvcResult result = this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI + "/1")
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI + "/{" + CoreCommonConstants.COMMON_FIELD_NAME_ID + "}", error.getOrigin());
		Assert.assertEquals("Orchestrator use flexible store!", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getStoreOchestrationProcessResponseByInvalidIdTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestrationResponseDTO dto =  new OrchestrationResponseDTO();
		when(orchestratorService.storeOchestrationProcessResponse(anyLong())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI + "/-1")
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI + "/{" + CoreCommonConstants.COMMON_FIELD_NAME_ID + "}", error.getOrigin());
		Assert.assertEquals("Consumer system :  Id must be greater than 0. ", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void createFlexibleStoreRuleOkWithSysNamesTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO("consumer", null),
																									   new SystemDescriberDTO("provider", null),
																									   "service",
																									   "HTTP-SECURE-JSON",
																									   null,
																									   10);
		
		final ArgumentCaptor<List<OrchestratorStoreFlexibleRequestDTO>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleDBService.createOrchestratorStoreFlexibleResponse(captor.capture())).thenReturn(new OrchestratorStoreFlexibleListResponseDTO(List.of(new OrchestratorStoreFlexibleResponseDTO()), 1));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isCreated());
		
		final OrchestratorStoreFlexibleRequestDTO captured = captor.getValue().get(0);
		assertEquals(requestDTO.getConsumerSystem().getSystemName(), captured.getConsumerSystem().getSystemName());
		assertEquals(requestDTO.getProviderSystem().getSystemName(), captured.getProviderSystem().getSystemName());
		assertEquals(requestDTO.getServiceDefinitionName(), captured.getServiceDefinitionName());
		assertEquals(requestDTO.getServiceInterfaceName(), captured.getServiceInterfaceName());
		assertEquals(requestDTO.getPriority().intValue(), captured.getPriority().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void createFlexibleStoreRuleOkWithSysMetaTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		final ArgumentCaptor<List<OrchestratorStoreFlexibleRequestDTO>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleDBService.createOrchestratorStoreFlexibleResponse(captor.capture())).thenReturn(new OrchestratorStoreFlexibleListResponseDTO(List.of(new OrchestratorStoreFlexibleResponseDTO()), 1));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isCreated());
		
		final OrchestratorStoreFlexibleRequestDTO captured = captor.getValue().get(0);
		assertTrue(captured.getConsumerSystem().getMetadata().entrySet().containsAll(requestDTO.getConsumerSystem().getMetadata().entrySet()));
		assertTrue(captured.getProviderSystem().getMetadata().entrySet().containsAll(requestDTO.getProviderSystem().getMetadata().entrySet()));
		assertEquals(requestDTO.getServiceDefinitionName(), captured.getServiceDefinitionName());
		assertTrue(captured.getServiceMetadata().entrySet().containsAll(requestDTO.getServiceMetadata().entrySet()));
		assertEquals(requestDTO.getPriority().intValue(), captured.getPriority().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createFlexibleStoreRuleWithFlagFalseTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createFlexibleStoreRuleWithInvalidConsumerDescriptorTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, null),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createFlexibleStoreRuleWithInvalidProviderDescriptorTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, null),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createFlexibleStoreRuleWithInvalidConsumerNameTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO("-invalid", Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createFlexibleStoreRuleWithInvalidProviderNameTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO("-invalid", Map.of("k2", "v2")),
																									   "service",
																									   null,
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createFlexibleStoreRuleWithInvalidInterfaceNameTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		final OrchestratorStoreFlexibleRequestDTO requestDTO = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO(null, Map.of("k1", "v1")),
																									   new SystemDescriberDTO(null, Map.of("k2", "v2")),
																									   "service",
																									   "invalid",
																									   Map.of("k3", "v3"),
																									   10);
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI)
				   .contentType(MediaType.APPLICATION_JSON)
				   .content(objectMapper.writeValueAsBytes(List.of(requestDTO)))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).createOrchestratorStoreFlexibleResponse(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeFlexibleStoreRuleByIdOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		doNothing().when(orchestratorStoreFlexibleDBService).deleteOrchestratorStoreFlexibleById(captor.capture());
		
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_REMOVE_FLEXIBLE_STORE_RULE_URI.replace("{id}", "5"))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());
		
		assertEquals(5, captor.getValue().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeFlexibleStoreRuleByIdFlagFalseTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		doNothing().when(orchestratorStoreFlexibleDBService).deleteOrchestratorStoreFlexibleById(captor.capture());
		
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_REMOVE_FLEXIBLE_STORE_RULE_URI.replace("{id}", "5"))
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).deleteOrchestratorStoreFlexibleById(anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void cleanFlexibleStoreRuleOkTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", true);
		
		doNothing().when(orchestratorStoreFlexibleDBService).deleteAllOrchestratorStoreFlexible();
		
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CLEAN_FLEXIBLE_STORE_URI)
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isOk());
		
		verify(orchestratorStoreFlexibleDBService, times(1)).deleteAllOrchestratorStoreFlexible();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void cleanFlexibleStoreRuleFlagFalseTest() throws Exception {
		ReflectionTestUtils.setField(controller, "useFlexibleStore", false);
		
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		doNothing().when(orchestratorStoreFlexibleDBService).deleteOrchestratorStoreFlexibleById(captor.capture());
		
		this.mockMvc.perform(delete(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CLEAN_FLEXIBLE_STORE_URI)
				   .accept(MediaType.APPLICATION_JSON))
				   .andExpect(status().isBadRequest());
		
		verify(orchestratorStoreFlexibleDBService, never()).deleteAllOrchestratorStoreFlexible();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void isQoSEnabledOkTest() throws Exception {
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_ENABLED_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllQoSReservationOkTest() throws Exception {
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void lockProvidersTemporaryOkTest() throws Exception {
		final QoSTemporaryLockResponseDTO responseDTO = new QoSTemporaryLockResponseDTO(List.of(new OrchestrationResultDTO()));
		when(orchestratorService.lockProvidersTemporarily(any())).thenReturn(responseDTO);
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);

		final QoSTemporaryLockRequestDTO requestDTO = new QoSTemporaryLockRequestDTO(requesterSystem, List.of(new OrchestrationResultDTO()));
		
		final MvcResult result = this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI)
				   							 .contentType(MediaType.APPLICATION_JSON)
				   							 .content(objectMapper.writeValueAsBytes(requestDTO))
				   							 .accept(MediaType.APPLICATION_JSON))
				   							 .andExpect(status().isOk())
				   							 .andReturn();
		final QoSTemporaryLockResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), QoSTemporaryLockResponseDTO.class);
		
		assertEquals(responseDTO.getResponse().size(), response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void lockProvidersTemporaryBlankSystemNameTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);

		final QoSTemporaryLockRequestDTO requestDTO = new QoSTemporaryLockRequestDTO(requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI)
				   							 .contentType(MediaType.APPLICATION_JSON)
				   							 .content(objectMapper.writeValueAsBytes(requestDTO))
				   							 .accept(MediaType.APPLICATION_JSON))
				   							 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void lockProvidersTemporaryNullSystemNameTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(null);
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);

		final QoSTemporaryLockRequestDTO requestDTO = new QoSTemporaryLockRequestDTO(requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI)
				   							 .contentType(MediaType.APPLICATION_JSON)
				   							 .content(objectMapper.writeValueAsBytes(requestDTO))
				   							 .accept(MediaType.APPLICATION_JSON))
				   							 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void lockProvidersTemporaryBlankSystemAddressTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("");
		requesterSystem.setPort(10000);

		final QoSTemporaryLockRequestDTO requestDTO = new QoSTemporaryLockRequestDTO(requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void lockProvidersTemporaryNullSystemAddressTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress(null);
		requesterSystem.setPort(10000);

		final QoSTemporaryLockRequestDTO requestDTO = new QoSTemporaryLockRequestDTO(requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void lockProvidersTemporaryNullSystemPortTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(null);

		final QoSTemporaryLockRequestDTO requestDTO = new QoSTemporaryLockRequestDTO(requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationOkTest() throws Exception {
		doNothing().when(orchestratorService).confirmProviderReservation(any());
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationBlankRequesterSystemNameTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationNullRequesterSystemNameTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(null);
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationBlankRequesterSystemAddressTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationNullRequesterSystemAddressTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress(null);
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationNullRequesterSystemPortTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(null);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationNullSelectedSystemTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(null);
		selectedResult.setService(requestedService);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmReservationNullRequestedServiceTest() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(null);;

		final QoSReservationRequestDTO requestDTO = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		this.mockMvc.perform(post(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI)
				   	.contentType(MediaType.APPLICATION_JSON)
				   	.content(objectMapper.writeValueAsBytes(requestDTO))
				   	.accept(MediaType.APPLICATION_JSON))
				   	.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyCrossValidationConstraint1() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.OVERRIDE_STORE, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("Requested service can not be null when \"" + Flag.OVERRIDE_STORE + "\" is TRUE", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyCrossValidationConstraint2() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.TRIGGER_INTER_CLOUD, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("Requested service can not be null when \"" + Flag.TRIGGER_INTER_CLOUD + "\" is TRUE", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyCrossValidationConstraint3() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_PREFERRED, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("There is no valid preferred provider, but \"" + Flag.ONLY_PREFERRED + "\" is set to true", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyCrossValidationConstraint4() throws Exception {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ENABLE_QOS, true);
		flags.put(Flag.MATCHMAKING, false);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		request.setCommands(Map.of(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "1"));
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals("Exclusive service orchestration is only possible when \"" + Flag.MATCHMAKING + "\" is set to true", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemNull() throws Exception {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System is null.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemNameNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank.", error.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemNameEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemAddressNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemAddressEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("\t");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System address is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemPortNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System port is null.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemPortTooLow() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(-1);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterSystemPortTooHigh() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(66000);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterCloudOperatorNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(new CloudRequestDTO())
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterCloudOperatorEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("\r");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(requesterCloud)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Cloud operator is null or blank.", error.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterCloudNameNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(requesterCloud)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyRequesterCloudNameEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requesterCloud(requesterCloud)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Cloud name is null or blank.", error.getErrorMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyServiceDefinitionNull() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(new ServiceQueryFormDTO())
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Requested service definition requirement is null or blank.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyServiceDefinitionEmpty() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Requested service definition requirement is null or blank.", error.getErrorMessage());
	}
	
	// Controller uses the same checking on the content of the preferred providers than on the requester system (and cloud) before, so we skip the related tests
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyTriggerInterCloudWithoutGatekeeper() throws Exception {
		ReflectionTestUtils.setField(controller, "gatekeeperIsPresent", false);

		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.TRIGGER_INTER_CLOUD, true)
																				   .build();
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isBadRequest());
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(ORCHESTRATION_PROCESS_BY_PROXY_URI, error.getOrigin());
		Assert.assertEquals("Forced inter cloud service request can not be served. Orchestrator runs in NO GATEKEEPER mode.", error.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyTriggerInterCloudWasCalled() throws Exception {
		ReflectionTestUtils.setField(controller, "gatekeeperIsPresent", true);
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.TRIGGER_INTER_CLOUD, true)
																				   .build();
		
		when(orchestratorService.triggerInterCloud(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(1, response.getResponse().size());
		
		verify(orchestratorService, times(1)).triggerInterCloud(any(OrchestrationFormRequestDTO.class));
		verify(orchestratorService, never()).orchestrationFromStore(any(OrchestrationFormRequestDTO.class));
		verify(orchestratorService, never()).dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(true));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyOrchestrationFromStoreWasCalled() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .build();
		
		when(orchestratorService.orchestrationFromStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(2, response.getResponse().size());

		verify(orchestratorService, never()).triggerInterCloud(any(OrchestrationFormRequestDTO.class));
		verify(orchestratorService, times(1)).orchestrationFromStore(any(OrchestrationFormRequestDTO.class));
		verify(orchestratorService, never()).dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(true));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationProcessByProxyDynamicOrchestrationWasCalled() throws Exception {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester");
		requesterSystem.setAddress("address.hu");
		requesterSystem.setPort(1234);
		final ServiceQueryFormDTO service = new ServiceQueryFormDTO();
		service.setServiceDefinitionRequirement("service");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(requesterSystem)
																				   .requestedService(service)
																				   .flag(Flag.OVERRIDE_STORE, true)
																				   .build();
		
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(true))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																			   			  new OrchestrationResultDTO(),
																																			   			  new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcessByProxy(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		
		Assert.assertEquals(3, response.getResponse().size());
		
		verify(orchestratorService, never()).triggerInterCloud(any(OrchestrationFormRequestDTO.class));
		verify(orchestratorService, never()).orchestrationFromStore(any(OrchestrationFormRequestDTO.class));
		verify(orchestratorService, times(1)).dynamicOrchestration(any(OrchestrationFormRequestDTO.class), eq(true));
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postOrchestrationProcess(final OrchestrationFormRequestDTO form, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(ORCHESTRATION_PROCESS_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(form))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
	
	//-------------------------------------------------------------------------------------------------
	private MvcResult postOrchestrationProcessByProxy(final OrchestrationFormRequestDTO form, final ResultMatcher matcher) throws Exception {
		return this.mockMvc.perform(post(ORCHESTRATION_PROCESS_BY_PROXY_URI)
						   .contentType(MediaType.APPLICATION_JSON)
						   .content(objectMapper.writeValueAsBytes(form))
						   .accept(MediaType.APPLICATION_JSON))
						   .andExpect(matcher)
						   .andReturn();
	}
};
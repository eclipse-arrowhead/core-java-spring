package eu.arrowhead.core.orchestrator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.internal.CloudRequestDTO;
import eu.arrowhead.common.dto.internal.ErrorMessageDTO;
import eu.arrowhead.common.dto.internal.OrchestrationFlags;
import eu.arrowhead.common.dto.internal.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestrationResultDTO;
import eu.arrowhead.common.dto.internal.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.internal.SystemRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestrationFlags.Flag;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrchestratorMain.class)
@ContextConfiguration(classes = { OrchestratorServiceTestContext.class })
public class OrchestratorControllerTest {

	//=================================================================================================
	// members
	
	private static final String ORCHESTRATION_PROCESS_URI = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS; 
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private OrchestratorController controller;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockOrchestratorService") 
	OrchestratorService orchestratorService;
	
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
		when(orchestratorService.orchestrationFromStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
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
		when(orchestratorService.orchestrationFromStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
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
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
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
		when(orchestratorService.orchestrationFromStore(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																				 new OrchestrationResultDTO())));
		when(orchestratorService.dynamicOrchestration(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO(),
																																			   new OrchestrationResultDTO(),
																																			   new OrchestrationResultDTO())));
		
		final MvcResult result = postOrchestrationProcess(request, status().isOk());
		final OrchestrationResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), OrchestrationResponseDTO.class);
		Assert.assertEquals(3, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getStoreOchestrationProcessResponseOkTest() throws Exception {
		final OrchestrationResponseDTO dto =  new OrchestrationResponseDTO();
		when(orchestratorService.storeOchestrationProcessResponse(anyLong())).thenReturn(dto);
		
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getStoreOchestrationProcessResponseByInvalidIdTest() throws Exception {
		final OrchestrationResponseDTO dto =  new OrchestrationResponseDTO();
		when(orchestratorService.storeOchestrationProcessResponse(anyLong())).thenReturn(dto);
		
		final MvcResult result = this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS + "/-1")
											 .accept(MediaType.APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andReturn();
		
		final ErrorMessageDTO error = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.BAD_PAYLOAD, error.getExceptionType());
		Assert.assertEquals(CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS + "/{" + CommonConstants.COMMON_FIELD_NAME_ID + "}", error.getOrigin());
		Assert.assertEquals("Consumer system :  Id must be greater than 0. ", error.getErrorMessage());
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
};
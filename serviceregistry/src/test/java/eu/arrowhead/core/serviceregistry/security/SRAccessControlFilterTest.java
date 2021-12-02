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

package eu.arrowhead.core.serviceregistry.security;

import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class SRAccessControlFilterTest {
	
	//=================================================================================================
	// members

	private static final String SERVICEREGISTRY_ECHO = CommonConstants.SERVICEREGISTRY_URI + "/echo";
	private static final String SERVICEREGISTRY_MGMT_SYSTEMS = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.MGMT_URI + "/systems";
	private static final String SERVICEREGISTRY_REGISTER = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI;
	private static final String SERVICEREGISTRY_REGISTER_SYSTEM = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_SYSTEM_URI;
	private static final String SERVICEREGISTRY_UNREGISTER = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI;
	private static final String SERVICEREGISTRY_UNREGISTER_SYSTEM = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI;
	private static final String SERVICEREGISTRY_PULL_SYSTEMS = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI;
	private static final String SERVICEREGISTRY_QUERY = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_QUERY_URI;
	private static final String SERVICEREGISTRY_MULTI_QUERY = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI;
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_ID = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_URI;
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI;
	private static final String SERVICEREGISTRY_QUERY_ALL = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI;

	@Autowired
	private ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockServiceRegistryDBService")
	private ServiceRegistryDBService serviceRegistryDBService;
	
	private MockMvc mockMvc;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		assumeTrue(secure);
		
		final SRAccessControlFilter sracFilter = appContext.getBean(SRAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(sracFilter)
									  .build();
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_ECHO)
 				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtServiceNoSysop() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_MGMT_SYSTEMS)
 				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtServiceSysop() throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_MGMT_SYSTEMS)
 				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterWithNoSystemName() throws Exception {
		// Filter breaks the filter chain and expects that the web service rejects the ill-formed request
		postRegister(new ServiceRegistryRequestDTO(), "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemNameAndClientNameDoesNotMatch() throws Exception {
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("something_else");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		
		postRegister(dto, "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemNameAndClientNameExactMatch() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("client-demo-provider");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		
		postRegister(dto, "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemNameAndClientNameCaseInsensitiveMatch() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("CLIENT-demo-provider");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		
		postRegister(dto, "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceDefinitionAndCoreServiceDefinitonsCaseInsensitiveMatch() throws Exception {
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("client-demo-provider");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		dto.setServiceDefinition(CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE.getServiceDefinition());
		
		postRegister(dto, "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterServiceDefinitionAndCoreServiceDefinitonsNoMatch() throws Exception {
		// Filter enables the service definition but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("client-demo-provider");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		dto.setServiceDefinition("test-service");
		
		postRegister(dto, "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterWithNoSystemName() throws Exception {
		// Filter breaks the filter chain and expects that the web service rejects the ill-formed request
		deleteUnregister("", "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemNameAndClientNameDoesNotMatch() throws Exception {
		deleteUnregister("something_else", "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemNameAndClientNameExactMatch() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		deleteUnregister("client-demo-provider", "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemNameAndClientNameCaseInsensitiveMatch() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		deleteUnregister("CLIENT-demo-provider", "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryNotAllowedCoreSystemClientBecauseOfNotSpecifiedService() throws Exception {
		postQuery(new ServiceQueryFormDTO(), "certificates/gateway.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryNotAllowedCoreSystemClientBecauseOfNotOwnService() throws Exception {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO.Builder("test-service").build();
		
		postQuery(form, "certificates/gateway.pem", status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryAllowedCoreSystemClientBecauseOfOwnService() throws Exception {
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO.Builder(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE.getServiceDefinition()).build();
		
		when(serviceRegistryDBService.queryRegistry(any())).thenReturn(new ServiceQueryResultDTO());
		
		postQuery(form, "certificates/authorization.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryByNonCoreSystemWithoutServiceSpecified() throws Exception {
		postQuery(new ServiceQueryFormDTO(), "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryNotPublicCoreSystemService() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		postQuery(serviceQueryFormDTO, "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryOrchestratorAllowedCoreSystemClient() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		postQuery(new ServiceQueryFormDTO(), "certificates/orchestrator.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryGateKeeperAllowed() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		postQuery(new ServiceQueryFormDTO(), "certificates/gatekeeper.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryPublicCoreSystemService() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement(CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition());
		
		when(serviceRegistryDBService.queryRegistry(any())).thenReturn(new ServiceQueryResultDTO());
		
		postQuery(serviceQueryFormDTO, "certificates/provider.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemIdOrchestrator() throws Exception {
		when(serviceRegistryDBService.getSystemById(anyLong())).thenReturn(new SystemResponseDTO());
		
		getQueryBySystemId(1l, "certificates/orchestrator.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemIdNotAllowedCoreSys() throws Exception {
		when(serviceRegistryDBService.getSystemById(anyLong())).thenReturn(new SystemResponseDTO());
		
		getQueryBySystemId(1l, "certificates/gateway.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemIdAppSys() throws Exception {
		when(serviceRegistryDBService.getSystemById(anyLong())).thenReturn(new SystemResponseDTO());
		
		getQueryBySystemId(1l, "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTOOrchestrator() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		
		postQueryBySystemDTO(new SystemRequestDTO("system", "testaddress", 5000, null, null), "certificates/orchestrator.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTONotAllowedCoreSys() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		
		postQueryBySystemDTO(new SystemRequestDTO("system", "0.0.0.0", 5000, null, null), "certificates/gateway.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryRegistryBySystemDTOAppSys() throws Exception {
		when(serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(anyString(), anyString(), anyInt())).thenReturn(new SystemResponseDTO());
		
		postQueryBySystemDTO(new SystemRequestDTO("system", "0.0.0.0", 5000, null, null), "certificates/provider.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryAllQoS() throws Exception {
		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(new ServiceRegistryListResponseDTO());
		
		getQueryAll("certificates/qosmonitor.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryAllGatekeeper() throws Exception {
		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(new ServiceRegistryListResponseDTO());
		
		getQueryAll("certificates/gatekeeper.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryAllNotAllowedCoreSys() throws Exception {
		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(new ServiceRegistryListResponseDTO());
		
		getQueryAll("certificates/orchestrator.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testQueryAllAppSys() throws Exception {
		when(serviceRegistryDBService.getServiceRegistryEntriesResponse(anyInt(), anyInt(), any(), anyString())).thenReturn(new ServiceRegistryListResponseDTO());
		
		getQueryAll("certificates/provider.pem", status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemWithSystemNameMatchSystemNameInCertifiacte() throws Exception {

		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(
				"sysop",//systemName,
				"192.168.1.103", //address
				12345,//port,
				null, //authenticationInfo
				null); // metadata
		postRegisterSystem(systemRequestDTO, "certificates/valid.pem", status().isCreated());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemWithSystemNameNotMatchSystemNameInCertifiacte() throws Exception {

		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(
				"not_sysop",//systemName,
				"192.168.1.103", //address
				12345,//port,
				null, //authenticationInfo
				null); // metadata
		postRegisterSystem(systemRequestDTO, "certificates/valid.pem", status().isUnauthorized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemWithNullSystemName() throws Exception {
		// Filter breaks the filter chain and expects that the web service rejects the ill-formed request
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(
				null,//systemName,
				"localhost", //address
				12345,//port,
				null, //authenticationInfo
				null); // metadata
		postRegisterSystem(systemRequestDTO, "certificates/valid.pem", status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemWithEmptySystemName() throws Exception {
		// Filter breaks the filter chain and expects that the web service rejects the ill-formed request
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(
				"   ",//systemName,
				"localhost", //address
				12345,//port,
				null, //authenticationInfo
				null); // metadata
		postRegisterSystem(systemRequestDTO, "certificates/valid.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemWithPlantDescriptionEngine() throws Exception {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(
				"consumer",//systemName,
				"localhost", //address
				12345,//port,
				null, //authenticationInfo
				null); // metadata
		postRegisterSystem(systemRequestDTO, "certificates/plantdescriptionengine.pem", status().isCreated());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemWithNotPlantDescriptionEngine() throws Exception {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(
				"consumer",//systemName,
				"localhost", //address
				12345,//port,
				null, //authenticationInfo
				null); // metadata
		postRegisterSystem(systemRequestDTO, "certificates/gatekeeper.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemWithSystemNameMatchSystemNameInCertifiacte() throws Exception {
		deleteUnregisterSystem("sysop", "address", 5555, "certificates/valid.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemWithSystemNameNotMatchSystemNameInCertifiacte() throws Exception {
		deleteUnregisterSystem("notsysop", "address", 5555, "certificates/valid.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUNregisterSystemWithEmptySystemName() throws Exception {
		deleteUnregisterSystem("   ", "address", 5555, "certificates/valid.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemWithPlantDescriptionEngine() throws Exception {
		deleteUnregisterSystem("consumer", "address", 5555, "certificates/plantdescriptionengine.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnregisterSystemWithNotPlantDescriptionEngine() throws Exception {
		deleteUnregisterSystem("consumer", "address", 5555, "certificates/gatekeeper.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPullSystemsWithPlantDescriptionEngine() throws Exception {
		getPullSystems("certificates/plantdescriptionengine.pem", status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPullSystemsWithNotPlantDescriptionEngine() throws Exception {
		getPullSystems("certificates/gatekeeper.pem", status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testMultiQueryOrchestratorAllowedCoreSystemClient() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		postMultiQuery(new ServiceQueryFormListDTO(), "certificates/orchestrator.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2699") // because of false positive in sonar
	@Test
	public void testMultiQueryUnauthorizedClient() throws Exception {
		postMultiQuery(new ServiceQueryFormListDTO(), "certificates/provider.pem", status().isUnauthorized());
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void postRegister(final ServiceRegistryRequestDTO request, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(SERVICEREGISTRY_REGISTER)
			    	.secure(true)
			    	.with(x509(certificatePath))
			    	.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(request))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}

	//-------------------------------------------------------------------------------------------------
	private void postRegisterSystem(final SystemRequestDTO request, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(SERVICEREGISTRY_REGISTER_SYSTEM)
					.secure(true)
					.with(x509(certificatePath))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}

	//-------------------------------------------------------------------------------------------------
	private void deleteUnregister(final String systemName, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(delete(SERVICEREGISTRY_UNREGISTER + "?" + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME + "=" + systemName)
			    	.secure(true)
			    	.with(x509(certificatePath)))
					.andExpect(matcher);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void deleteUnregisterSystem(final String systemName, final String address, final int port, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(delete(SERVICEREGISTRY_UNREGISTER_SYSTEM)
					.secure(true)
					.with(x509(certificatePath))
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, systemName)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, address)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, String.valueOf(port))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void getPullSystems(final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_PULL_SYSTEMS)
					.secure(true)
					.with(x509(certificatePath))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void postQuery(final ServiceQueryFormDTO form, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(SERVICEREGISTRY_QUERY)
			    	.secure(true)
			    	.with(x509(certificatePath))
			    	.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(form))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void postMultiQuery(final ServiceQueryFormListDTO form, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(SERVICEREGISTRY_MULTI_QUERY)
			    	.secure(true)
			    	.with(x509(certificatePath))
			    	.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(form))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void getQueryBySystemId(final long systemId, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_QUERY_BY_SYSTEM_ID.replace("{id}", String.valueOf(systemId)))
			    	.secure(true)
			    	.with(x509(certificatePath)))
					.andExpect(matcher);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void postQueryBySystemDTO(final SystemRequestDTO request, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO)
			    	.secure(true)
			    	.with(x509(certificatePath))
					.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(request))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
	

	//-------------------------------------------------------------------------------------------------
	private void getQueryAll(final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(get(SERVICEREGISTRY_QUERY_ALL)
			    	.secure(true)
			    	.with(x509(certificatePath)))
					.andExpect(matcher);
	}
}
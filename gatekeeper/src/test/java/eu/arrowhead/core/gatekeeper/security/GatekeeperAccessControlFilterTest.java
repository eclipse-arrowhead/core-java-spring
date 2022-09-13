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

package eu.arrowhead.core.gatekeeper.security;

import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

/**
* IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
*
*/
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class GatekeeperAccessControlFilterTest {

	//=================================================================================================
	// members
	
	private static final String GATEKEEPER_ECHO_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.ECHO_URI;
	private static final String GATEKEEPER_MGMT_CLOUDS_URI = CommonConstants.GATEKEEPER_URI + CoreCommonConstants.MGMT_URI + "/clouds";
	private static final String GATEKEEPER_INIT_GSD_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GSD_SERVICE;
	private static final String GATEKEEPER_INIT_ICN_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_ICN_SERVICE;
	private static final String GATEKEEPER_PULL_CLOUDS_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_PULL_CLOUDS_SERVICE;
	private static final String GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_SERVICE;
	private static final String GATEKEEPER_COLLECT_ACCESS_TYPES_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_COLLECT_ACCESS_TYPES_SERVICE;
	private static final String GATEKEEPER_GET_CLOUD_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GET_CLOUD_SERVICE;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockCnVerifier")
	private CommonNamePartVerifier cnVerifier;
	
	private MockMvc mockMvc;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		assumeTrue(secure);
		
		final GatekeeperAccessControlFilter gkFilter = appContext.getBean(GatekeeperAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(gkFilter)
									  .build();
		
		when(cnVerifier.isValid(anyString())).thenReturn(true);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateSameCloud() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_ECHO_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_ECHO_URI)
				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtCloudsCertificateSysop() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_MGMT_CLOUDS_URI)
				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtCloudsCertificateNotSysop() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_MGMT_CLOUDS_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitGSDCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_INIT_GSD_URI)
				    .secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getGSDQueryForm()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitGSDCertificateNotOrchestrator() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_INIT_GSD_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getGSDQueryForm()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitICNCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_INIT_ICN_URI)
				    .secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getICNRequestFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitICNCertificateNotOrchestrator() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_INIT_ICN_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getICNRequestFormDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPullCloudsCertificateQoSMonitor() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_PULL_CLOUDS_URI)
				    .secure(true)
					.with(x509("certificates/qosmonitor.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPullCloudsCertificateNotQoSMonitor() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_PULL_CLOUDS_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectSystemAddressesCertificateQoSMonitor() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI)
				    .secure(true)
					.with(x509("certificates/qosmonitor.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getCloudRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectSystemAddressesCertificateNotQoSMonitor() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(getCloudRequestDTO()))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectAccessTypesCertificateQoSMonitor() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_COLLECT_ACCESS_TYPES_URI)
				    .secure(true)
					.with(x509("certificates/qosmonitor.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(List.of(getCloudRequestDTO())))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectAccessTypesCertificateNotQoSMonitor() throws Exception {
		this.mockMvc.perform(post(GATEKEEPER_COLLECT_ACCESS_TYPES_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(List.of(getCloudRequestDTO())))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudTypesCertificateQoSMonitor() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_GET_CLOUD_URI + "/operator/  ")
				    .secure(true)
					.with(x509("certificates/qosmonitor.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //BadRequest means that request gone through on the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudTypesCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_GET_CLOUD_URI + "/operator/  ")
				    .secure(true)
					.with(x509("certificates/orchestrator.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); //BadRequest means that request gone through on the filter
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudTypesCertificateProvider() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_GET_CLOUD_URI + "/operator/name")
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GSDQueryFormDTO getGSDQueryForm() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");		
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		return gsdQueryFormDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ICNRequestFormDTO getICNRequestFormDTO() {
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName("test-sytem");
		system.setAddress("1.1.1.1");
		system.setPort(1000);
		
		final ICNRequestFormDTO icnRequestFormDTO = new ICNRequestFormDTO();
		icnRequestFormDTO.setRequesterSystem(system);
		icnRequestFormDTO.setRequestedService(requestedService);
		icnRequestFormDTO.setTargetCloudId(1L);
		
		return icnRequestFormDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getCloudRequestDTO() {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		return cloudRequestDTO;
	}
}
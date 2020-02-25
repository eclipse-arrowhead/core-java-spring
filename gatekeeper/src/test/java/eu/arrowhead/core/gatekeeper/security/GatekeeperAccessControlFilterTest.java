package eu.arrowhead.core.gatekeeper.security;

import static org.junit.Assume.assumeTrue;
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
	
	@Autowired
	private ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	private ObjectMapper objectMapper;
	
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
					.with(x509("certificates/qos_monitor.pem"))
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
					.with(x509("certificates/qos_monitor.pem"))
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
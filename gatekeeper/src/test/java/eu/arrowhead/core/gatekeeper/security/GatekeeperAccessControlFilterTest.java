package eu.arrowhead.core.gatekeeper.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;

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
	private static final String GATEKEEPER_MGMT_CLOUDS_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.MGMT_URI + "/clouds";
	
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
	public void testMgmtCloudsCertificateNoSysop() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_MGMT_CLOUDS_URI)
				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitGSDCertificateOrchestrator() throws Exception {
		this.mockMvc.perform(get(GATEKEEPER_MGMT_CLOUDS_URI)
				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	
//	private void f() {
//		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
//		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
//		
//		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
//		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
//		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
//		cloudRequestDTO.setOperator("test-operator");
//		cloudRequestDTO.setName("test-name");
//		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
//	}
}

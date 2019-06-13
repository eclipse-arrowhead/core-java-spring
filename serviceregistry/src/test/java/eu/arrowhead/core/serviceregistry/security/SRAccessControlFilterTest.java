package eu.arrowhead.core.serviceregistry.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.security.cert.CertificateException;

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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;

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

	private static final String SERVICE_REGISTRY_ECHO = CommonConstants.SERVICE_REGISTRY_URI + "/echo";
	private static final String SERVICE_REGISTRY_MGMT_SYSTEMS = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.MGMT_URI + "/systems";
	private static final String SERVICE_REGISTRY_REGISTER = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;

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
		
		final SRAccessControlFilter sracFilter = appContext.getBean(SRAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(sracFilter)
									  .build();
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_ECHO)
 				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtServiceNoSysop() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SYSTEMS)
 				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMgmtServiceSysop() throws Exception {
		this.mockMvc.perform(get(SERVICE_REGISTRY_MGMT_SYSTEMS)
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
	public void testRegisterSystemNameAndClientNameIgnoreUnderscoresMatch() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("c_l_i_e_n_t-demo-provider");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		
		postRegister(dto, "certificates/provider.pem", status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSystemNameAndClientNameIgnoreUnderscoresAndCaseInsensitiveMatch() throws Exception {
		// Filter enables the access but we use ill-formed input to make sure DB operation is never happened (without mocking it)
		final SystemRequestDTO systemDto = new SystemRequestDTO();
		systemDto.setSystemName("c_l_i_e_n_t-demo-Provider");
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setProviderSystem(systemDto);
		
		postRegister(dto, "certificates/provider.pem", status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void postRegister(final ServiceRegistryRequestDTO request, final String certificatePath, final ResultMatcher matcher) throws Exception {
		this.mockMvc.perform(post(SERVICE_REGISTRY_REGISTER)
			    	.secure(true)
			    	.with(x509(certificatePath))
			    	.contentType(MediaType.APPLICATION_JSON)
			    	.content(objectMapper.writeValueAsBytes(request))
			    	.accept(MediaType.APPLICATION_JSON))
					.andExpect(matcher);
	}
}
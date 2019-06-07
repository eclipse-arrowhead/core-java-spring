package eu.arrowhead.core.serviceregistry.security;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Ignore;
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

import eu.arrowhead.common.CommonConstants;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@Ignore
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class SRAccessControlFilterTest {
	
	@Autowired
	ApplicationContext appContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	public boolean secure;
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		assumeTrue(secure);
		
		final SRAccessControlFilter sracFilter = appContext.getBean(SRAccessControlFilter.class);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
									  .apply(springSecurity())
									  .addFilters(sracFilter)
									  .build();
		
	}
	
	@Test
	public void testEchoCertificateOtherCloud() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/echo")
 				    .secure(true)
					.with(x509("certificates/other_cloud.pem"))
					.accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void testMgmtServiceNoSysop() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
 				    .secure(true)
					.with(x509("certificates/provider.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void testMgmtServiceSysop() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
 				    .secure(true)
					.with(x509("certificates/valid.pem"))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
}
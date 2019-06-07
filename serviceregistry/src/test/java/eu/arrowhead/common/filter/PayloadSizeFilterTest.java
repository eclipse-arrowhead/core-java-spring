package eu.arrowhead.common.filter;

import static org.junit.Assume.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.arrowhead.core.serviceregistry.ServiceRegistryMain;
import eu.arrowhead.core.serviceregistry.security.SRAccessControlFilter;

/**
 * IMPORTANT: These tests may fail if the certificates are changed in the src/main/resources folder. 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@AutoConfigureMockMvc
public class PayloadSizeFilterTest {

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
	public void testPayloadFilterGetWithBody() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/echo")
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.content("{ \"abc\": \"def\" }")
				    .accept(MediaType.TEXT_PLAIN))
					.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testPayloadFilterPostWithoutBody() throws Exception {
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
					.secure(true)
					.with(x509("certificates/valid.pem"))
					.contentType(MediaType.APPLICATION_JSON)
				    .accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
}
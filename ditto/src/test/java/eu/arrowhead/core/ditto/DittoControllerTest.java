package eu.arrowhead.core.ditto;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import eu.arrowhead.common.CommonConstants;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DittoTestContext.class})
public class DittoControllerTest {

	//=================================================================================================
	// members

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	private static final String DITTO_ECHO_URI = CommonConstants.DITTO_URI + CommonConstants.ECHO_URI;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void echo() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(DITTO_ECHO_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		assertEquals("Got it!", response.getResponse().getContentAsString());
	}

}

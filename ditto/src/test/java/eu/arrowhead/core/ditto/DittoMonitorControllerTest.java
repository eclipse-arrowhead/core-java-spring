package eu.arrowhead.core.ditto;

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
import eu.arrowhead.common.dto.internal.InventoryIdDTO;
import eu.arrowhead.common.dto.internal.SystemDataDTO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DittoTestContext.class})
public class DittoMonitorControllerTest {

	//=================================================================================================
	// members

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static final String BASE_URI = CommonConstants.DITTO_URI + CommonConstants.MONITOR_URI;

	//=================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	//=================================================================================================
	// Tests of Echo service

	@Test
	public void ping() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(BASE_URI + CommonConstants.PING_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		assertEquals("OK", response.getResponse().getContentAsString());
	}

	@Test
	public void systemData() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(BASE_URI + CommonConstants.SYSTEM_DATA_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		final String content = response.getResponse().getContentAsString();
		final Map<String, String> systemData = objectMapper.readValue(content, SystemDataDTO.class).getSystemData();
		assertEquals("ditto", systemData.get("systemName"));
		assertEquals(DittoMonitorController.SYSTEM_DESCRIPTION, systemData.get("description"));
	}

	@Test
	public void inventoryId() throws Exception {
		final MvcResult response = this.mockMvc.perform(get(BASE_URI + CommonConstants.INVENTORY_ID_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		final String content = response.getResponse().getContentAsString();
		InventoryIdDTO inventoryId = objectMapper.readValue(content, InventoryIdDTO.class);
		assertNull(inventoryId.getInventoryId());
	}

}

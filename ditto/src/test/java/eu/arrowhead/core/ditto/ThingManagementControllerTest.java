package eu.arrowhead.core.ditto;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import eu.arrowhead.common.dto.internal.ThingRequestDTO;
import eu.arrowhead.core.ditto.service.DittoHttpClient;

@RunWith(SpringRunner.class)
@SpringBootTest()
@ContextConfiguration(classes = {DittoTestContext.class})
public class ThingManagementControllerTest {

	// =================================================================================================
	// members

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockDittoHttpClient")
	private DittoHttpClient dittoHttpClient;

	private MockMvc mockMvc;

	private final String THING_ID = "x:y:z";
	private final String THING_DEFINITION = "a:b:c";

	final String thingJson =
			"{\"thingId\":\"" + THING_ID + "\",\"policyId\":\"" + Constants.DITTO_POLICY_ID
					+ "\",\"definition\":\"" + THING_DEFINITION + "\",\"attributes\":{},\"features\":{}}";

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void getThings() throws Exception {

		final String mockResponseBody = "{<MOCK_DATA>}";
		final ResponseEntity<String> mockedResponse =
				new ResponseEntity<>(mockResponseBody, HttpStatus.OK);
		Mockito.when(dittoHttpClient.getThings()).thenReturn(mockedResponse);

		final MvcResult response = this.mockMvc.perform(get(Constants.THING_MGMT_URI)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		assertEquals(mockResponseBody, response.getResponse().getContentAsString());
	}

	@Test
	public void getThing() throws Exception {
		final String thingUri = Constants.THING_MGMT_URI + "/" + THING_ID;
		final String mockResponseBody = "{<MOCK_RESPONSE_BODY>}";
		final ResponseEntity<String> mockedResponse =
				new ResponseEntity<>(mockResponseBody, HttpStatus.OK);
		Mockito.when(dittoHttpClient.getThing(THING_ID)).thenReturn(mockedResponse);

		final MvcResult response = this.mockMvc.perform(get(thingUri)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		assertEquals(mockResponseBody, response.getResponse().getContentAsString());
	}

	@Test
	public void putThing() throws Exception {

		final ThingRequestDTO thingRequestDTO =
				new ThingRequestDTO(THING_DEFINITION, null, Collections.emptyMap(), Collections.emptyMap());
		final String thingRequestJson = objectMapper.writeValueAsString(thingRequestDTO);
		final String thingUri = Constants.THING_MGMT_URI + "/" + THING_ID;
		final String mockResponseBody = "{<MOCK_RESPONSE_BODY>}";
		final ResponseEntity<String> mockedResponse =
				new ResponseEntity<>(mockResponseBody, HttpStatus.CREATED);

		Mockito.when(dittoHttpClient.putThing(THING_ID, thingJson)).thenReturn(mockedResponse);

		final MockHttpServletRequestBuilder requestBuilder = put(thingUri)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(thingRequestJson);

		// TODO: Check that an event is published

		final MvcResult response = this.mockMvc.perform(requestBuilder)
				.andExpect(status().isCreated())
				.andReturn();
		assertEquals(mockResponseBody, response.getResponse().getContentAsString());
	}

	@Test
	public void deleteThing() throws Exception {

		final String thingUri = Constants.THING_MGMT_URI + "/" + THING_ID;

		final ResponseEntity<String> getResponse = new ResponseEntity<>(thingJson, HttpStatus.OK);
		final ResponseEntity<Void> deletionResponse = new ResponseEntity<>(null, HttpStatus.OK);

		Mockito.when(dittoHttpClient.getThing(THING_ID)).thenReturn(getResponse);
		Mockito.when(dittoHttpClient.deleteThing(THING_ID)).thenReturn(deletionResponse);

		// TODO: Check that an event is published

		this.mockMvc.perform(delete(thingUri)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}

}

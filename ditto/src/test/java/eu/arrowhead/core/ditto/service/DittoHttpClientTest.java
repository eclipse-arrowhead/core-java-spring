package eu.arrowhead.core.ditto.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import eu.arrowhead.core.ditto.Constants;

// @AutoConfigureWebClient(registerRestTemplate = true)
@RunWith(SpringRunner.class)
public class DittoHttpClientTest {

	//=================================================================================================
	// members

	@InjectMocks
	private DittoHttpClient testingObject;

	@Value(Constants.$DITTO_HTTP_ADDRESS_WD)
	private String dittoAddress;

	@Mock
	RestTemplate restTemplate;

	final ResponseEntity<String> mockedResponse = new ResponseEntity<>("res-xyz", HttpStatus.OK);

	//=================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------

	@Before
	public void initMocks() {
			MockitoAnnotations.initMocks(this);
	}

	@Before
	public void initConfig() {
			ReflectionTestUtils.setField(testingObject, "dittoAddress", dittoAddress);
			ReflectionTestUtils.setField(testingObject, "dittoUsername", "ditto");
			ReflectionTestUtils.setField(testingObject, "dittoPassword", "ditto");
	}

	@Test
	public void getThing() {
		final String thingId = "thing-abc";
		final String dittoThingUri = dittoAddress + "/api/2/things/" + thingId;

		Mockito.when(restTemplate.exchange(
				eq(dittoThingUri),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(mockedResponse);

		ResponseEntity<String> response = testingObject.getThing(thingId);

		assertEquals(mockedResponse.getBody(), response.getBody());
	}

	@Test
	public void getThings() {
		final String dittoThingsUri = dittoAddress + "/api/2/search/things/";
		Mockito.when(restTemplate.exchange(
				eq(dittoThingsUri),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(mockedResponse);

		ResponseEntity<String> response = testingObject.getThings();
		assertEquals(mockedResponse.getBody(), response.getBody());
	}

	@Test
	public void getProperty() {
		final String thingId = "thing-a";
		final String featureId = "feature-b";
		final String propertyId = "property-c";

		final String DITTO_PROPERTY_URI_TEMPLATE =
				dittoAddress + "/api/2/things/%s/features/%s/properties/%s";
		final String dittoPropertiesUri =
				String.format(DITTO_PROPERTY_URI_TEMPLATE, thingId, featureId, propertyId);
		Mockito.when(restTemplate.exchange(
				eq(dittoPropertiesUri),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(mockedResponse);

		ResponseEntity<String> response = testingObject.getProperty(thingId, featureId, propertyId);
		assertEquals(mockedResponse.getBody(), response.getBody());
	}

}

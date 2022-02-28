/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.ditto.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import eu.arrowhead.common.dto.internal.ThingRequestDTO;
import eu.arrowhead.core.ditto.Constants;

@Component
public class DittoHttpClient {

	//=================================================================================================
	// members

	final private static String DITTO_THINGS_SEARCH_URI = "/api/2/search/things/";
	final private static String DITTO_THINGS_URI = "/api/2/things/";
	final private static String DITTO_PROPERTY_URI_TEMPLATE = "/api/2/things/%s/features/%s/properties/%s";

	@Value(Constants.$DITTO_HTTP_ADDRESS_WD)
	private String dittoAddress;

	@Value(Constants.$DITTO_USERNAME)
	private String dittoUsername;

	@Value(Constants.$DITTO_PASSWORD)
	private String dittoPassword;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private final Logger logger = LogManager.getLogger(DittoHttpClient.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> getThings() {
		return sendGetRequest(DITTO_THINGS_SEARCH_URI);
	}

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> getThing(final String thingId) {
		Assert.notNull(thingId, "thingId is null");
		return sendGetRequest(DITTO_THINGS_URI + thingId);
	}

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> getProperty(
			final String thing,
			final String feature,
			final String property) {
		Assert.notNull(thing, "Thing is null");
		Assert.notNull(feature, "Feature is null");
		Assert.notNull(property, "Property is null");

		final String path = String.format(DITTO_PROPERTY_URI_TEMPLATE, thing, feature, property);
		return sendGetRequest(path);
	}

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> registerThing(final String thingId, final ThingRequestDTO thing) {
		String stringValue = null;
		try {
			stringValue = objectMapper.writeValueAsString(thing);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendPutRequest(DITTO_THINGS_URI + thingId, stringValue);
	}

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> unregisterThing(final String thingId) {
		return sendDeleteRequest(DITTO_THINGS_URI + thingId);
	}

	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<String> sendGetRequest(final String path) {
		return sendRequest(HttpMethod.GET, path, null);
	}

	// -------------------------------------------------------------------------------------------------
	private ResponseEntity<String> sendPutRequest(final String path, final String body) {
		return sendRequest(HttpMethod.PUT, path, body);
	}

	// -------------------------------------------------------------------------------------------------
	private ResponseEntity<String> sendDeleteRequest(final String path) {
		return sendRequest(HttpMethod.DELETE, path, null);
	}

	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<String> sendRequest(final HttpMethod method, final String path, final String body) {
		final String uri = dittoAddress + path;
		final HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(dittoUsername, dittoPassword);
		if (body != null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
		}
		final HttpEntity<String> request = new HttpEntity<>(body, headers);
		logger.debug("Sending HTTP " + method + " request to Eclipse Ditto, URI " + uri);
		return restTemplate.exchange(uri, method, request, String.class);
	}
}

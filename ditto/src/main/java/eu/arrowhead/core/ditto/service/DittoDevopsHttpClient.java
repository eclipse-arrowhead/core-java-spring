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
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import eu.arrowhead.core.ditto.Constants;
import eu.arrowhead.core.ditto.DittoDevopsCommands;

@Service
public class DittoDevopsHttpClient {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(DittoDevopsHttpClient.class);

	private static final String DITTO_CONNECTIVITY_URI = "/devops/piggyback/connectivity";

	@Value(Constants.$DITTO_HTTP_ADDRESS_WD)
	private String dittoAddress;

	@Value(Constants.$DITTO_DEVOPS_USERNAME)
	private String dittoDevopsUsername;

	@Value(Constants.$DITTO_DEVOPS_PASSWORD)
	private String dittoDevopsPassword;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DittoDevopsCommands dittoDevopsCommands;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> getConnections() {
		// TODO: Implement!
		throw new NotImplementedException("Not implemented!");
	}

	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<JsonNode> getConnection(final String connectionId, final String methode) {
		final String command = dittoDevopsCommands.create(connectionId, methode);
		ResponseEntity<JsonNode> response = sendPostRequest(DITTO_CONNECTIVITY_URI, command);
		final JsonNode contents = response.getBody().get("?").get("?");
		final int statusCode = contents.get("status").asInt();
		return new ResponseEntity<>(contents, HttpStatus.valueOf(statusCode));
	}

	public ResponseEntity<JsonNode> putConnection(final JsonNode connection, final String methode) {
		final String command = dittoDevopsCommands.create(connection, methode);
		System.out.println(command);
		ResponseEntity<JsonNode> response = sendPostRequest(DITTO_CONNECTIVITY_URI, command);

		// TODO: Figure out why the response has this format: {{"?": "?": { ... }}}
		final JsonNode contents = response.getBody().get("?").get("?");
		final int statusCode = contents.get("status").asInt();
		return new ResponseEntity<JsonNode>(contents, HttpStatus.valueOf(statusCode));
	}

	public ResponseEntity<String> deleteConnection(final String connectionId) {
		throw new NotImplementedException("Not implemented!");
	}

	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<JsonNode> sendPostRequest(final String path, final String body) {
		return sendRequest(HttpMethod.POST, path, body, JsonNode.class);
	}

	//-------------------------------------------------------------------------------------------------
	private <T> ResponseEntity<T> sendRequest(
			final HttpMethod method,
			final String path,
			final String body,
			Class<T> responseType) {
		final String uri = dittoAddress + path;
		final HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(dittoDevopsUsername, dittoDevopsPassword);
		if (body != null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
		}
		final HttpEntity<String> request = new HttpEntity<>(body, headers);
		logger.debug("Sending HTTP " + method + " request to Eclipse Ditto, URI " + uri);

		try {
			return restTemplate.exchange(uri, method, request, responseType);
		} catch (final HttpClientErrorException e) {
			return new ResponseEntity<>(e.getStatusCode());
		} catch (Exception e) {
			logger.error(e);
			return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
		}
	}

}

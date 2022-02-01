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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

@Service
public class DittoHttpClient {

	// =================================================================================================
	// members

	// TODO: Move property names to CommonConstants?

	@Value("${ditto_http_address}")
	private String dittoAddress;

	@Value("${ditto_username}")
	private String dittoUsername;

	@Value("${ditto_password}")
	private String dittoPassword;

	private final RestTemplate restTemplate = new RestTemplate();

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	public ResponseEntity<String> sendGetRequest(final String path) {
		Assert.notNull(path, "Path is null");
		final String uri = dittoAddress + path;
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(dittoUsername, dittoPassword);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		return restTemplate.exchange(uri, HttpMethod.GET,
				request, String.class);
	}
}

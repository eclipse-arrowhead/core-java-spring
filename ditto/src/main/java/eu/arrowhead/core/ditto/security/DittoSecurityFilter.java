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

package eu.arrowhead.core.ditto.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.common.token.TokenSecurityFilter;
import eu.arrowhead.common.token.TokenUtilities.TokenInfo;

public class DittoSecurityFilter extends TokenSecurityFilter {

	//=================================================================================================
	// members

	private PrivateKey myPrivateKey;

	private Map<String, Object> arrowheadContext;
	private HttpService httpService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DittoSecurityFilter(final Map<String, Object> arrowheadContext, final HttpService httpService) {
		Assert.notNull(arrowheadContext, "arrowheadContext is null");
		Assert.notNull(httpService, "httpService is null");
		this.arrowheadContext = arrowheadContext;
		this.httpService = httpService;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected PrivateKey getMyPrivateKey() {
		return myPrivateKey;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected PublicKey getAuthorizationPublicKey() {
		// TODO: Return cached value.
		return queryAuthorizationPublicKey();
	}

	//-------------------------------------------------------------------------------------------------
	public void setMyPrivateKey(final PrivateKey myPrivateKey) {
		Assert.notNull(myPrivateKey, "myPrivateKey cannot be null");
		this.myPrivateKey = myPrivateKey;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected TokenInfo checkToken(
			final String clientCN,
			final String token,
			final String requestTarget) {
		String feature = null;
		try {
			// TODO: Fix this up, make it safe.
			final URL url = new URL(requestTarget);
			String[] parts = url.getPath().split("/things/|/features/|/properties/");
			feature = parts[2];
		} catch (MalformedURLException e) {
			throw new AuthException("Malformed URL");
		}
		TokenInfo tokenInfo = super.checkToken(clientCN, token, requestTarget);
		if (!tokenInfo.getService().equals(feature)) {
			throw new AuthException(
					"The requested feature did not match the service specified in the token");
		}
		return tokenInfo;
	}

	//-------------------------------------------------------------------------------------------------
	public PublicKey queryAuthorizationPublicKey() {

		final ResponseEntity<String> response =
				httpService.sendRequest(getAuthPublicKeyUri(), HttpMethod.GET, String.class);

		final String encodedKey = Utilities.fromJson(response.getBody(), String.class);
		return Utilities.getPublicKeyFromBase64EncodedString(encodedKey);
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthPublicKeyUri() {
		final String errorMessage = "Can't find Authorization system's public key service URI.";
		final String key = CoreSystemService.AUTH_PUBLIC_KEY_SERVICE.getServiceDefinition()
				+ CoreCommonConstants.URI_SUFFIX;

		if (arrowheadContext.containsKey(key)) {
			try {
				return (UriComponents) arrowheadContext.get(key);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException(errorMessage, ex);
			}
		}
		throw new ArrowheadException(errorMessage);
	}

}

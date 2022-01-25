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

import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;
import javax.annotation.Resource;

@Service
public class DittoService {

	// =================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(DittoService.class);

	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	protected SSLProperties sslProperties;

	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String address;
	
	@Value(CoreCommonConstants.$SERVER_PORT)
	private int port;
	
	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String systemName;

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	public String registerService() {
		final UriComponents uri = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host("localhost")
				.port(8443)
				.path("/serviceregistry/register")
				.build();

		final ResponseEntity<ServiceRegistryResponseDTO> response = httpService.sendRequest(
				uri, HttpMethod.POST, ServiceRegistryResponseDTO.class, getServiceRegistryRequest());
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody().getServiceUri());
		return response.getBody().getServiceUri();
	}

	// -------------------------------------------------------------------------------------------------
	private ServiceRegistryRequestDTO getServiceRegistryRequest() {
		final ServiceRegistryRequestDTO request = new ServiceRegistryRequestDTO();
		request.setProviderSystem(getSystemDescription());
		request.setServiceDefinition("service-xyz");
		request.setServiceUri("/path/to/service");
		request.setSecure(sslProperties.isSslEnabled() ? ServiceSecurityType.CERTIFICATE.name()
				: ServiceSecurityType.NOT_SECURE.name());
		request.setInterfaces(List.of(CommonConstants.HTTP_SECURE_JSON));
		return request;
	}

	// -------------------------------------------------------------------------------------------------
	private SystemRequestDTO getSystemDescription() {
		final SystemRequestDTO system = new SystemRequestDTO();
		system.setAddress("127.0.0.1");
		system.setPort(port);
		system.setSystemName(systemName);
		
		if (sslEnabled) {
			final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
			final String authInfo = Base64.getEncoder().encodeToString(publicKey.getEncoded());
			system.setAuthenticationInfo(authInfo);
		}
		return system;
	}

}

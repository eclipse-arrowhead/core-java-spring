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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.common.security.DefaultSecurityConfig;

@Configuration
@EnableWebSecurity
public class DittoSecurityConfig extends DefaultSecurityConfig {

	//=================================================================================================
	// members

	@Autowired
	protected SSLProperties sslProperties;

	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String, Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	public FilterRegistrationBean<DittoSecurityFilter> filterRegistrationBean() {
		FilterRegistrationBean<DittoSecurityFilter> registrationBean =
				new FilterRegistrationBean<DittoSecurityFilter>();
		DittoSecurityFilter dittoSecurityFilter = new DittoSecurityFilter(arrowheadContext, httpService);

		dittoSecurityFilter.setMyPrivateKey(getMyPrivateKey());
		registrationBean.setFilter(dittoSecurityFilter);
		registrationBean.addUrlPatterns("/things/*"); // TODO: Move constant somewhere else.
		return registrationBean;
	}

	// -------------------------------------------------------------------------------------------------
	private PrivateKey getMyPrivateKey() {
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
			keystore.load(sslProperties.getKeyStore().getInputStream(),
					sslProperties.getKeyStorePassword().toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
			throw new ArrowheadException(ex.getMessage());
		}
		return Utilities.getPrivateKey(keystore, sslProperties.getKeyPassword());
	}

}

/********************************************************************************
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.ditto.security;

import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
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
	private Map<String, Object> context;

	final static String THINGS_URL_PATTERN = "/things/*";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	public FilterRegistrationBean<DittoSecurityFilter> filterRegistrationBean() {
		FilterRegistrationBean<DittoSecurityFilter> registrationBean =
				new FilterRegistrationBean<DittoSecurityFilter>();
		final DittoSecurityFilter dittoSecurityFilter = new DittoSecurityFilter(context, httpService);
		registrationBean.setFilter(dittoSecurityFilter);
		registrationBean.addUrlPatterns(THINGS_URL_PATTERN);
		return registrationBean;
	}

}

package eu.arrowhead.client.skeleton.provider.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import eu.arrowhead.client.skeleton.common.config.DefaultSecurityConfig;
import eu.arrowhead.client.skeleton.common.util.ClientCommonConstants;
import eu.arrowhead.common.CommonConstants;

@Configuration
@EnableWebSecurity
public class ProviderSecurityConfig extends DefaultSecurityConfig {
	
	//=================================================================================================
	// members
	
	@Value(ClientCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	private ProviderTokenSecurityFilter tokenSecurityFilter;
	
	//=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		if (tokenSecurityFilterEnabled) {
			tokenSecurityFilter = new ProviderTokenSecurityFilter();
			http.addFilterAfter(tokenSecurityFilter, SecurityContextHolderAwareRequestFilter.class);			
		} else if (sslEnabled) {
			http.addFilterAfter(new ProviderAccessControlFilter(), SecurityContextHolderAwareRequestFilter.class);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ProviderTokenSecurityFilter getTokenSecurityFilter() {
		return tokenSecurityFilter;
	}	
}

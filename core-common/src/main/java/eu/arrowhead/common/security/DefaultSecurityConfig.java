package eu.arrowhead.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.filter.InboundDebugFilter;
import eu.arrowhead.common.filter.PayloadSizeFilter;

public class DefaultSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Value(CommonConstants.$LOG_ALL_REQUEST_AND_RESPONSE_WD)
	private boolean debugMode;
	
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
    	http.httpBasic().disable()
    	    .csrf().disable()
    		.addFilterAfter(new PayloadSizeFilter(), SecurityContextHolderAwareRequestFilter.class);
    	if (sslEnabled) {
    		http.requiresChannel().anyRequest().requiresSecure();
    	}
    	
    	if (debugMode) {
    		http.addFilterAfter(new InboundDebugFilter(), X509AuthenticationFilter.class);
    	}
    }
}
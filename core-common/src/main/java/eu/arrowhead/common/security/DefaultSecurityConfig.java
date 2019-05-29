package eu.arrowhead.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import eu.arrowhead.common.CommonConstants;

public class DefaultSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http = http.httpBasic().disable()
    			   .csrf().disable();
    	if (sslEnabled) {
    		http.requiresChannel().anyRequest().requiresSecure();
    		http.addFilterAfter(new AccessControlFilter(), SecurityContextHolderAwareRequestFilter.class);
    	}
    }
}
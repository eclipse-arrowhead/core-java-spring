package eu.arrowhead.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public class DefaultSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED)
	private boolean sslEnabled;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http = http.httpBasic().disable();
    	if (sslEnabled) {
    		http.requiresChannel().anyRequest().requiresSecure();
    	}
    }
}
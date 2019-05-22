package eu.arrowhead.core.gatekeeper.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import eu.arrowhead.common.DefaultSecurityConfig;

@Configuration
@EnableWebSecurity
public class GKSecurityConfig extends DefaultSecurityConfig {

}

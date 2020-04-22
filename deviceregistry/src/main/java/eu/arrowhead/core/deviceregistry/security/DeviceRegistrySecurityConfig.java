package eu.arrowhead.core.deviceregistry.security;

import eu.arrowhead.common.security.DefaultSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class DeviceRegistrySecurityConfig extends DefaultSecurityConfig {

}
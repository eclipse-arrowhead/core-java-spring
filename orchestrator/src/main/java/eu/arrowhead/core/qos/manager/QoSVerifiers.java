package eu.arrowhead.core.qos.manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.qos.manager.impl.ServiceTimeVerifier;

@Configuration
public class QoSVerifiers {
	
	//=================================================================================================
	// members
	
	public static final String SERVICE_TIME_VERIFIER = "serviceTimeVerifier";

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(SERVICE_TIME_VERIFIER)
	public QoSVerifier getServiceTimeVerifier() {
		return new ServiceTimeVerifier();
	}
}
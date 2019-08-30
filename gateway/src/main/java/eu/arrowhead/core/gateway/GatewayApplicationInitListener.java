package eu.arrowhead.core.gateway;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;

@Component
public class GatewayApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	public ConcurrentHashMap<String,ActiveSessionDTO> getActiveSessions() {
		return new ConcurrentHashMap<>();
	}
	
	//TODO: maybe in customDestroy close all active sessions
}
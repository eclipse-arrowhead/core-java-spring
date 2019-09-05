package eu.arrowhead.core.gateway;

import java.util.Queue;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;

@Component
public class GatewayApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Value(CommonConstants.$GATEWAY_MIN_PORT_WD)
	private int minPort;
	
	@Value(CommonConstants.$GATEWAY_MAX_PORT_WD)
	private int maxPort;


	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	public ConcurrentMap<String,ActiveSessionDTO> getActiveSessions() {
		return new ConcurrentHashMap<>();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE)
	public Queue<Integer> getAvailablePorts() {
		final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
		if (minPort <= maxPort) {
			for (int i = minPort; i <= maxPort; ++i) {
				queue.offer(i);
			}
		}
		
		return queue;
	}
	
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		if (!sslProperties.isSslEnabled()) {
			throw new ServiceConfigurationError("Gateway can only started in SECURE mode!");
		}
		
		if (minPort > maxPort) {
			throw new ServiceConfigurationError("Available port interval is invalid: [" + minPort + " - " + maxPort + "]");
		}
	}

	//TODO: maybe in customDestroy close all active sessions
}
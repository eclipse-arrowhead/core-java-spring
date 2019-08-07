package eu.arrowhead.core.gatekeeper;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.RelayClientFactory;

@Component
public class GatekeeperApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private long timeout;
	
	@Value(CommonConstants.$NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS_WD)
	private int noWorkers;
	
	private Set<Session> openConnections = new HashSet<>();
	private GatekeeperRelayClient gatekeeperRelayClient;

	//=================================================================================================
	// assistant methods
		
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		if (!sslProperties.isSslEnabled()) {
			throw new ServiceConfigurationError("Gatekeeper can only started in SECURE mode!");
		}
		
		initializeGatekeeperRelayClient(event.getApplicationContext());
		subscribeListenersToGatekeepers();
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {
		for (final Session session : openConnections) {
				gatekeeperRelayClient.closeConnection(session);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void initializeGatekeeperRelayClient(final ApplicationContext appContext) {
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
		final PublicKey publicKey = (PublicKey) context.get(CommonConstants.SERVER_PUBLIC_KEY);
		final PrivateKey privateKey = (PrivateKey) context.get(CommonConstants.SERVER_PRIVATE_KEY);

		this.gatekeeperRelayClient = RelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, timeout);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void subscribeListenersToGatekeepers() {
		final Set<Relay> gatekeeperRelays = gatekeeperDBService.getAllLiveGatekeeperRelays();
		for (final Relay relay : gatekeeperRelays) {
			try {
				final Session session = gatekeeperRelayClient.createConnection(relay.getAddress(), relay.getPort());
				openConnections.add(session);
				final MessageConsumer consumer = gatekeeperRelayClient.subscribeGeneralAdvertisementTopic(session);
				consumer.setMessageListener(null); // TODO: continue set here a valid listener
			} catch (final JMSException | ArrowheadException ex) {
				logger.debug("Error while trying to subscribe relay {}:{}", relay.getAddress(), relay.getPort()); // we skip the wrong ones
			}
		}
	}
}
package eu.arrowhead.core.ditto.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.ditto.client.DisconnectedDittoClient;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.things.model.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import com.neovisionaries.ws.client.WebSocket;

@Service
public class DittoWsClient {

	// =================================================================================================
	// members

	// TODO: Change these values.
	final String WS_ENDPOINT = "ws://localhost:8080";
	final String DITTO_USERNAME = "ditto";
	final String DITTO_PASSWORD = "ditto";
	final String THING_REGISTRATION_ID = "THING_REGISTRATION_ID";

	private final Logger logger = LogManager.getLogger(DittoWsClient.class);

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	AuthenticationProvider<WebSocket> authenticationProvider =
			AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
					.username(DITTO_USERNAME)
					.password(DITTO_PASSWORD)
					.build());

	MessagingProvider messagingProvider =
			MessagingProviders.webSocket(WebSocketMessagingConfiguration
					.newBuilder()
					.endpoint(WS_ENDPOINT)
					.build(), authenticationProvider);

	DisconnectedDittoClient disconnectedDittoClient = DittoClients.newInstance(messagingProvider);

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@PostConstruct
	private void init() {
		logger.debug("Connecting to Ditto's WebSocket API");
		disconnectedDittoClient.connect()
				.thenAccept(this::onConnected)
				.exceptionally(this::onError);
	}

	// -------------------------------------------------------------------------------------------------
	private void onConnected(final DittoClient client) {
		logger.debug("Connected to Ditto's WebSocket API");
		this.subscribeForTwinEvents(client);
		client.twin().registerForThingChanges(THING_REGISTRATION_ID, this::onThingChange);
	}

	// -------------------------------------------------------------------------------------------------
	private void subscribeForTwinEvents(final DittoClient client) {
		try {
			client.twin().startConsumption().toCompletableFuture().get(); // this will block the thread!
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to connect to Ditto's WebSocket API", e);
		}
		logger.debug("Subscribed for Ditto Twin events");
	}

	// -------------------------------------------------------------------------------------------------
	private void onThingChange(ThingChange change) {
		final Optional<Thing> thing = change.getThing();
		logger.debug("Thing change detected. Action: " + change.getAction() + ", " + "Thing: " + thing);

		ThingEvent event = new ThingEvent(this, change);
		eventPublisher.publishEvent(event);
	}

	// -------------------------------------------------------------------------------------------------
	private Void onError(final Throwable e) {
		logger.error("Failed to connect to Ditto's WebSocket API", e);
		disconnectedDittoClient.destroy();
		return null;
	}

}

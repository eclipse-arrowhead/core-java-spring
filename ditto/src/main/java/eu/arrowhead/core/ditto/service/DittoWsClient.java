/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

 package eu.arrowhead.core.ditto.service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import com.neovisionaries.ws.client.WebSocket;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import eu.arrowhead.core.ditto.Constants;

@Service
public class DittoWsClient {

	// =================================================================================================
	// members

	@Value(Constants.$DITTO_WS_ADDRESS_WD)
	private String dittoWsAddress;

	@Value(Constants.$DITTO_USERNAME)
	private String dittoUsername;

	@Value(Constants.$DITTO_PASSWORD)
	private String dittoPassword;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	private final Logger logger = LogManager.getLogger(DittoWsClient.class);

	// Arbitrary ID which can be used to cancel the registration later on:
	final String THING_REGISTRATION_ID = "THING_REGISTRATION_ID";

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@PostConstruct
	private void init() {

		AuthenticationProvider<WebSocket> authenticationProvider =
				AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
						.username(dittoUsername)
						.password(dittoPassword)
						.build());

		MessagingProvider messagingProvider =
				MessagingProviders.webSocket(WebSocketMessagingConfiguration
						.newBuilder()
						.endpoint(dittoWsAddress)
						.build(), authenticationProvider);

		logger.debug("Connecting to Ditto's WebSocket API");

		DisconnectedDittoClient dittoClient = DittoClients.newInstance(messagingProvider);
		dittoClient.connect()
				.thenAccept(this::onConnected)
				.exceptionally((final Throwable e) -> {
					logger.error("Failed to connect to Ditto's WebSocket API", e);
					dittoClient.destroy();
					return null;
				});
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

		ThingChangeEvent event = new ThingChangeEvent(this, change);
		eventPublisher.publishEvent(event);
	}

}

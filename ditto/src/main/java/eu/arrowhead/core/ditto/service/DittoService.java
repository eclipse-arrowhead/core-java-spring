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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class DittoService implements ApplicationListener<ThingChangeEvent> {

	// =================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(DittoService.class);

	private static final String BASE_THING_SERVICE_URI = "/things";

	@Autowired
	private ServiceRegistryClient serviceRegistryClient;

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@Override
	public void onApplicationEvent(final ThingChangeEvent event) {
		Assert.notNull(event, "Thing change event is null");

		final ThingChange change = event.getChange();

			if (change.getThing().isEmpty()) {
				logger.error("No Thing present in ThingChange");
				return;
			}

			Thing thing = change.getThing().get();
			switch (change.getAction()) {
				case CREATED:
					registerServices(thing);
					break;
				case UPDATED:
					unregisterServices(thing);
					registerServices(thing);
					break;
				case DELETED:
					unregisterServices(thing);
					break;
				default:
					logger.debug("Unhandled ThingChange");
					break;
			}
	}

	// -------------------------------------------------------------------------------------------------
	private void registerServices(Thing thing) {

		if (thing.getEntityId().isEmpty()) {
			logger.error("No EntityId present in Thing");
			return;
		}

		final String entityId = thing.getEntityId().get().toString();
		final Optional<Features> features = thing.getFeatures();

		logger.debug("Registering services for thing '" + entityId + "'");

		if (features.isPresent()) {
			for (final Feature feature : features.get()) {
				this.registerFeature(entityId, feature);
			}
		}
	}

	// -------------------------------------------------------------------------------------------------
	private void unregisterServices(Thing thing) {
		// TODO: Implement!
	}
	
	// -------------------------------------------------------------------------------------------------
	private void registerFeature(String entityId, Feature feature) {
		final String serviceDefinition = getServiceDefinition(feature);
		final String serviceUri = BASE_THING_SERVICE_URI + "/" + entityId + "/" + feature.getId();
		serviceRegistryClient.registerService(serviceDefinition, serviceUri);
	}

	private static String getServiceDefinition(Feature feature) {
		// TODO: Allow some way for the requester to specify the service definition.
		// TODO: Ensure that a valid service definition is returned.
		return feature.getId();
	}

}

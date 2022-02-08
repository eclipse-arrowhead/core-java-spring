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

import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.Attributes;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.ditto.Constants;
import eu.arrowhead.core.ditto.DittoModelException;

@Service
public class DittoService implements ApplicationListener<ThingChangeEvent> {

	// =================================================================================================
	// members

	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE = "Service definition has invalid format. Service definition only contains maximum 63 character of letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";

	private final Logger logger = LogManager.getLogger(DittoService.class);

	@Autowired
	private ServiceRegistryClient serviceRegistryClient;

	@Autowired
	private CommonNamePartVerifier cnVerifier;

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
		try {

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
		} catch (DittoModelException ex) {
			logger.error(ex);
		}
	}

	// -------------------------------------------------------------------------------------------------
	private void registerServices(final Thing thing) throws DittoModelException {

		if (thing.getEntityId().isEmpty()) {
			logger.error("No EntityId present in Thing");
			return;
		}

		final String entityId = thing.getEntityId().get().toString();
		final Optional<Features> features = thing.getFeatures();

		logger.debug("Registering services for thing '" + entityId + "'");

		if (features.isPresent()) {
			final Optional<JsonObject> serviceDefinitions = getServiceDefinitions(thing);
			for (final Feature feature : features.get()) {
				final Optional<String> serviceDefinition =
						getServiceDefinition(serviceDefinitions, feature.getId());
				this.registerFeature(entityId, feature, serviceDefinition);
			}
		}
	}

	// -------------------------------------------------------------------------------------------------
	private void unregisterServices(Thing thing) {
		// TODO: Implement!
	}

	// -------------------------------------------------------------------------------------------------
	private void registerFeature(
			final String entityId,
			final Feature feature,
			final Optional<String> serviceDefinitionOptional) {
		final String serviceDefinition = serviceDefinitionOptional.isPresent()
				? serviceDefinitionOptional.get()
				: getDefaultServiceDefinition(feature);
		final String serviceUri = String.format(
				Constants.SERVICE_URI_TEMPLATE,
				entityId,
				feature.getId());
		final Map<String, String> metadata = getMetadata(entityId);

		try {
			serviceRegistryClient.registerService(serviceDefinition, serviceUri, metadata);
		} catch (final Exception ex) {
			logger.error("Service registration for feature failed: " + ex);
		}
	}

	// -------------------------------------------------------------------------------------------------
	private Optional<JsonObject> getServiceDefinitions(final Thing thing) {
		if (thing.getAttributes().isPresent()) {
			final Attributes attributes = thing.getAttributes().get();
			final Optional<JsonValue> serviceDefinitions =
					attributes.getValue(Constants.SERVICE_DEFINITIONS);
			if (serviceDefinitions.isPresent() && serviceDefinitions.get().isObject()) {
				return Optional.of(serviceDefinitions.get().asObject());
			}
		}
		return Optional.empty();
	}

	// -------------------------------------------------------------------------------------------------
	private Optional<String> getServiceDefinition(final Optional<JsonObject> serviceDefinitions,
			String featureId) throws DittoModelException {
		if (serviceDefinitions.isPresent()) {
			final Optional<JsonValue> serviceDefinitionOptional =
					serviceDefinitions.get().getValue(featureId);
			return getServiceDefinition(serviceDefinitionOptional);
		}
		return Optional.empty();
	}

	// -------------------------------------------------------------------------------------------------
	private Optional<String> getServiceDefinition(
			final Optional<JsonValue> serviceDefinitionOptional) throws DittoModelException {
		if (serviceDefinitionOptional.isPresent()) {
			final JsonValue serviceDefinition = serviceDefinitionOptional.get();
			if (serviceDefinition.isString()) {
				final String serviceDefinitionString = serviceDefinition.asString();
				if (cnVerifier.isValid(serviceDefinitionString)) {
					return Optional.of(serviceDefinition.asString());
				}
			}
			throw new DittoModelException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE);
		}
		return Optional.empty();
	}

	// -------------------------------------------------------------------------------------------------
	private static String getDefaultServiceDefinition(final Feature feature) {
		return feature.getId(); // TODO: Figure out what to use here.
	}

	// -------------------------------------------------------------------------------------------------
	private Map<String, String> getMetadata(final String entityId) {
		return Map.of(Constants.ENTITY_ID, entityId);
	}

}

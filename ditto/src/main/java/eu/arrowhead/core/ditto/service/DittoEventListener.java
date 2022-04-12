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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.ditto.json.JsonKey;
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
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.ditto.Constants;
import eu.arrowhead.core.ditto.DittoModelException;
import eu.arrowhead.core.ditto.ThingEvent;
import eu.arrowhead.core.ditto.ThingEventType;

@Service
public class DittoEventListener implements ApplicationListener<ThingEvent> {

	//=================================================================================================
	// members

	private static final String SERVICE_URI_TEMPLATE =
			CommonConstants.DITTO_URI + Constants.ACCESS_THING + "/%s/features/%s";

	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE =
			"Service definition has invalid format. Service definition only contains maximum 63 character of letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";

	private static final String SERVICE_DEFINITIONS_WRONG_FORMAT_ERROR_MESSAGE = "Invalid serviceDefinitions attribute";

	private final Logger logger = LogManager.getLogger(DittoEventListener.class);

	@Autowired
	private ServiceRegistryClient serviceRegistryClient;

	@Autowired
	private CommonNamePartVerifier cnVerifier;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onApplicationEvent(final ThingEvent event) {
		Assert.notNull(event, "Thing change event is null");

		final ThingEventType type = event.getType();
		final Thing thing = event.getThing();

		try {
			switch (type) {
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
					throw new RuntimeException("Unhandled ThingEvent");
			}
		} catch (DittoModelException ex) {
			logger.error(ex);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void registerServices(final Thing thing) throws DittoModelException {

		if (thing.getEntityId().isEmpty()) {
			logger.error("No EntityId present in Thing");
			return;
		}

		final String thingId = thing.getEntityId().get().toString();
		final Optional<Features> features = thing.getFeatures();

		logger.debug("Registering services for thing '" + thingId + "'");

		if (features.isPresent()) {
			final Map<String, String> serviceDefinitions = getServiceDefinitions(thing);
			for (final Feature feature : features.get()) {
				final String featureId = feature.getId();
				final String serviceDefinition =
						serviceDefinitions.containsKey(featureId)
								? serviceDefinitions.get(featureId)
								: getDefaultServiceDefinition(thingId, featureId);
				this.registerFeature(thingId, feature, serviceDefinition);
			}
		}
	}

	// -------------------------------------------------------------------------------------------------
	private void unregisterServices(Thing thing) throws DittoModelException {
		if (thing.getEntityId().isEmpty()) {
			logger.error("No EntityId present in Thing");
			return;
		}

		final String thingId = thing.getEntityId().get().toString();
		final Optional<Features> features = thing.getFeatures();

		logger.debug("Unregistering services for thing '" + thingId + "'");

		if (features.isPresent()) {
			final Map<String, String> serviceDefinitions = getServiceDefinitions(thing);
			for (final Feature feature : features.get()) {
				unregisterFeature(thingId, feature.getId(), serviceDefinitions);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void registerFeature(final String thingId, final Feature feature, final String serviceDefinition) {
		final String serviceUri =
				String.format(SERVICE_URI_TEMPLATE, thingId, feature.getId());
		final Map<String, String> metadata = getMetadata(thingId);

		try {
			serviceRegistryClient.registerService(serviceDefinition, serviceUri, metadata);
		} catch (final Exception ex) {
			logger.error("Service registration for feature failed: " + ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void unregisterFeature(final String thingId, final String featureId, final Map<String, String> serviceDefinitions) {
		try {
			final String serviceUri = String.format(SERVICE_URI_TEMPLATE, thingId, featureId);

			final String serviceDefinition =
					serviceDefinitions.containsKey(featureId)
							? serviceDefinitions.get(featureId)
							: getDefaultServiceDefinition(thingId, featureId);

			serviceRegistryClient.unregisterService(serviceDefinition, serviceUri);
		} catch (final Exception ex) {
			logger.error("Service registration for feature failed: " + ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Map<String, String> getServiceDefinitions(final Thing thing) throws DittoModelException {
		final Map<String, String> result = new HashMap<>();
		if (thing.getAttributes().isPresent()) {
			final Attributes attributes = thing.getAttributes().get();
			final Optional<JsonValue> serviceDefinitionsOptional =
					attributes.getValue(Constants.SERVICE_DEFINITIONS);
			if (serviceDefinitionsOptional.isPresent()) {
				if (!serviceDefinitionsOptional.get().isObject()) {
					throw new DittoModelException(SERVICE_DEFINITIONS_WRONG_FORMAT_ERROR_MESSAGE);
				}
				JsonObject serviceDefinitions = serviceDefinitionsOptional.get().asObject();
				for (final JsonKey key : serviceDefinitions.getKeys()) {
					final JsonValue value = serviceDefinitions.getValue(key).get();
					final String serviceDefinition = validServiceDefinition(value);
					result.put(key.toString(), serviceDefinition);
				}
			}
		}
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private String validServiceDefinition(final JsonValue serviceDefinitionJson)
			throws DittoModelException {
		if (serviceDefinitionJson.isString()) {
			final String serviceDefinition = serviceDefinitionJson.asString();
			if (cnVerifier.isValid(serviceDefinition)) {
				return serviceDefinition;
			}
		}
		throw new DittoModelException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE);
	}

	//-------------------------------------------------------------------------------------------------
	private static String getDefaultServiceDefinition(final String entityId, final String featureId) {
		return entityId + "-" + featureId;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<String, String> getMetadata(final String thingId) {
		return Map.of(Constants.THING_ID, thingId);
	}

}

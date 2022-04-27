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

package eu.arrowhead.core.ditto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpStatus;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.ditto.service.DittoHttpClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(Constants.THING_MGMT_URI)
public class ThingManagementController {

	//=================================================================================================
	// members

	private static final String GET_THING_HTTP_200_MESSAGE = "Ditto Thing by requested id returned";
	private static final String GET_THINGS_HTTP_200_MESSAGE = "Ditto Things returned";
	private static final String PUT_THING_HTTP_201_MESSAGE = "Ditto Thing registered";
	private static final String PUT_THING_HTTP_204_MESSAGE = "Ditto Thing updated";
	private static final String DELETE_THING_HTTP_200_MESSAGE = "Ditto Thing removed";

	@Value(Constants.$SUBSCRIBE_TO_DITTO_EVENTS)
	private boolean subscribeToDittoEvents;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private DittoHttpClient dittoHttpClient;

	private final Logger logger = LogManager.getLogger(ThingManagementController.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns a list of all registered Ditto Things", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_THINGS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping
	public ResponseEntity<String> getThings() {
		return dittoHttpClient.getThings();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the specified Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_THING_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = "/{thingId}")
	public ResponseEntity<String> getThing(@PathVariable("thingId") String thingId) {
		 return dittoHttpClient.getThing(thingId);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates or updates the given Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_THING_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = PUT_THING_HTTP_204_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = "/{thingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ResponseEntity<String> putThing(@PathVariable("thingId") String thingId, @RequestBody final String thingRequest) {

		Thing thing = ThingsModelFactory.newThingBuilder(thingRequest)
			.setId(ThingId.of(thingId))
			.setPolicyId(PolicyId.of(Constants.DITTO_POLICY_ID))
			.build();

		final ResponseEntity<String> putResponse = dittoHttpClient.putThing(thingId, thing.toJsonString());
		final int statusCode = putResponse.getStatusCode().value();
		final boolean wasCreated = statusCode == HttpStatus.SC_CREATED;
		final boolean wasUpdated = statusCode == HttpStatus.SC_NO_CONTENT;

		if (!wasCreated && !wasUpdated) {
			return putResponse; // Error response
		}

		if (!subscribeToDittoEvents) {
			final ThingEventType eventType = wasCreated ? ThingEventType.CREATED : ThingEventType.UPDATED;
			ThingEvent event = new ThingEvent(this, thing, eventType);
			eventPublisher.publishEvent(event);
		}

		return putResponse; // Success response
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Deletes the specified Ditto Thing", response = String.class,
			tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_THING_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = "/{thingId}")
	@ResponseBody
	public ResponseEntity<Void> deleteThing(@PathVariable("thingId") String thingId) {

		final ResponseEntity<String> getThingResponse = dittoHttpClient.getThing(thingId);
		if (getThingResponse.getStatusCode() != org.springframework.http.HttpStatus.OK) {
			throw new DataNotFoundException("Thing with ID " + thingId + " does not exist.");
		}

		final ResponseEntity<Void> deletionResponse = dittoHttpClient.deleteThing(thingId);
		if (deletionResponse.getStatusCode() != org.springframework.http.HttpStatus.NO_CONTENT) {
			return deletionResponse; // Error response
		}

		if (!subscribeToDittoEvents) {
			final String thingJson = getThingResponse.getBody();
			if (thingJson == null) {
				final String errorMessage = "No thing data received from Ditto";
				logger.error(errorMessage);
				throw new ArrowheadException(errorMessage);
			}
			final Thing thing = ThingsModelFactory.newThingBuilder(thingJson).build();
			final ThingEvent event = new ThingEvent(this, thing, ThingEventType.DELETED);
			eventPublisher.publishEvent(event);
		}

		return deletionResponse; // Success response
	}

}

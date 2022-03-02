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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.core.ditto.service.DittoHttpClient;
import eu.arrowhead.core.ditto.service.ThingEvent;
import eu.arrowhead.core.ditto.service.ThingEventType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(CommonConstants.DITTO_URI)
public class DittoManagementController {

	//=================================================================================================
	// members

	private static final String GET_THING_HTTP_200_MESSAGE = "Ditto Thing returned";
	private static final String GET_THINGS_HTTP_200_MESSAGE = "Ditto Things returned";
	private static final String PUT_THING_HTTP_201_MESSAGE = "Ditto Thing registered/updated";
	private static final String DELETE_THING_HTTP_200_MESSAGE = "Ditto Thing removed";

	@Value(Constants.$SUBSCRIBE_TO_DITTO_EVENTS)
	private boolean subscribeToDittoEvents;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private DittoHttpClient dittoHttpClient;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns a list of all registered Ditto Things", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_THINGS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = "/things")
	public String getThings() {
		return dittoHttpClient.getThings().getBody();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the specified Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_THING_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = "/things/{thingId}")
	public String getThing(@PathVariable("thingId") String thingId) {
		return dittoHttpClient.getThing(thingId).getBody();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates or updates the given Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_THING_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PutMapping(path = "/things/{thingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String putThing(@PathVariable("thingId") String thingId, @RequestBody final String thingRequest) { // TODO: Change response type

		Thing thing = ThingsModelFactory.newThingBuilder(thingRequest)
			.setId(ThingId.of(thingId))
			.setPolicyId(PolicyId.of(Constants.DITTO_POLICY_ID))
			.build();

		final ResponseEntity<String> response = dittoHttpClient.putThing(thingId, thing.toJsonString());

		if (!subscribeToDittoEvents) {
			final boolean wasCreated = response.getStatusCode().value() == HttpStatus.SC_CREATED;
			final ThingEventType eventType = wasCreated ? ThingEventType.CREATED : ThingEventType.UPDATED;
			ThingEvent event = new ThingEvent(this, thing, eventType);
			eventPublisher.publishEvent(event);
		}

		return response.getBody();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Deletes the specified Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_THING_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.OK)
	@DeleteMapping(path = "/things/{thingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void deleteThing(@PathVariable("thingId") String thingId) {

		final String thingJson = dittoHttpClient.getThing(thingId).getBody();
		dittoHttpClient.deleteThing(thingId);

		if (!subscribeToDittoEvents) {
			Thing thing = ThingsModelFactory.newThingBuilder(thingJson).build();
			ThingEvent event = new ThingEvent(this, thing, ThingEventType.DELETED);
			eventPublisher.publishEvent(event);
		}
	}

}

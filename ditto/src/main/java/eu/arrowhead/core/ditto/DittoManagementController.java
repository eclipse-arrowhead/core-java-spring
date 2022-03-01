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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import eu.arrowhead.common.dto.internal.ThingRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.ditto.service.DittoHttpClient;
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
	@ApiOperation(value = "Registers or updates the given Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_THING_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PutMapping(path = "/things/{thingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String registerThing(@PathVariable("thingId") String thingId, @RequestBody final ThingRequestDTO thing) { // TODO: Change response type
		final String givenPolicyId = thing.getPolicyId();
		if (givenPolicyId != null && !givenPolicyId.equals(Constants.DITTO_POLICY_ID)) {
			throw new ArrowheadException("Cannot set Policy ID when registering Things via this system");
		}
		thing.setPolicyId(Constants.DITTO_POLICY_ID);
		return dittoHttpClient.registerThing(thingId, thing).getBody();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Unregisters the specified Ditto Thing", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_THING_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.OK)
	@DeleteMapping(path = "/things/{thingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String unregisterThing(@PathVariable("thingId") String thingId) {
		return dittoHttpClient.unregisterThing(thingId).getBody();
	}

}
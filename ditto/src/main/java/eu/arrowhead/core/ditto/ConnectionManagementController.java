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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import eu.arrowhead.core.ditto.service.DittoDevopsHttpClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(Constants.CONNECTION_MGMT_URI)
public class ConnectionManagementController {

	//=================================================================================================
	// members

	private static final String GET_CONNECTION_HTTP_200_MESSAGE = "Ditto connection by requested id returned";
	private static final String GET_CONNECTIONS_HTTP_200_MESSAGE = "Ditto connections returned";
	private static final String PUT_CONNECTION_HTTP_201_MESSAGE = "Ditto connection created";
	private static final String PUT_CONNECTION_HTTP_204_MESSAGE = "Ditto connection updated";
	private static final String DELETE_CONNECTION_HTTP_200_MESSAGE = "Ditto connection removed";

	@Value(Constants.$SUBSCRIBE_TO_DITTO_EVENTS)
	private boolean subscribeToDittoEvents;

	@Autowired
	private DittoDevopsHttpClient dittoDevopsHttpClient;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns a list of all Ditto connections", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CONNECTIONS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping
	public ResponseEntity<String> getConnections() {
		return dittoDevopsHttpClient.getConnections();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the specified Ditto connections", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CONNECTION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = "/{connectionId}")
	public ResponseEntity<String> getConnection(@PathVariable("connectionId") String connectionId) {
		return dittoDevopsHttpClient.getConnection(connectionId);
	}

		//-------------------------------------------------------------------------------------------------
		@ApiOperation(value = "Creates or updates the given Ditto connection", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
		@ApiResponses(value = {
				@ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_CONNECTION_HTTP_201_MESSAGE),
				@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = PUT_CONNECTION_HTTP_204_MESSAGE),
				@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
				@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
		})
		@PutMapping(path = "/{connectionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		@ResponseBody public ResponseEntity<JsonNode> putConnection(@PathVariable("connectionId") final String connectionId, @RequestBody final JsonNode connectionRequest) {
			var response = dittoDevopsHttpClient.putConnection(connectionRequest);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			return response;
		}
}

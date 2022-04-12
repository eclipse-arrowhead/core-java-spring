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

import java.util.Map;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.InventoryIdDTO;
import eu.arrowhead.common.dto.internal.SystemDataDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(CommonConstants.DITTO_URI + CommonConstants.MONITOR_URI)
public class DittoMonitorController {

	//=================================================================================================
	// members

	private static final String PING_HTTP_200_MESSAGE = "The system is live";
	private static final String SYSTEM_DATA_HTTP_200_MESSAGE = "System data returned";
	private static final String INVENTORY_ID_HTTP_200_MESSAGE = "Inventory ID returned";

	public static final String SYSTEM_DESCRIPTION = "An Arrowhead core system enabling interactions with Eclipse Ditto.";

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String coreSystemName;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PING_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.PING_URI)
	public String monitorPing() {
		return "OK";
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return system data describing this core system", response = SystemDataDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_DATA_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.SYSTEM_DATA_URI)
	public SystemDataDTO getSystemData() {
		return generateSystemData();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an inventory ID for this core system", response = InventoryIdDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = INVENTORY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.INVENTORY_ID_URI)
	public InventoryIdDTO getInventoryId() {
		return new InventoryIdDTO(null);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SystemDataDTO generateSystemData() {
		final Map<String, String> systemData = Map.of(
			"systemName", coreSystemName.toLowerCase(),
			"description", SYSTEM_DESCRIPTION
		);
		return new SystemDataDTO(systemData);
	}

}

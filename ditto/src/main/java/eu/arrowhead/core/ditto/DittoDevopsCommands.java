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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;

@Service
public class DittoDevopsCommands {

	//=================================================================================================
	// members
	@Autowired
	private ObjectMapper objectMapper;

	public static final String DITTO_PASSWORD = "ditto_password";
	public static final String $DITTO_PASSWORD = "${" + DITTO_PASSWORD + "}";

	public static final String SUBSCRIBE_TO_DITTO_EVENTS = "subscribe_to_ditto_events";
	public static final String $SUBSCRIBE_TO_DITTO_EVENTS = "${" + SUBSCRIBE_TO_DITTO_EVENTS + "}";

	public static final String THING_MGMT_URI = CommonConstants.DITTO_URI + CoreCommonConstants.MGMT_URI + "/things";
	public static final String CONNECTION_MGMT_URI = CommonConstants.DITTO_URI + CoreCommonConstants.MGMT_URI + "/connectivity";
	public static final String ACCESS_THING = "/access/things";

	public static final String SERVICE_DEFINITIONS = "serviceDefinitions";
	public static final String THING_ID = "thingId";
	public static final String DITTO_POLICY_ID = "eu.arrowhead:ah-ditto";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String create(final JsonNode connection, final String method) {
		// create a JSON object
		final ObjectNode command = objectMapper.createObjectNode();

		command.put("targetActorSelection", "/system/sharding/connection");
		command.set("headers", buildHeaders());
		if ("create".equals(method)){
			command.set("piggybackCommand",
				buildPiggybackCommand("connectivity.commands:createConnection", connection));
		} else if ("modify".equals(method)){
			command.set("piggybackCommand",
				buildPiggybackCommand("connectivity.commands:modifyConnection", connection));
		}
		return command.toString();
	}

	// create function overload
	public String create(final String connectionId, final String method) {
		// create a JSON object
		final ObjectNode command = objectMapper.createObjectNode();

		command.put("targetActorSelection", "/system/sharding/connection");
		command.set("headers", buildHeaders());
		if ("retrieve".equals(method)){
			command.set("piggybackCommand",
				buildPiggybackCommand("connectivity.commands:retrieveConnection", connectionId));
		} else if ("open".equals(method)){
			command.set("piggybackCommand",
				buildPiggybackCommand("connectivity.commands:openConnection", connectionId));
		} else if ("close".equals(method)){
			command.set("piggybackCommand",
				buildPiggybackCommand("connectivity.commands:closeConnection", connectionId));
		} else if ("delete".equals(method)){
			command.set("piggybackCommand",
				buildPiggybackCommand("connectivity.commands:deleteConnection", connectionId));
		}

		return command.toString();
	}

	//-------------------------------------------------------------------------------------------------
	private ObjectNode buildHeaders() {
		final ObjectNode headers = objectMapper.createObjectNode();
		headers.put("aggregate", false);
		headers.put("is-group-topic", true);
		return headers;
	}

	//-------------------------------------------------------------------------------------------------
	private JsonNode buildPiggybackCommand(final String type, final JsonNode connection) {
		final ObjectNode piggybackCommand = objectMapper.createObjectNode();
		piggybackCommand.put("type", type);
		piggybackCommand.set("connection", connection);
		return piggybackCommand;
	}

	// overloaded function
	private JsonNode buildPiggybackCommand(final String type, final String connectionID) {
		final ObjectNode piggybackCommand = objectMapper.createObjectNode();
		piggybackCommand.put("type", type);
		piggybackCommand.put("connectionId", connectionID);
		return piggybackCommand;
	}
}

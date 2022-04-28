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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;

public class Constants {

	//=================================================================================================
	// members

	public static final String DITTO_HTTP_ADDRESS = "ditto_http_address";
	public static final String $DITTO_HTTP_ADDRESS_WD = "${" + DITTO_HTTP_ADDRESS + "}";

	public static final String DITTO_WS_ADDRESS = "ditto_ws_address";
	public static final String $DITTO_WS_ADDRESS_WD = "${" + DITTO_WS_ADDRESS + "}";

	public static final String DITTO_USERNAME = "ditto_username";
	public static final String $DITTO_USERNAME = "${" + DITTO_USERNAME + "}";

	public static final String DITTO_PASSWORD = "ditto_password";
	public static final String $DITTO_PASSWORD = "${" + DITTO_PASSWORD + "}";

	public static final String GLOBAL_DITTO_POLICY = "global_policy_id";
	public static final String $GLOBAL_DITTO_POLICY = "${" + GLOBAL_DITTO_POLICY + "}";

	public static final String DITTO_DEVOPS_USERNAME = "ditto_devops_username";
	public static final String $DITTO_DEVOPS_USERNAME = "${" + DITTO_DEVOPS_USERNAME + "}";

	public static final String DITTO_DEVOPS_PASSWORD = "ditto_devops_password";
	public static final String $DITTO_DEVOPS_PASSWORD = "${" + DITTO_DEVOPS_PASSWORD + "}";

	public static final String SUBSCRIBE_TO_DITTO_EVENTS = "subscribe_to_ditto_events";
	public static final String $SUBSCRIBE_TO_DITTO_EVENTS = "${" + SUBSCRIBE_TO_DITTO_EVENTS + "}";

	public static final String THING_MGMT_URI = CommonConstants.DITTO_URI + CoreCommonConstants.MGMT_URI + "/things";
	public static final String CONNECTION_MGMT_URI = CommonConstants.DITTO_URI + CoreCommonConstants.MGMT_URI + "/connectivity";
	public static final String ACCESS_THING = "/access/things";

	public static final String SERVICE_DEFINITIONS = "serviceDefinitions";
	public static final String THING_ID = "thingId";

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Constants() {
		throw new UnsupportedOperationException();
	}
}

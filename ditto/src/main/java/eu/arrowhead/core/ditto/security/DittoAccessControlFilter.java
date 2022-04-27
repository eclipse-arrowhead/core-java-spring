/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 ********************************************************************************/
package eu.arrowhead.core.ditto.security;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;
import eu.arrowhead.core.ditto.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class DittoAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);

		final String cloudCN = getServerCloudCN();

		if (requestTarget.contains(Constants.THING_MGMT_URI)) {
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		}

	}

}

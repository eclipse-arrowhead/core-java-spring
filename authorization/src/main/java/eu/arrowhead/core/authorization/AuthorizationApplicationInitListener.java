/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.authorization;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;

@Component
public class AuthorizationApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$AUTHORIZATION_IS_EVENTHANDLER_PRESENT_WD)
	private boolean eventhandlerIsPresent;
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		if (eventhandlerIsPresent) {
			return List.of(CoreSystemService.EVENT_PUBLISH_AUTH_UPDATE_SERVICE);
		} else {
			return List.of();
		}
	}
}
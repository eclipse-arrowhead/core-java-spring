/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;

@Component
public class QoSMonitorApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$QOS_IS_GATEKEEPER_PRESENT_WD)
	private boolean gatekeeperIsPresent;

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		final List<CoreSystemService> result = new ArrayList<>(5);
		
		if (gatekeeperIsPresent) {
			result.add(CoreSystemService.GATEKEEPER_PULL_CLOUDS); 
			result.add(CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES);
			result.add(CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES);
			result.add(CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE);
			result.add(CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE);
		}

		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		final ApplicationContext appContext = event.getApplicationContext();
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);

		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;

		final UriComponents queryAll = createQueryAllUri(scheme);
		context.put(CoreCommonConstants.SR_QUERY_ALL, queryAll);
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryAllUri(final String scheme) {
		logger.debug("createQueryAllUri started...");

		final String registyUriStr = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_ALL_SERVICE_URI;

		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registyUriStr);
	}
}
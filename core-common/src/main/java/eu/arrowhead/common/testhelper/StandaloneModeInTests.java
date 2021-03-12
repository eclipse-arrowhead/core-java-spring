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

package eu.arrowhead.common.testhelper;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;

public abstract class StandaloneModeInTests {
	
	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(StandaloneModeInTests.class);
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	@Order(5)
	public void onApplicationEvent(final ContextRefreshedEvent event)  {
		logger.info("STANDALONE mode is set...");
		arrowheadContext.put(CoreCommonConstants.SERVER_STANDALONE_MODE, true);
	}
}
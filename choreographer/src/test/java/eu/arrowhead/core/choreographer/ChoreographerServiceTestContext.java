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

package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerExecutorService;
import eu.arrowhead.core.choreographer.service.ChoreographerService;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanExecutionChecker;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanValidator;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class ChoreographerServiceTestContext {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public ChoreographerPlanDBService mockChoreographerPlanDBService() {
        return Mockito.mock(ChoreographerPlanDBService.class);
    }
    
	//-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public ChoreographerSessionDBService mockChoreographerSessionDBService() {
        return Mockito.mock(ChoreographerSessionDBService.class);
    }
    
	//-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public ChoreographerExecutorService mockChoreographerExecutorService() {
        return Mockito.mock(ChoreographerExecutorService.class);
    }
    
    //-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public JmsTemplate mockJmsService() {
		return Mockito.mock(JmsTemplate.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public ChoreographerPlanValidator mockPlanValidator() {
		return Mockito.mock(ChoreographerPlanValidator.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public ChoreographerPlanExecutionChecker mockPlanChecker() {
		return Mockito.mock(ChoreographerPlanExecutionChecker.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public ChoreographerService mockChoreographerService() {
		return Mockito.mock(ChoreographerService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public CommonDBService mockCommonDBService() {
		return Mockito.mock(CommonDBService.class);
	}
}
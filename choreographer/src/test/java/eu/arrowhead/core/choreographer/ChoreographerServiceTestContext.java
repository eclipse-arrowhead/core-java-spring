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

import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerExecutorService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
}
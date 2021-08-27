/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.choreographer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionException;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;

@RunWith(SpringRunner.class)
public class ChoreographerServiceTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerService testObject;
	
	@Mock
    private ChoreographerPlanDBService planDBService;
    
	@Mock
    private ChoreographerSessionDBService sessionDBService;

	@Mock
    private ChoreographerDriver driver;
    
	@Mock
    private SessionDataStorage sessionDataStorage;
    
	@Mock
    private ExecutorSelector executorSelector;

	@Mock
    protected CoreSystemRegistrationProperties registrationProperties;
    
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		when(registrationProperties.getCoreSystemName()).thenReturn(CoreSystem.CHOREOGRAPHER.name());
		
		testObject.init();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInit() {
		final SystemRequestDTO requesterSystem = (SystemRequestDTO) ReflectionTestUtils.getField(testObject, "requesterSystem");
		Assert.assertNotNull(requesterSystem);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveStartSessionMessageInputNull() {
		try {
			testObject.receiveStartSessionMessage(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageExceptionWrapping() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);

		when(planDBService.getPlanById(2)).thenThrow(new ArrowheadException("test"));

		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			
			Assert.assertEquals(ArrowheadException.class, ex.getCause().getClass());
			Assert.assertEquals("test", ex.getCause().getMessage());
			
			throw ex;
		}

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageExecutorNotFound() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMinVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//TODO: continue (we two similar steps we can test manual session step registering)
}
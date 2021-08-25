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

package eu.arrowhead.core.choreographer.validation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ChoreographerPlanExecutionCheckerTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerPlanExecutionChecker testObject;
	
	@Mock
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Mock
	private ChoreographerPlanDBService planDBService;
	
	@Mock
	private ChoreographerExecutorDBService executorDBService;
	
	@Mock
	private ChoreographerDriver driver;
	
	@Mock
	private ExecutorSelector executorSelector;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionRequestNull() {
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(null);
		
		Assert.assertNull(response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("Request is null.", response.getErrorMessages().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionBasicChecks1() {
		doThrow(InvalidParameterException.class).when(networkAddressVerifier).verify(anyString());
		
		final ChoreographerRunPlanRequestDTO request = new ChoreographerRunPlanRequestDTO();
		request.setNotifyAddress("1.2.3.4.5");
		request.setNotifyProtocol("FTP");
		request.setNotifyPort(-2);
		request.setNotifyPath(null);
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(request);
		
		Assert.assertNull(response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(4, response.getErrorMessages().size());
		Assert.assertEquals("Plan id is not valid.", response.getErrorMessages().get(0));
		Assert.assertEquals("Invalid notify protocol.", response.getErrorMessages().get(1));
		Assert.assertTrue(response.getErrorMessages().get(2).startsWith("Invalid notify address. "));
		Assert.assertTrue(response.getErrorMessages().get(3).startsWith("Notify port must be between "));
		Assert.assertEquals("", request.getNotifyPath());
		
		verify(networkAddressVerifier, times(1)).verify("1.2.3.4.5");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionBasicChecks2() {
		doNothing().when(networkAddressVerifier).verify(anyString());
		
		final ChoreographerRunPlanRequestDTO request = new ChoreographerRunPlanRequestDTO();
		request.setPlanId(0L);
		request.setNotifyAddress("localhost");
		request.setNotifyPort(100000);
		request.setNotifyPath("/abc");
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(request);
		
		Assert.assertEquals((Long) 0L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(2, response.getErrorMessages().size());
		Assert.assertEquals("Plan id is not valid.", response.getErrorMessages().get(0));
		Assert.assertTrue(response.getErrorMessages().get(1).startsWith("Notify port must be between "));
		Assert.assertEquals("/abc", request.getNotifyPath());
		
		verify(networkAddressVerifier, times(1)).verify("localhost");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionPlanNotFound() {
		when(planDBService.getPlanById(anyLong())).thenThrow(new InvalidParameterException("test"));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("test", response.getErrorMessages().get(0));
		
		verify(planDBService, times(1)).getPlanById(1);
	}
	
	//TODO: continue 
}
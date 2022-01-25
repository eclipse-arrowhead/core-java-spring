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

package eu.arrowhead.core.choreographer.exception;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.jms.JMSException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.core.choreographer.service.ChoreographerService;

@RunWith(SpringRunner.class)
public class ChoreographerSessionErrorHandlerTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerSessionErrorHandler testObject;
	
	@Mock
	private ChoreographerService service;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHandleErrorChoreographerSessionException() {
		doNothing().when(service).abortSession(1L, 1L, "message");
		final JMSException jmsException = new JMSException("jms");
		jmsException.initCause(new ChoreographerSessionException(1L, 1L, "message"));
		
		testObject.handleError(jmsException);
		
		verify(service, times(1)).abortSession(1L, 1L, "message");
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHandleErrorGeneralException() {
		final JMSException jmsException = new JMSException("jms");
		jmsException.initCause(new Exception("abc"));
		
		testObject.handleError(jmsException);
		
		verify(service, never()).abortSession(anyLong(), anyLong(), anyString());
	}
}
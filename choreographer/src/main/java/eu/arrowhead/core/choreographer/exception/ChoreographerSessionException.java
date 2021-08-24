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

@SuppressWarnings("serial")
public class ChoreographerSessionException extends RuntimeException {
	
	//=================================================================================================
	// members
	
	private final long sessionId;
	private final Long sessionStepId;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionException(final long sessionId, final String message) {
		this(sessionId, null, message, null);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionException(final long sessionId, final Throwable cause) {
		this(sessionId, null, null, cause);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionException(final long sessionId, final String message, final Throwable cause) {
		this(sessionId, null, message, cause);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionException(final long sessionId, final Long sessionStepId, final String message) {
		this(sessionId, sessionStepId, message, null);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionException(final long sessionId, final Long sessionStepId, final Throwable cause) {
		this(sessionId, sessionStepId, null, cause);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerSessionException(final long sessionId, final Long sessionStepId, final String message, final Throwable cause) {
		super(message, cause);
		
		this.sessionId = sessionId;
		this.sessionStepId = sessionStepId;
	}

	//-------------------------------------------------------------------------------------------------
	public long getSessionId() { return sessionId; }
	public Long getSessionStepId() { return sessionStepId; }
	
	//-------------------------------------------------------------------------------------------------
	public String getDetailedMessage() {
		String result = this.getCause() != null ? this.getCause().getMessage() : "";
		result += " " + this.getMessage();
		
		return result.trim();
 	}
}
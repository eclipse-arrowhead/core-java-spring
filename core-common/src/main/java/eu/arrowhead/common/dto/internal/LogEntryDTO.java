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

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogEntryDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -8522921151534783561L;
	
	private String logId;
	private String entryDate;
	private String logger;
	private String logLevel;
	private String systemName;
	private String message;
	private String exception;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public LogEntryDTO() {}

	//-------------------------------------------------------------------------------------------------
	public LogEntryDTO(final String logId, final String entryDate, final String logger, final String logLevel, final String systemName, final String message, final String exception) {
		this.logId = logId;
		this.entryDate = entryDate;
		this.logger = logger;
		this.logLevel = logLevel;
		this.systemName = systemName;
		this.message = message;
		this.exception = exception;
	}

	//-------------------------------------------------------------------------------------------------
	public String getLogId() { return logId; }
	public String getEntryDate() { return entryDate; }
	public String getLogger() { return logger; }
	public String getLogLevel() { return logLevel; }
	public String getSystemName() { return systemName; }
	public String getMessage() { return message; }
	public String getException() { return exception; }

	//-------------------------------------------------------------------------------------------------
	public void setLogId(final String logId) { this.logId = logId; }
	public void setEntryDate(final String entryDate) { this.entryDate = entryDate; }
	public void setLogger(final String logger) { this.logger = logger; }
	public void setLogLevel(final String logLevel) { this.logLevel = logLevel; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setMessage(final String message) { this.message = message; }
	public void setException(final String exception) { this.exception = exception; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}
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

package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import eu.arrowhead.common.CoreDefaults;

@Entity
public class Logs {
	
	//=================================================================================================
	// members
	
	@Id
	@Column(length = CoreDefaults.VARCHAR_LOG)
	private String logId;
	
	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime entryDate;
	
	@Column(nullable = true, length = CoreDefaults.VARCHAR_LOG)
	private String logger;
	
	@Column(nullable = true, length = CoreDefaults.VARCHAR_LOG)
	private String logLevel;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String message;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String exception;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Logs() {}

	//-------------------------------------------------------------------------------------------------
	public Logs(final String logId, final ZonedDateTime entryDate, final String logger, final String logLevel, final String message, final String exception) {
		this.logId = logId;
		this.entryDate = entryDate;
		this.logger = logger;
		this.logLevel = logLevel;
		this.message = message;
		this.exception = exception;
	}

	//-------------------------------------------------------------------------------------------------
	public String getLogId() { return logId; }
	public ZonedDateTime getEntryDate() { return entryDate; }
	public String getLogger() { return logger; }
	public String getLogLevel() { return logLevel; }
	public String getMessage() { return message; }
	public String getException() { return exception; }

	//-------------------------------------------------------------------------------------------------
	public void setLogId(final String logId) { this.logId = logId; }
	public void setEntryDate(final ZonedDateTime entryDate) { this.entryDate = entryDate; }
	public void setLogger(final String logger) { this.logger = logger; }
	public void setLogLevel(final String logLevel) { this.logLevel = logLevel; }
	public void setMessage(final String message) { this.message = message; }
	public void setException(final String exception) { this.exception = exception; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "Logs [logId = " + logId + ", entryDate = " + entryDate + ", logger = " + logger + ", logLevel = " + logLevel + ", message = " + message + "]";
	}
}
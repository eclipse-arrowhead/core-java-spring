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

package eu.arrowhead.common.exception;

@SuppressWarnings("serial")
public class DataNotFoundException extends ArrowheadException {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public DataNotFoundException(final String msg, final int errorCode, final String origin, final Throwable cause) {
	    super(msg, errorCode, origin, cause);
	    this.setExceptionType(ExceptionType.DATA_NOT_FOUND);
	}
	
	//-------------------------------------------------------------------------------------------------
	public DataNotFoundException(final String msg, final int errorCode, final String origin) {
	    super(msg, errorCode, origin);
	    this.setExceptionType(ExceptionType.DATA_NOT_FOUND);
	}
	
	//-------------------------------------------------------------------------------------------------
	public DataNotFoundException(final String msg, final int errorCode, final Throwable cause) {
	    super(msg, errorCode, cause);
	    this.setExceptionType(ExceptionType.DATA_NOT_FOUND);
	}
	
	//-------------------------------------------------------------------------------------------------
	public DataNotFoundException(final String msg, final int errorCode) {
	    super(msg, errorCode);
	    this.setExceptionType(ExceptionType.DATA_NOT_FOUND);
	}
	
	//-------------------------------------------------------------------------------------------------
	public DataNotFoundException(final String msg, final Throwable cause) {
	    super(msg, cause);
	    this.setExceptionType(ExceptionType.DATA_NOT_FOUND);
	}
	
	//-------------------------------------------------------------------------------------------------
	public DataNotFoundException(final String msg) {
	    super(msg);
	    this.setExceptionType(ExceptionType.DATA_NOT_FOUND);
	}
}
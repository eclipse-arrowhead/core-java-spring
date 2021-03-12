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
public class ArrowheadException extends RuntimeException {
	
	//=================================================================================================
	// members

	private ExceptionType exceptionType = ExceptionType.ARROWHEAD; //NOSONAR can't be final because derived class want to use the setter
	private final int errorCode;
	private final String origin;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ArrowheadException(final String msg, final int errorCode, final String origin, final Throwable cause) {
	    super(msg, cause);
	    this.errorCode = errorCode;
	    this.origin = origin;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ArrowheadException(final String msg, final int errorCode, final String origin) {
	    super(msg);
	    this.errorCode = errorCode;
	    this.origin = origin;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ArrowheadException(final String msg, final int errorCode, final Throwable cause) {
	    super(msg, cause);
	    this.errorCode = errorCode;
	    this.origin = null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ArrowheadException(final String msg, final int errorCode) {
	    super(msg);
	    this.errorCode = errorCode;
	    this.origin = null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ArrowheadException(final String msg, final Throwable cause) {
	    super(msg, cause);
	    this.errorCode = 0;
	    this.origin = null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ArrowheadException(final String msg) {
	    super(msg);
	    this.errorCode = 0;
	    this.origin = null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ExceptionType getExceptionType() { return exceptionType; }
	public int getErrorCode() { return errorCode; }
	public String getOrigin() { return origin; }
	
	//-------------------------------------------------------------------------------------------------
	protected void setExceptionType(final ExceptionType exceptionType) { this.exceptionType = exceptionType; }
}
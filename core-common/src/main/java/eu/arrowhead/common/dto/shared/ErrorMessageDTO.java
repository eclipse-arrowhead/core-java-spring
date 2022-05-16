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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.ExceptionType;

@JsonInclude(Include.NON_NULL)
public class ErrorMessageDTO implements Serializable, ErrorWrapperDTO {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -3104121879721369522L;
	
	private String errorMessage;
	private int errorCode;
	private ExceptionType exceptionType;
	private String origin;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ErrorMessageDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ErrorMessageDTO(final ArrowheadException ex) {
		setError(ex);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ErrorMessageDTO(final String errorMessage, final int errorCode, final ExceptionType exceptionType, final String origin) {
		setError(errorMessage, errorCode, exceptionType, origin);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setError(final String errorMessage, final int errorCode, final ExceptionType exceptionType, final String origin) {
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
		this.exceptionType = exceptionType;
		this.origin = origin;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setError(final ArrowheadException ex) {
		if (ex != null) {
			setError(ex.getMessage(), ex.getErrorCode(), ex.getExceptionType(), ex.getOrigin());
		}
	}
	  
	//-------------------------------------------------------------------------------------------------
	public String getErrorMessage() { return errorMessage; }
	public int getErrorCode() { return errorCode; }
	public ExceptionType getExceptionType() { return exceptionType; }
	public String getOrigin() { return origin; }
	
	//-------------------------------------------------------------------------------------------------
	public void setErrorMessage(final String errorMessage) { this.errorMessage = errorMessage; }
	public void setErrorCode(final int errorCode) { this.errorCode = errorCode; }
	public void setExceptionType(final ExceptionType exceptionType) { this.exceptionType = exceptionType; }
	public void setOrigin(final String origin) { this.origin = origin; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}

	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	@Override
	public boolean isError() {
		return true;
	}
}
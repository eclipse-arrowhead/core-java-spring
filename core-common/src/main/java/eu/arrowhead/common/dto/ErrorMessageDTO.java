package eu.arrowhead.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.ExceptionType;

@JsonInclude(Include.NON_NULL)
public class ErrorMessageDTO {

	private String errorMessage;
	private int errorCode;
	private ExceptionType exceptionType;
	private String origin;
	
	public ErrorMessageDTO() {}
	
	public ErrorMessageDTO(final ArrowheadException ex) {
		setError(ex);
	}
	
	public void setError(final String errorMessage, final int errorCode, final ExceptionType exceptionType, final String origin) {
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
		this.exceptionType = exceptionType;
		this.origin = origin;
	}
	
	public void setError(final ArrowheadException ex) {
		if (ex != null) {
			setError(ex.getMessage(), ex.getErrorCode(), ex.getExceptionType(), ex.getOrigin());
		}
	}
	  
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}
	
	public ExceptionType getExceptionType() {
		return exceptionType;
	}
	
	public void setExceptionType(final ExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public void setOrigin(final String origin) {
		this.origin = origin;
	}
	
	@Override
	public String toString() {
		return "ErrorMessageDTO [errorMessage=" + errorMessage + ", errorCode=" + errorCode + ", exceptionType=" + exceptionType + ", origin=" + origin + "]";
	}
}
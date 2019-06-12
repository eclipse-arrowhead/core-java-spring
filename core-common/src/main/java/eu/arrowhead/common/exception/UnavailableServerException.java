/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

/**
 * Thrown when a HTTP request times out because the endpoint is not available.
 */
@SuppressWarnings("serial")
public class UnavailableServerException extends ArrowheadException {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public UnavailableServerException(final String msg, final int errorCode, final String origin, final Throwable cause) {
	    super(msg, errorCode, origin, cause);
	    this.setExceptionType(ExceptionType.UNAVAILABLE);
	}

	//-------------------------------------------------------------------------------------------------
	public UnavailableServerException(final String msg, final int errorCode, final String origin) {
	    super(msg, errorCode, origin);
	    this.setExceptionType(ExceptionType.UNAVAILABLE);
	}
	
	//-------------------------------------------------------------------------------------------------
	public UnavailableServerException(final String msg, final int errorCode, final Throwable cause) {
	    super(msg, errorCode, cause);
	    this.setExceptionType(ExceptionType.UNAVAILABLE);
	}
	
	//-------------------------------------------------------------------------------------------------
	public UnavailableServerException(final String msg, final int errorCode) {
	    super(msg, errorCode);
	    this.setExceptionType(ExceptionType.UNAVAILABLE);
	}
	
	//-------------------------------------------------------------------------------------------------
	public UnavailableServerException(final String msg, final Throwable cause) {
	    super(msg, cause);
	    this.setExceptionType(ExceptionType.UNAVAILABLE);
	}
	
	//-------------------------------------------------------------------------------------------------
	public UnavailableServerException(final String msg) {
	    super(msg);
	    this.setExceptionType(ExceptionType.UNAVAILABLE);
	}
}
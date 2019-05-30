/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

@SuppressWarnings("serial")
public class ArrowheadException extends RuntimeException {

	private ExceptionType exceptionType = ExceptionType.ARROWHEAD;
	private final int errorCode;
	private final String origin;

	public ArrowheadException(final String msg, final int errorCode, final String origin, final Throwable cause) {
	    super(msg, cause);
	    this.errorCode = errorCode;
	    this.origin = origin;
	}
	
	public ArrowheadException(final String msg, final int errorCode, final String origin) {
	    super(msg);
	    this.errorCode = errorCode;
	    this.origin = origin;
	}
	
	public ArrowheadException(final String msg, final int errorCode, final Throwable cause) {
	    super(msg, cause);
	    this.errorCode = errorCode;
	    this.origin = null;
	}
	
	public ArrowheadException(final String msg, final int errorCode) {
	    super(msg);
	    this.errorCode = errorCode;
	    this.origin = null;
	}
	
	public ArrowheadException(final String msg, final Throwable cause) {
	    super(msg, cause);
	    this.errorCode = 0;
	    this.origin = null;
	}
	
	public ArrowheadException(final String msg) {
	    super(msg);
	    this.errorCode = 0;
	    this.origin = null;
	}
	
	public ExceptionType getExceptionType() {
	    return exceptionType;
	}
	
	void setExceptionType(final ExceptionType exceptionType) {
	    this.exceptionType = exceptionType;
	}
	
	public int getErrorCode() {
	    return errorCode;
	}
	
	public String getOrigin() {
	    return origin;
	}
}
package eu.arrowhead.common.exception;

@SuppressWarnings("serial")
public class TimeoutException extends ArrowheadException {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public TimeoutException(final String msg, final int errorCode, final String origin, final Throwable cause) {
		super(msg, errorCode, origin, cause);
		this.setExceptionType(ExceptionType.TIMEOUT);
	}

	//-------------------------------------------------------------------------------------------------
	public TimeoutException(final String msg, final int errorCode, final String origin) {
		super(msg, errorCode, origin);
		this.setExceptionType(ExceptionType.TIMEOUT);
	}

	//-------------------------------------------------------------------------------------------------
	public TimeoutException(final String msg, final int errorCode, final Throwable cause) {
		super(msg, errorCode, cause);
		this.setExceptionType(ExceptionType.TIMEOUT);
	}

	//-------------------------------------------------------------------------------------------------
	public TimeoutException(final String msg, final int errorCode) {
		super(msg, errorCode);
		this.setExceptionType(ExceptionType.TIMEOUT);
	}

	//-------------------------------------------------------------------------------------------------
	public TimeoutException(final String msg, final Throwable cause) {
		super(msg, cause);
		this.setExceptionType(ExceptionType.TIMEOUT);
	}

	//-------------------------------------------------------------------------------------------------
	public TimeoutException(final String msg) {
		super(msg);
		this.setExceptionType(ExceptionType.TIMEOUT);
	}
}
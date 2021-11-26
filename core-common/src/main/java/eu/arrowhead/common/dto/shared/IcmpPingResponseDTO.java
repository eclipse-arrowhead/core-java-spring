package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class IcmpPingResponseDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 7382047276356365361L;

	private boolean successFlag;
	private boolean timeoutFlag;
	private String errorMessage;
	private String throwable;
	private String host;
	private Integer size;
	private Integer rtt;
	private Integer ttl;
	private Long duration;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public boolean isSuccessFlag() { return successFlag; }
	public boolean isTimeoutFlag() { return timeoutFlag; }
	public String getErrorMessage() { return errorMessage; }
	public String getThrowable() { return throwable; }
	public String getHost() { return host; }
	public Integer getSize() { return size; }
	public Integer getRtt() { return rtt; }
	public Integer getTtl() { return ttl; }
	public Long getDuration() { return duration; }

	//-------------------------------------------------------------------------------------------------
	public void setSuccessFlag(final boolean successFlag) { this.successFlag = successFlag; }
	public void setTimeoutFlag(final boolean timeoutFlag) { this.timeoutFlag = timeoutFlag; }
	public void setErrorMessage(final String errorMessage) { this.errorMessage = errorMessage; }
	public void setThrowable(final String throwable) { this.throwable = throwable; }
	public void setHost(final String host) { this.host = host; }
	public void setSize(final Integer size) { this.size = size; }
	public void setRtt(final Integer rtt) { this.rtt = rtt; }
	public void setTtl(final Integer ttl) { this.ttl = ttl; }
	public void setDuration(final Long duration) { this.duration = duration; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return
		"[" +
		"hashCode: " + super.hashCode () + ", " +
		"successFlag: " + successFlag + ", " +
		"timeoutFlag: " + timeoutFlag + ", " +
		"errorMessage: " + errorMessage + ", " +
		"throwable: " + throwable + ", " +
		"host: " + host + ", " +
		"size: " + size + ", " +
		"rtt: " + rtt + ", " +
		"ttl: " + ttl + ", " +
		"duration: " + duration +
		"]";
	}

}

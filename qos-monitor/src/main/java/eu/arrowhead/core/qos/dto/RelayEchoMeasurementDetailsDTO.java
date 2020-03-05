package eu.arrowhead.core.qos.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class RelayEchoMeasurementDetailsDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6417775229433791206L;
	
	private int measurementSequenceNumber;
	private boolean timeoutFlag;
	private String errorMessage;
	private String throwable;
	private Integer size;
	private Integer duration;
	private ZonedDateTime measuredAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public RelayEchoMeasurementDetailsDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public RelayEchoMeasurementDetailsDTO(final int measurementSequenceNumber, final boolean timeoutFlag, final String errorMessage, final String throwable, final Integer size,
										  final Integer duration, final ZonedDateTime measuredAt) {
		this.timeoutFlag = timeoutFlag;
		this.errorMessage = errorMessage;
		this.throwable = throwable;
		this.size = size;
		this.duration = duration;
		this.measuredAt = measuredAt;
	}

	//-------------------------------------------------------------------------------------------------
	public int getMeasurementSequenceNumber() { return measurementSequenceNumber; }	
	public boolean getTimeoutFlag() { return timeoutFlag; }
	public String getErrorMessage() { return errorMessage; }
	public String getThrowable() { return throwable; }
	public Integer getSize() { return size; }
	public Integer getDuration() { return duration; } 
	public ZonedDateTime getMeasuredAt() { return measuredAt; }

	//-------------------------------------------------------------------------------------------------
	public void setMeasurementSequenceNumber(int measurementSequenceNumber) { this.measurementSequenceNumber = measurementSequenceNumber; }
	public void setTimeoutFlag(final boolean timeoutFlag) { this.timeoutFlag = timeoutFlag; }	
	public void setErrorMessage(final String errorMessage) { this.errorMessage = errorMessage; }
	public void setThrowable(final String throwable) { this.throwable = throwable; }
	public void setSize(final Integer size) { this.size = size; }
	public void setDuration(final Integer duration) { this.duration = duration; }
	public void setMeasuredAt(final ZonedDateTime measuredAt) { this.measuredAt = measuredAt; }
}

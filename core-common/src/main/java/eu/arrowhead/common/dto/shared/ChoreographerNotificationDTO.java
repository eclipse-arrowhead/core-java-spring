package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;

public class ChoreographerNotificationDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 222508477814610141L;
	
	private long sessionId;
	private long planId;
	private String planName;
	private String timestamp;
	private ChoreographerSessionStatus status;
	private String message;
	private String details;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ChoreographerNotificationDTO() {} 
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerNotificationDTO(final long sessionId, final long planId, final String planName, final String timestamp, final ChoreographerSessionStatus status, final String message, final String details) {
		super();
		this.sessionId = sessionId;
		this.planId = planId;
		this.planName = planName;
		this.timestamp = timestamp;
		this.status = status;
		this.message = message;
		this.details = details;
	}


	//-------------------------------------------------------------------------------------------------
	public long getSessionId() { return sessionId; }
	public long getPlanId() { return planId; }
	public String getPlanName() { return planName; }
	public String getTimestamp() { return timestamp; }
	public ChoreographerSessionStatus getStatus() { return status; }
	public String getMessage() { return message; }
	public String getDetails() { return details; }

	//-------------------------------------------------------------------------------------------------
	public void setSessionId(final long sessionId) { this.sessionId = sessionId; }
	public void setPlanId(final long planId) { this.planId = planId; }
	public void setPlanName(final String planName) { this.planName = planName; }
	public void setTimestamp(final String timestamp) { this.timestamp = timestamp; }
	public void setStatus(final ChoreographerSessionStatus status) { this.status = status; }
	public void setMessage(final String message) { this.message = message; }
	public void setDetails(final String details) { this.details = details; }
}
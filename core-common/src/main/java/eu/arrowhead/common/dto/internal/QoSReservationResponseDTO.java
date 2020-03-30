package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class QoSReservationResponseDTO implements Serializable{

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7502548027753697110L;
	
	private long id;
	private long reservedProviderId;
	private long reservedServiceId;	
	private String consumerSystemName;
	private String consumerAddress;
	private int consumerPort;	
	private ZonedDateTime reservedTo;	
	private boolean temporaryLock;
	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSReservationResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSReservationResponseDTO(final long id, final long reservedProviderId, final long reservedServiceId, final String consumerSystemName, final String consumerAddress,
									 final int consumerPort, final ZonedDateTime reservedTo, final boolean temporaryLock, final ZonedDateTime createdAt, final ZonedDateTime updatedAt) {
		this.id = id;
		this.reservedProviderId = reservedProviderId;
		this.reservedServiceId = reservedServiceId;
		this.consumerSystemName = consumerSystemName;
		this.consumerAddress = consumerAddress;
		this.consumerPort = consumerPort;
		this.reservedTo = reservedTo;
		this.temporaryLock = temporaryLock;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public long getReservedProviderId() { return reservedProviderId; }
	public long getReservedServiceId() { return reservedServiceId; }
	public String getConsumerSystemName() { return consumerSystemName; }
	public String getConsumerAddress() { return consumerAddress; }
	public int getConsumerPort() { return consumerPort; }
	public ZonedDateTime getReservedTo() { return reservedTo; }
	public boolean isTemporaryLock() { return temporaryLock; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setReservedProviderId(final long reservedProviderId) { this.reservedProviderId = reservedProviderId; }
	public void setReservedServiceId(final long reservedServiceId) { this.reservedServiceId = reservedServiceId; }
	public void setConsumerSystemName(final String consumerSystemName) { this.consumerSystemName = consumerSystemName; }
	public void setConsumerAddress(final String consumerAddress) { this.consumerAddress = consumerAddress; }
	public void setConsumerPort(final int consumerPort) { this.consumerPort = consumerPort; }
	public void setReservedTo(final ZonedDateTime reservedTo) { this.reservedTo = reservedTo; }
	public void setTemporaryLock(final boolean temporaryLock) { this.temporaryLock = temporaryLock; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }	
}

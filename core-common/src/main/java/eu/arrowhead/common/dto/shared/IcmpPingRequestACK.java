package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.UUID;

public class IcmpPingRequestACK implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -7812241728073959599L;

	private String ackOk;
	private UUID externalMeasurementUuid;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestACK() {}

	//-------------------------------------------------------------------------------------------------
	public String getAckOk() { return ackOk; }
	public UUID getExternalMeasurementUuid() { return externalMeasurementUuid; }

	//-------------------------------------------------------------------------------------------------
	public void setAckOk(final String ackOk) { this.ackOk = ackOk; }
	public void setExternalMeasurementUuid(final UUID externalMeasurementUuid) { this.externalMeasurementUuid = externalMeasurementUuid; }

}

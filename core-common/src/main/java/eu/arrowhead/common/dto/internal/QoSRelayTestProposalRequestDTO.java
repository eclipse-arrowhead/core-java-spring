package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class QoSRelayTestProposalRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -3624831623265548479L;
	
	private CloudRequestDTO requesterCloud;
	private RelayRequestDTO relay;
	private String senderQoSMonitorPublicKey;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalRequestDTO(final CloudRequestDTO requesterCloud, final RelayRequestDTO relay, final String senderQoSMonitorPublicKey) {
		Assert.notNull(requesterCloud, "Requester cloud is null.");
		Assert.notNull(relay, "relay is null.");
		Assert.isTrue(!Utilities.isEmpty(senderQoSMonitorPublicKey), "Sender QoS Monitor's public key is null or empty.");
		
		this.requesterCloud = requesterCloud;
		this.relay = relay;
		this.senderQoSMonitorPublicKey = senderQoSMonitorPublicKey;
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public RelayRequestDTO getRelay() { return relay; }
	public String getSenderQoSMonitorPublicKey() { return senderQoSMonitorPublicKey; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setSenderQoSMonitorPublicKey(final String senderQoSMonitorPublicKey) { this.senderQoSMonitorPublicKey = senderQoSMonitorPublicKey; }
}
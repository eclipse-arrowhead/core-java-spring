package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;

public class QoSRelayTestProposalResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 5632469446847929652L;
	
	private String queueId;
	private String peerName;
	private String receiverQoSMonitorPublicKey;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO(final String queueId, final String peerName, final String receiverQoSMonitorPublicKey) {
		Assert.isTrue(!Utilities.isEmpty(queueId), "Queue id is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(peerName), "Peer name is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(receiverQoSMonitorPublicKey), "Receiver QoS Monitor's public key is null or empty.");
	}

	//-------------------------------------------------------------------------------------------------
	public String getQueueId() { return queueId; }
	public String getPeerName() { return peerName; }
	public String getReceiverQoSMonitorPublicKey() { return receiverQoSMonitorPublicKey; }

	//-------------------------------------------------------------------------------------------------
	public void setQueueId(final String queueId) { this.queueId = queueId; }
	public void setPeerName(final String peerName) { this.peerName = peerName; }
	public void setReceiverQoSMonitorPublicKey(final String receiverQoSMonitorPublicKey) { this.receiverQoSMonitorPublicKey = receiverQoSMonitorPublicKey; }
}
package eu.arrowhead.core.gatekeeper.service;

import java.util.concurrent.BlockingQueue;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayResponse;
import eu.arrowhead.core.gatekeeper.relay.GeneralAdvertisementResult;

public class GSDPollTask implements Runnable{
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GSDPollTask.class);
	
	private final GatekeeperRelayClient relayClient;
	private final Session session;
	private final String recipientCloudCN;
	private final String recipientCloudPublicKey;
	private final GSDPollRequestDTO gsdPollRequestDTO;
	private final BlockingQueue<GSDPollResponseDTO> queue;

	//=================================================================================================
	// methods
		
	public GSDPollTask(final GatekeeperRelayClient relayClient, final Session session, final String recipientCloudCN, final String recipientCloudPublicKey,
					   final GSDPollRequestDTO gsdPollRequestDTO, final BlockingQueue<GSDPollResponseDTO> queue) {
		
		this.relayClient = relayClient;
		this.session = session;
		this.recipientCloudCN = recipientCloudCN;
		this.recipientCloudPublicKey = recipientCloudPublicKey;
		this.gsdPollRequestDTO = gsdPollRequestDTO;
		this.queue = queue;
	}


	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {
		logger.debug("GDSPollTask.run started...");
		
		Assert.notNull(relayClient, "relayClient is null");
		Assert.notNull(session, "session is null");
		Assert.isTrue(!Utilities.isEmpty(recipientCloudCN), "recipientCloudCN is empty");
		Assert.isTrue(!Utilities.isEmpty(recipientCloudPublicKey), "recipientCloudCN is empty");
		Assert.notNull(gsdPollRequestDTO, "gsdPollRequestDTO is null");
		Assert.notNull(queue, "queue is null");
		
		if (Thread.currentThread().isInterrupted()) {
			logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
			return;
		}
		
		try {
						
			final GeneralAdvertisementResult result = relayClient.publishGeneralAdvertisement(session, recipientCloudCN, recipientCloudPublicKey);
			final GatekeeperRelayResponse response = relayClient.sendRequestAndReturnResponse(session, result , gsdPollRequestDTO);
			
			queue.add(response.getGSDPollResponse());
			
		} catch (JMSException | ArrowheadException ex) {
			logger.debug("Exception:", ex.getMessage());			
		} finally {
			//adding empty responseDTO into the blocking queue in order to having exactly as many response as request was sent
			queue.add(new GSDPollResponseDTO()); 
		}
	}
}

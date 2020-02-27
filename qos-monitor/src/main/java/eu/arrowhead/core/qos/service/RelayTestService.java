package eu.arrowhead.core.qos.service;

import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;

@Service
public class RelayTestService {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
//	@EventListener
//	@Order(15) // to make sure QoSMonitorApplicationInitListener finished before this method is called (the common name and the keys are added to the context in the init listener)
//	public void onApplicationEvent(final ContextRefreshedEvent event) {
//		logger.debug("onApplicationEvent started...");
//		
//		if (!arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)) {
//			throw new ArrowheadException("Server's certificate not found.");
//		}
//		final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
//		
//		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
//			throw new ArrowheadException("Server's public key is not found.");
//		}
//		myPublicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
//		
//		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)) {
//			throw new ArrowheadException("Server's private key is not found.");
//		}
//		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);
//	
//		relayClient = GatewayRelayClientFactory.createGatewayRelayClient(serverCN, privateKey, sslProps);	
//	}

	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO joinRelayTest(final QoSRelayTestProposalRequestDTO request) {
		// TODO Auto-generated method stub
		return null;
	}
}
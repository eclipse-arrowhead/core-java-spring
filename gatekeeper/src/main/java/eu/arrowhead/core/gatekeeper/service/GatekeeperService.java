package eu.arrowhead.core.gatekeeper.service;

import java.util.List;

import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;

@Service
public class GatekeeperService {

	//=================================================================================================
	// members
	
	//=================================================================================================
	// methods
	
	public GSDQueryResultDTO initGSDPoll(final ServiceQueryFormDTO requestedService, final List<CloudResponseDTO> cloudBoundaries) {
		//TODO
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO doGSDPoll(final GSDPollRequestDTO request) {
		//TODO: implement
		
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO doICNProposal(final ICNProposalRequestDTO request) {
		//TODO: implement
		
		return null;
	}
}
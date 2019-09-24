package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class GSDQueryResultDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6750137008409005909L;
	
	private List<GSDPollResponseDTO> results;
	private int unsuccessfulRequests;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryResultDTO(final List<GSDPollResponseDTO> results, final int unsuccessfulRequests) {
		this.results = results;
		this.unsuccessfulRequests = unsuccessfulRequests;
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<GSDPollResponseDTO> getResults() { return results; }
	public int getUnsuccessfulRequests() { return unsuccessfulRequests; }
	
	//-------------------------------------------------------------------------------------------------	
	public void setResults(final List<GSDPollResponseDTO> results) { this.results = results; }
	public void setUnsuccessfulRequests(final int unsuccessfulRequests) { this.unsuccessfulRequests = unsuccessfulRequests; }	
}
package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TokenGenerationDetailedResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 7914253742329191383L;
	
	private String service;
	private String consumerName;
	private String consumerAdress;
	private int consumerPort;
	private List<TokenDataDTO> tokenData = new ArrayList<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public String getService() { return service; }
	public String getConsumerName() { return consumerName; }
	public String getConsumerAdress() { return consumerAdress; }
	public int getConsumerPort() { return consumerPort; }
	public List<TokenDataDTO> getTokenData() { return tokenData; }
	
	//-------------------------------------------------------------------------------------------------
	public void setService(final String service) { this.service = service; }
	public void setConsumerName(final String consumerName) { this.consumerName = consumerName; }
	public void setConsumerAdress(final String consumerAdress) { this.consumerAdress = consumerAdress; }
	public void setConsumerPort(final int consumerPort) { this.consumerPort = consumerPort; }
	public void setTokenData(final List<TokenDataDTO> tokenData) { this.tokenData = tokenData; }	
}

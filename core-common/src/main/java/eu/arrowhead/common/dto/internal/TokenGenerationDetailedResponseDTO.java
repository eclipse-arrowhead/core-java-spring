/********************************************************************************
 * Copyright (c) 2021 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}
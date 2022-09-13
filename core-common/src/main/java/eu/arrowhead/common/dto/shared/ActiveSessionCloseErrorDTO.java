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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActiveSessionCloseErrorDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1369339628228024167L;

	private int port;
	private String error;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveSessionCloseErrorDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ActiveSessionCloseErrorDTO(final int port, final String error) {
		this.port = port;
		this.error = error;
	}

	//-------------------------------------------------------------------------------------------------
	public int getPort() { return port; }
	public String getError() { return error; }

	//-------------------------------------------------------------------------------------------------
	public void setPort(final int port) { this.port = port; }
	public void setError(final String error) { this.error = error; }
	
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
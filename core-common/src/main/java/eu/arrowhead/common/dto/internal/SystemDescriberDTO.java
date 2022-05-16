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
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SystemDescriberDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 8527918606848411923L;
	
	private String systemName;
	private Map<String,String> metadata;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemDescriberDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SystemDescriberDTO(final String systemName, final  Map<String,String> metadata) {
		this.systemName = systemName;
		this.metadata = metadata;
	}

	//-------------------------------------------------------------------------------------------------
	public String getSystemName() { return systemName; }
	public Map<String,String> getMetadata() { return metadata; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setMetadata(final  Map<String,String> metadata) { this.metadata = metadata; }
	
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
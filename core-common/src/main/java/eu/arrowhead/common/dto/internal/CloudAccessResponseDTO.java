/********************************************************************************
 * Copyright (c) 2020 AITIA
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;

public class CloudAccessResponseDTO implements Serializable, ErrorWrapperDTO {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2559154226729243387L;
	
	private String cloudName;
	private String cloudOperator;
	private boolean directAccess;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudAccessResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudAccessResponseDTO(final String cloudName, final String cloudOperator, final boolean directAccess) {
		this.cloudName = cloudName;
		this.cloudOperator = cloudOperator;
		this.directAccess = directAccess;
	}

	//-------------------------------------------------------------------------------------------------
	public String getCloudName() { return cloudName; }
	public String getCloudOperator() { return cloudOperator; }
	public boolean isDirectAccess() { return directAccess; }

	//-------------------------------------------------------------------------------------------------
	public void setCloudName(final String cloudName) { this.cloudName = cloudName; }
	public void setCloudOperator(final String cloudOperator) { this.cloudOperator = cloudOperator; }
	public void setDirectAccess(final boolean directAccess) { this.directAccess = directAccess; }

	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	@Override
	public boolean isError() {
		return false;
	}

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

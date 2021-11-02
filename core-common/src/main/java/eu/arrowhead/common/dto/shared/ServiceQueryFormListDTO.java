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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceQueryFormListDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8818224363564234620L;
	
	private List<? extends ServiceQueryFormDTO> forms = new ArrayList<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormListDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormListDTO(final List<? extends ServiceQueryFormDTO> forms) {
		if (forms != null) {
			this.forms = forms;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<? extends ServiceQueryFormDTO> getForms() { return forms; }

	//-------------------------------------------------------------------------------------------------
	public void setForms(final List<? extends ServiceQueryFormDTO> forms) {
		if (forms != null) {
			this.forms = forms;
		}
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
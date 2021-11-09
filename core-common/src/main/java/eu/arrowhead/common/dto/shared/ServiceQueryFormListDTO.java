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

public class ServiceQueryFormListDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8818224363564234620L;
	
	private List<ServiceQueryFormDTO> forms = new ArrayList<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormListDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormListDTO(final List<ServiceQueryFormDTO> forms) {
		if (forms != null) {
			this.forms = forms;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public List<ServiceQueryFormDTO> getForms() { return forms; }

	//-------------------------------------------------------------------------------------------------
	public void setForms(final List<ServiceQueryFormDTO> forms) {
		if (forms != null) {
			this.forms = forms;
		}
	}
}
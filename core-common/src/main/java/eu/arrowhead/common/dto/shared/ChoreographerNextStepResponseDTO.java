/********************************************************************************
 * Copyright (c) 2019 AITIA
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

public class ChoreographerNextStepResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -4318735564703960811L;
	
	private long id;
    private String stepName;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerNextStepResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerNextStepResponseDTO(final long id, final String stepName) {
        this.id = id;
        this.stepName = stepName;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getStepName() { return stepName; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setStepName(final String stepName) { this.stepName = stepName; }
}
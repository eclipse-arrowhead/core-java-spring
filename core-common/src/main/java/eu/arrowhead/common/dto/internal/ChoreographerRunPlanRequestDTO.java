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

public class ChoreographerRunPlanRequestDTO {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -4337560592612039357L;

    private long id;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunPlanRequestDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunPlanRequestDTO(long id) {
        this.id = id;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
}

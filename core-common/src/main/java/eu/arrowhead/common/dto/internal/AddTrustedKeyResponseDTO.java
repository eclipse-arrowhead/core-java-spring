/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class AddTrustedKeyResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String validAfter;
    private String validBefore;

    public AddTrustedKeyResponseDTO(long id) {
        this(id, null, null);
    }

    public AddTrustedKeyResponseDTO(long id, String validAfter, String validBefore) {
        this.id = id;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    public String getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(String validAfter) {
        this.validAfter = validAfter;
    }

    public String getValidBefore() {
        return validBefore;
    }

    public void setValidBefore(String validBefore) {
        this.validBefore = validBefore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

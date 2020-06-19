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
import java.util.ArrayList;
import java.util.List;

public class TrustedKeysResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long count;
    private List<TrustedKeyDTO> trustedKeys;

    public TrustedKeysResponseDTO(List<TrustedKeyDTO> trustedKeyDTOs, int count) {
        this.count = count;
        setTrustedKeys(trustedKeyDTOs);
    }

    public TrustedKeysResponseDTO() {
        setTrustedKeys(new ArrayList<>());
    }

    public List<TrustedKeyDTO> getTrustedKeys() {
        return trustedKeys;
    }

    public void setTrustedKeys(List<TrustedKeyDTO> trustedKeys) {
        this.trustedKeys = trustedKeys;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

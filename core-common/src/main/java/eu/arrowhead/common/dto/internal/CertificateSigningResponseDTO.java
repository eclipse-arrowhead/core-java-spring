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
import java.util.List;

public class CertificateSigningResponseDTO implements Serializable {

    private static final long serialVersionUID = -6810780579000655432L;

    private long id;
    private List<String> certificateChain;

    public CertificateSigningResponseDTO() {
        this.id = 0;
    }

    public CertificateSigningResponseDTO(long id) {
        this.id = id;
    }

    public CertificateSigningResponseDTO(long id, List<String> certificateChain) {
        this.id = id;
        this.certificateChain = certificateChain;
    }

    public List<String> getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(List<String> certificateChain) {
        this.certificateChain = certificateChain;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

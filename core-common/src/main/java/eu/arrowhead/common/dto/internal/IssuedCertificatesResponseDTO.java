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

public class IssuedCertificatesResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long count;
    private List<IssuedCertificateDTO> issuedCertificates;

    public IssuedCertificatesResponseDTO() {
        setIssuedCertificates(new ArrayList<>());
    }

    public IssuedCertificatesResponseDTO(List<IssuedCertificateDTO> certificates, long count) {
        setIssuedCertificates(certificates);
        this.count = count;
    }

    public List<IssuedCertificateDTO> getIssuedCertificates() {
        return issuedCertificates;
    }

    public void setIssuedCertificates(List<IssuedCertificateDTO> issuedCertificates) {
        this.issuedCertificates = issuedCertificates;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

}

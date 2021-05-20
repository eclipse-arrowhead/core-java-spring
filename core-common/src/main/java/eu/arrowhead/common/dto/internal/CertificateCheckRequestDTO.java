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

import javax.validation.constraints.NotBlank;

public class CertificateCheckRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int version;

    @NotBlank(message = "The certificate is mandatory")
    private String certificate;

    public CertificateCheckRequestDTO() {}

    public CertificateCheckRequestDTO(int version, String certificate) {
        this.version = version;
        this.certificate = certificate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}

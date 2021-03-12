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

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class CertificateSigningRequestDTO implements Serializable {

    private static final long serialVersionUID = -6810780579000655432L;

    @NotBlank(message = "The encodedCSR is mandatory")
    private String encodedCSR;
    private String validAfter;
    private String validBefore;

    public CertificateSigningRequestDTO() {
    }

    public CertificateSigningRequestDTO(String encodedCSR) {
        this.encodedCSR = encodedCSR;
    }

    public CertificateSigningRequestDTO(String encodedCSR, String validAfter, String validBefore) {
        this.encodedCSR = encodedCSR;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    public String getEncodedCSR() {
        return encodedCSR;
    }

    public void setEncodedCSR(String encodedCSR) {
        this.encodedCSR = encodedCSR;
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
}

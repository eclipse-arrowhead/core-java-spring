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

public class AddTrustedKeyRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "The publicKey is mandatory")
    private String publicKey;

    @NotBlank(message = "The description is mandatory")
    private String description;

    @NotBlank(message = "The validAfter field is mandatory")
    private String validAfter;

    @NotBlank(message = "The validBefore field is mandatory")
    private String validBefore;

    public AddTrustedKeyRequestDTO() {
    }

    public AddTrustedKeyRequestDTO(String publicKey, String description, String validAfter, String validBefore) {
        this.publicKey = publicKey;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
        this.description = description;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getValidBefore() {
        return validBefore;
    }

    public void setValidBefore(String validBefore) {
        this.validBefore = validBefore;
    }

    public String getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(String validAfter) {
        this.validAfter = validAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

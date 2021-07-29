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
import java.math.BigInteger;

public class IssuedCertificateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String createdBy;
    private String createdAt;
    private String revokedAt;
    private String validFrom;
    private String validUntil;
    private String commonName;
    private BigInteger serialNumber;
    private IssuedCertificateStatus status;

    public IssuedCertificateDTO() {
        status = IssuedCertificateStatus.UNKNOWN;
    }

    public IssuedCertificateDTO(long id, String createdBy, String createdAt, String revokedAt,
                                String validFrom, String validUntil,
                                String commonName, BigInteger serialNumber, IssuedCertificateStatus status) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.commonName = commonName;
        this.serialNumber = serialNumber;
        this.status = status;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(String revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    public IssuedCertificateStatus getStatus() {
        return status;
    }

    public void setStatus(IssuedCertificateStatus status) {
        this.status = status;
    }
}

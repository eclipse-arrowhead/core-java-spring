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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IssuedCertificateDTO implements Serializable {

    //=================================================================================================
	// members
	
	private static final long serialVersionUID = -7430192640059791256L;
	
	private long id;
    private String createdBy;
    private String createdAt;
    private String revokedAt;
    private String validFrom;
    private String validUntil;
    private String commonName;
    private BigInteger serialNumber;
    private IssuedCertificateStatus status;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public IssuedCertificateDTO() {
        status = IssuedCertificateStatus.UNKNOWN;
    }

    //-------------------------------------------------------------------------------------------------
	public IssuedCertificateDTO(final long id, final String createdBy, final String createdAt, final String revokedAt,
                                final String validFrom, final String validUntil,
                                final String commonName, final BigInteger serialNumber, final IssuedCertificateStatus status) {
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

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getCreatedBy() { return createdBy; }
	public String getCreatedAt() { return createdAt; }
	public String getRevokedAt() { return revokedAt; }
	public String getValidFrom() { return validFrom; }
	public String getValidUntil() { return validUntil; }
	public String getCommonName() { return commonName; }
	public BigInteger getSerialNumber() { return serialNumber; }
	public IssuedCertificateStatus getStatus() { return status; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setCreatedBy(final String createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setRevokedAt(final String revokedAt) { this.revokedAt = revokedAt; }
    public void setValidFrom(final String validFrom) { this.validFrom = validFrom; }
    public void setValidUntil(final String validUntil) { this.validUntil = validUntil; }
    public void setCommonName(final String commonName) { this.commonName = commonName; }
    public void setSerialNumber(final BigInteger serialNumber) { this.serialNumber = serialNumber; }
    public void setStatus(final IssuedCertificateStatus status) { this.status = status; }
    
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}
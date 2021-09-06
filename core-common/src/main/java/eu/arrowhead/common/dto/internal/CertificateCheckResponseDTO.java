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

public class CertificateCheckResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1474805207746099331L;
	
	private int version;
    private String producedAt;
    private String endOfValidity;
    private String commonName;
    private BigInteger serialNumber;
    private IssuedCertificateStatus status;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public CertificateCheckResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public CertificateCheckResponseDTO(final int version, final String producedAt, final String endOfValidity, final String commonName,
                                       final BigInteger serialNumber, final IssuedCertificateStatus status) {
        this.version = version;
        this.producedAt = producedAt;
        this.endOfValidity = endOfValidity;
        this.commonName = commonName;
        this.serialNumber = serialNumber;
        this.status = status;
    }

    //-------------------------------------------------------------------------------------------------
	public int getVersion() { return version; }
	public String getProducedAt() { return producedAt; }
	public String getEndOfValidity() { return endOfValidity; }
	public String getCommonName() { return commonName; }
	public BigInteger getSerialNumber() { return serialNumber; }
	public IssuedCertificateStatus getStatus() { return status; }

    //-------------------------------------------------------------------------------------------------
	public void setVersion(final int version) { this.version = version; }
    public void setProducedAt(final String producedAt) { this.producedAt = producedAt; }
    public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
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
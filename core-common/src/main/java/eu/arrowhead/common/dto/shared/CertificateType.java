/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.util.Objects;

import eu.arrowhead.common.Utilities;
import org.springframework.util.Assert;

public enum CertificateType {

    AH_ONBOARDING("onboarding", 0),
    AH_DEVICE("device", 1),
    AH_SYSTEM("", 2),
    UNKNOWN("", 0);
	
	//=================================================================================================
	// members

    private final String commonNamePart;
    private final int strength;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	CertificateType(final String commonNamePart, final int strength) {
        this.commonNamePart = commonNamePart;
        this.strength = strength;
    }

	//-------------------------------------------------------------------------------------------------
	public static CertificateType getTypeFromCN(final String commonName) {
        if (Objects.isNull(commonName)) { return CertificateType.UNKNOWN; }

        final String[] split = commonName.split("\\.");

        if (Utilities.isKeyStoreCNArrowheadValid(commonName)) {
            return AH_SYSTEM;
        } else if (split.length >= 2) {
            return CertificateType.getTypeFromCNPart(split[1]);
        } else {
            return CertificateType.getTypeFromCNPart(split[0]);
        }
    }
	
	//-------------------------------------------------------------------------------------------------
	public boolean hasMinimumStrength(final CertificateType minimumStrengthType) {
		Assert.notNull(minimumStrengthType, "CertificateType must not be null");
		return this.strength >= minimumStrengthType.strength;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String appendTypeToCN(final String commonName) {
		/* if (Utilities.notEmpty(commonNamePart)) {
            return commonName + '.' + commonNamePart;
        } else { return commonName; } */
		return commonName;
	}
	
	//=================================================================================================
	// assistant methods

    //-------------------------------------------------------------------------------------------------
	private static CertificateType getTypeFromCNPart(final String commonNamePart) {
        for (final CertificateType type : values()) {
            if (type.commonNamePart.equalsIgnoreCase(commonNamePart)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
package eu.arrowhead.common.dto.shared;

import java.util.Objects;

import eu.arrowhead.common.Utilities;
import org.springframework.util.Assert;

public enum CertificateType {

    AH_ONBOARDING("onboarding", 0),
    AH_DEVICE("device", 1),
    AH_SYSTEM("", 2),
    UNKNOWN("", 0);

    private final String commonNamePart;
    private final int strength;

    CertificateType(final String commonNamePart, final int strength) {
        this.commonNamePart = commonNamePart;
        this.strength = strength;
    }

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

    private static CertificateType getTypeFromCNPart(final String commonNamePart) {
        for (CertificateType type : values()) {
            if (type.commonNamePart.equalsIgnoreCase(commonNamePart)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public boolean hasMinimumStrength(final CertificateType minimumStrengthType) {
        Assert.notNull(minimumStrengthType, "CertificateType must not be null");
        return this.strength >= minimumStrengthType.strength;
    }

    public String appendTypeToCN(final String commonName) {
        /* if (Utilities.notEmpty(commonNamePart)) {
            return commonName + '.' + commonNamePart;
        } else { return commonName; } */
        return commonName;
    }
}

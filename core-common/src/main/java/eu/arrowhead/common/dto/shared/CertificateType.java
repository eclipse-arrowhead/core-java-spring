package eu.arrowhead.common.dto.shared;

import eu.arrowhead.common.Utilities;

public enum CertificateType {

    AH_ONBOARDING("onboarding", 0),
    AH_DEVICE("device", 1),
    AH_SYSTEM("system", 2),
    UNKNOWN("unknown", 0);

    private final String commonNamePart;
    private final int strength;

    private CertificateType(final String commonNamePart, final int strength) {
        this.commonNamePart = commonNamePart;
        this.strength = strength;
    }

    public static CertificateType getTypeFromCN(final String commonName) {
        final String[] split = commonName.split("\\.");

        if(Utilities.isKeyStoreCNArrowheadValid(commonName)) {
            return AH_SYSTEM;
        } else if (split.length >= 2) {
            return CertificateType.getTypeFromCNPart(split[1]);
        } else {
            return CertificateType.getTypeFromCNPart(split[0]);
        }
    }

    public boolean hasMinimumStrength(final CertificateType minimumStrengthType) {
        return this.strength >= minimumStrengthType.strength;
    }

    private static CertificateType getTypeFromCNPart(final String commonNamePart) {
        for (CertificateType type : values()) {
            if (type.commonNamePart.equalsIgnoreCase(commonNamePart)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getCommonNamePart() {
        return commonNamePart;
    }
}

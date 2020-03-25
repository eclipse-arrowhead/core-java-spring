package eu.arrowhead.common.dto.internal;

public enum CertificateType {

    ONBOARDING("onboarding"),
    DEVICE("device"),
    SYSTEM("system"),
    SERVICE("service"),
    UNKNOWN("unknown");

    private final String commonNamePart;

    private CertificateType(final String commonNamePart) {
        this.commonNamePart = commonNamePart;
    }

    public static CertificateType getTypeFromCN(final String commonName) {
        final String[] split = commonName.split("\\.");
        if (split.length >= 2) {
            return CertificateType.getTypeFromCNPart(split[1]);
        }

        return UNKNOWN;
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

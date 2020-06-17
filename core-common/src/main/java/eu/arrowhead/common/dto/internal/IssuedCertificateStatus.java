package eu.arrowhead.common.dto.internal;

public enum IssuedCertificateStatus {
    UNKNOWN("unknown"),
    GOOD("good"),
    REVOKED("revoked"),
    EXPIRED("expired");

    IssuedCertificateStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    private final String name;
}

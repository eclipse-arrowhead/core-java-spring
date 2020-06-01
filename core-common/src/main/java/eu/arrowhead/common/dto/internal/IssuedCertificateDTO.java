package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.math.BigInteger;

public class IssuedCertificateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        GOOD("good"), REVOKED("revoked"), EXPIRED("expired");

        Status(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    private long id;
    private String createdBy;
    private String createdAt;
    private String revokedAt;
    private String validFrom;
    private String validUntil;
    private String commonName;
    private BigInteger serialNumber;
    private Status status;

    public IssuedCertificateDTO() {
    }

    public IssuedCertificateDTO(long id, String createdBy, String createdAt, String revokedAt,
                                String validFrom, String validUntil,
                                String commonName, BigInteger serialNumber, Status status) {
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

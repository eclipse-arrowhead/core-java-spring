package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.ZonedDateTime;

public class IssuedCertificateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static enum Status {
        GOOD("good"), REVOKED("revoked"), EXPIRED("expired");

        Status(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private String name;
    }

    private long id;
    private String createdBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime revokedAt;
    private ZonedDateTime validFrom;
    private ZonedDateTime validUntil;
    private String commonName;
    private BigInteger serialNumber;
    private Status status;

    public IssuedCertificateDTO() {
    }

    public IssuedCertificateDTO(long id, String createdBy, ZonedDateTime createdAt, ZonedDateTime revokedAt,
            ZonedDateTime validFrom, ZonedDateTime validUntil, String commonName, BigInteger serialNumber, Status status) {
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(ZonedDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(ZonedDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public ZonedDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(ZonedDateTime validUntil) {
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

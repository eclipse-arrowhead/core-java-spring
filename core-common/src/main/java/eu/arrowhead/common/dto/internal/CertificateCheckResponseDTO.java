package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.ZonedDateTime;

public class CertificateCheckResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int version;
    private ZonedDateTime producedAt;
    private ZonedDateTime endOfValidity;
    private String commonName;
    private BigInteger serialNumber;
    private String status;

    public CertificateCheckResponseDTO() {
    }

    public CertificateCheckResponseDTO(String commonName, 
            BigInteger serialNumber, String status,
            ZonedDateTime endOfValidity) {
        this(1, ZonedDateTime.now(), endOfValidity, commonName, serialNumber, status);
    }

    public CertificateCheckResponseDTO(int version, ZonedDateTime producedAt, ZonedDateTime endOfValidity,
            String commonName, BigInteger serialNumber, String status) {
        this.version = version;
        this.producedAt = producedAt;
        this.endOfValidity = endOfValidity;
        this.commonName = commonName;
        this.serialNumber = serialNumber;
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ZonedDateTime getProducedAt() {
        return producedAt;
    }

    public void setProducedAt(ZonedDateTime producedAt) {
        this.producedAt = producedAt;
    }

    public ZonedDateTime getEndOfValidity() {
        return endOfValidity;
    }

    public void setEndOfValidity(ZonedDateTime endOfValidity) {
        this.endOfValidity = endOfValidity;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

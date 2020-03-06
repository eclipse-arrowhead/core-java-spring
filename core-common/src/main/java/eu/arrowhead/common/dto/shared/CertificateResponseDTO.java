package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class CertificateResponseDTO implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    private String certificate;
    private String keyAlgorithm;
    private String keyFormat;
    private byte[] publicKey;
    private byte[] privateKey;


    //=================================================================================================
    // constructors

    public CertificateResponseDTO() {
    }

    public CertificateResponseDTO(final String certificate, final String keyAlgorithm, final String keyFormat,
                                  final byte[] publicKey) {
        this.certificate = certificate;
        this.keyAlgorithm = keyAlgorithm;
        this.keyFormat = keyFormat;
        this.publicKey = publicKey;
    }

    public CertificateResponseDTO(final String certificate, final String keyAlgorithm, final String keyFormat,
                                  final byte[] publicKey, final byte[] privateKey) {
        this.certificate = certificate;
        this.keyAlgorithm = keyAlgorithm;
        this.keyFormat = keyFormat;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    //=================================================================================================
    // methods

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertificateResponseDTO that = (CertificateResponseDTO) o;
        return certificate.equals(that.certificate) &&
                Objects.equals(keyAlgorithm, that.keyAlgorithm) &&
                Objects.equals(keyFormat, that.keyFormat) &&
                Arrays.equals(publicKey, that.publicKey) &&
                Arrays.equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(certificate, keyAlgorithm, keyFormat);
        result = 31 * result + Arrays.hashCode(publicKey);
        result = 31 * result + Arrays.hashCode(privateKey);
        return result;
    }
}

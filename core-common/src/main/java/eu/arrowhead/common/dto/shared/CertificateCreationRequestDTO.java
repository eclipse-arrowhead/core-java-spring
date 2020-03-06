package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class CertificateCreationRequestDTO implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    private String commonName;
    private byte[] publicKey;
    private byte[] privateKey;


    //=================================================================================================
    // constructors

    public CertificateCreationRequestDTO() {
    }

    public CertificateCreationRequestDTO(final String commonName) {
        this.commonName = commonName;
    }

    public CertificateCreationRequestDTO(final String commonName, final byte[] publicKey, final byte[] privateKey) {
        this.commonName = commonName;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    //=================================================================================================
    // methods

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
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
}

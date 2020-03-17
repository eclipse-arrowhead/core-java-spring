package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class CertificateCreationRequestDTO implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    private String commonName;
    private String publicKey;
    private String privateKey;


    //=================================================================================================
    // constructors

    public CertificateCreationRequestDTO() {
    }

    public CertificateCreationRequestDTO(final String commonName) {
        this.commonName = commonName;
    }

    public CertificateCreationRequestDTO(final String commonName, final String publicKey, final String privateKey) {
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}

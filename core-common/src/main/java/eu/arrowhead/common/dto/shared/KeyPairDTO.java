package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class KeyPairDTO implements Serializable {

    private String keyAlgorithm;
    private String keyFormat;
    private String publicKey;
    private String privateKey;

    public KeyPairDTO() { super(); }

    public KeyPairDTO(final String keyAlgorithm, final String keyFormat, final String publicKey, final String privateKey) {
        this.keyAlgorithm = keyAlgorithm;
        this.keyFormat = keyFormat;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(final String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(final String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KeyPairDTO.class.getSimpleName() + "[", "]")
                .add("keyAlgorithm='" + keyAlgorithm + "'")
                .add("keyFormat='" + keyFormat + "'")
                .add("publicKey='" + publicKey + "'")
                .add("privateKey='" + Objects.nonNull(privateKey) + "'")
                .toString();
    }
}

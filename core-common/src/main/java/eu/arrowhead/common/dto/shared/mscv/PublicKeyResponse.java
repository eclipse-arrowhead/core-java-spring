package eu.arrowhead.common.dto.shared.mscv;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class PublicKeyResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String base64PublicKey;
    private String sshPublicKey;
    private String algorithm;
    private String format;

    public PublicKeyResponse() {
        super();
    }

    public PublicKeyResponse(final String base64PublicKey, final String sshPublicKey, final String algorithm, final String format) {
        this.base64PublicKey = base64PublicKey;
        this.sshPublicKey = sshPublicKey;
        this.algorithm = algorithm;
        this.format = format;
    }

    public String getBase64PublicKey() {
        return base64PublicKey;
    }

    public void setBase64PublicKey(final String base64PublicKey) {
        this.base64PublicKey = base64PublicKey;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(final String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final PublicKeyResponse that = (PublicKeyResponse) o;
        return Objects.equals(base64PublicKey, that.base64PublicKey) &&
                Objects.equals(sshPublicKey, that.sshPublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base64PublicKey, sshPublicKey);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PublicKeyResponse.class.getSimpleName() + "[", "]")
                .add("base64PublicKey='...'")
                .add("sshPublicKey='...'")
                .toString();
    }
}

package eu.arrowhead.common.dto.shared.mscv;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class TargetLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private SshTargetDto target;
    private String credentials;

    public TargetLoginRequest() { super(); }

    public SshTargetDto getTarget() {
        return target;
    }

    public void setTarget(final SshTargetDto target) {
        this.target = target;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(final String credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final TargetLoginRequest that = (TargetLoginRequest) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, credentials);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TargetLoginRequest.class.getSimpleName() + "[", "]")
                .add("target=" + target)
                .add("credentials='" + credentials + "'")
                .toString();
    }
}

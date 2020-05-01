package eu.arrowhead.core.mscv.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MscvKeyPairProvider implements KeyPairProvider {

    private final KeyPairFileStorage msvcKeyPair;

    @Autowired
    public MscvKeyPairProvider(final KeyPairFileStorage msvcKeyPair) {
        super();
        this.msvcKeyPair = msvcKeyPair;
    }

    @Override
    public Iterable<KeyPair> loadKeys(final SessionContext session) throws IOException, GeneralSecurityException {
        // TODO implement
        return null;
    }
}

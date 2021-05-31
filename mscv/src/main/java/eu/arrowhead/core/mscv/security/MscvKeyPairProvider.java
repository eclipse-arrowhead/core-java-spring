package eu.arrowhead.core.mscv.security;

import java.security.KeyPair;
import java.util.Set;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MscvKeyPairProvider implements KeyPairProvider {

    private final KeyPairFileStorage mscvKeyPair;

    @Autowired
    public MscvKeyPairProvider(final KeyPairFileStorage mscvKeyPair) {
        super();
        this.mscvKeyPair = mscvKeyPair;
    }

    @Override
    public Iterable<KeyPair> loadKeys(final SessionContext session) {

        return Set.of(mscvKeyPair.getKeyPair());
    }
}

package eu.arrowhead.core.mscv.handlers;

import java.net.SocketAddress;
import java.security.PublicKey;
import java.util.Base64;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Accepts all server public keys and persists them in the database in SshTarget.authInfo
 */
@Component
public class UpdatingAcceptAllKeyVerifier extends AbstractHostKeyVerifier implements ServerKeyVerifier {

    private final Logger logger = LogManager.getLogger();
    private final SshTargetRepository targetRepository;

    @Autowired
    public UpdatingAcceptAllKeyVerifier(final SshTargetRepository targetRepository) {
        super(targetRepository);
        this.targetRepository = targetRepository;
    }

    @Override
    public boolean verifyServerKey(final ClientSession clientSession, final SocketAddress remoteAddress, final PublicKey serverKey) {

        try {
            final SshTarget target = findMatchingTarget(clientSession, remoteAddress);
            target.setAuthInfo(Base64.getEncoder().encodeToString(serverKey.getEncoded()));
            targetRepository.saveAndFlush(target);
            return true;
        } catch (final Exception e) {
            logger.warn("SSH server key encoding failed: {}", e.getMessage());
            return false;
        }
    }
}

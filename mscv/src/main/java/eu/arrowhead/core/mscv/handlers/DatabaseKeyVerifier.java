package eu.arrowhead.core.mscv.handlers;

import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Uses the database SshTarget.authInfo field to verify the server public key
 */
@Component
public class DatabaseKeyVerifier extends AbstractHostKeyVerifier implements ServerKeyVerifier {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    public DatabaseKeyVerifier(final SshTargetRepository targetRepository) {
        super(targetRepository);
    }

    @Override
    public boolean verifyServerKey(final ClientSession clientSession, final SocketAddress remoteAddress, final PublicKey serverKey) {

        try {
            final KeyFactory keyFactory = KeyFactory.getInstance(serverKey.getAlgorithm());
            final SshTarget target = findMatchingTarget(clientSession, remoteAddress);

            if (Objects.isNull(target.getAuthInfo())) {
                logger.debug("SSH public key of {} was never verified ...", target);
                return false;
            }

            final KeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(target.getAuthInfo()));
            final PublicKey authInfo = keyFactory.generatePublic(keySpec);

            if (KeyUtils.compareKeys(authInfo, serverKey)) {
                logger.debug("Successfully verified ssh public key of {}", target);
                return true;
            } else {
                logger.debug("Failed to verify ssh public key of {}", target);
                return false;
            }

        } catch (final Exception e) {
            logger.warn("SSH server key verification failed: {}", e.getMessage());
            return false;
        }
    }
}

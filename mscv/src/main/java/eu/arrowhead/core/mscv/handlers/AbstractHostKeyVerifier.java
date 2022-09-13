package eu.arrowhead.core.mscv.handlers;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.net.SshdSocketAddress;

public abstract class AbstractHostKeyVerifier {

    private final Logger logger = LogManager.getLogger();
    private final SshTargetRepository targetRepository;

    protected AbstractHostKeyVerifier(final SshTargetRepository targetRepository) {this.targetRepository = targetRepository;}

    protected SshTarget findMatchingTarget(final ClientSession clientSession, final SocketAddress remoteAddress) throws SshException {
        final Collection<SshdSocketAddress> candidates = resolveHostNetworkIdentities(clientSession, remoteAddress);
        if (GenericUtils.isEmpty(candidates)) {
            throw new SshException("Unable to resolve host network identities for " + remoteAddress);
        }

        for (SshdSocketAddress host : candidates) {
            logger.debug("Searching database for ssh target entries with {}:{}", host.getHostName(), host.getPort());
            final Optional<SshTarget> optionalSshTarget = targetRepository.findByAddressAndPort(host.getHostName(), host.getPort());
            if (optionalSshTarget.isEmpty()) {
                continue;
            }

            return optionalSshTarget.get();
        }

        throw new SshException("No database entries found for " + remoteAddress);
    }


    /*
     * copy from org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier
     */
    protected Collection<SshdSocketAddress> resolveHostNetworkIdentities(final ClientSession clientSession, final SocketAddress remoteAddress) {
        /*
         * NOTE !!! we do not resolve the fully-qualified name to avoid long DNS timeouts.
         * Instead we use the reported peer address and the original connection target host
         */
        Collection<SshdSocketAddress> candidates = new TreeSet<>(SshdSocketAddress.BY_HOST_AND_PORT);
        candidates.add(SshdSocketAddress.toSshdSocketAddress(remoteAddress));
        SocketAddress connectAddress = clientSession.getConnectAddress();
        candidates.add(SshdSocketAddress.toSshdSocketAddress(connectAddress));
        return candidates;
    }
}

package eu.arrowhead.core.mscv.delegate;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.core.mscv.MscvDefaults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.Validation.TARGET_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.USERNAME_NULL_ERROR_MESSAGE;

@Component
public class SshExecutionHandler implements ExecutionHandler<SshTarget> {

    private static final int SSH_HOST_VERIFICATION_ERROR = SshConstants.SSH2_DISCONNECT_HOST_KEY_NOT_VERIFIABLE;
    private final Logger logger = LogManager.getLogger();
    private final MscvDefaults.SshDefaults sshDefaults;
    private final UpdatingAcceptAllKeyVerifier verifier;
    private final SshClient sshClient;

    @Autowired
    public SshExecutionHandler(final MscvDefaults.SshDefaults sshDefaults,
                               final UpdatingAcceptAllKeyVerifier verifier,
                               final SshClient sshClient) {
        super();
        this.sshDefaults = sshDefaults;
        this.verifier = verifier;
        this.sshClient = sshClient;
    }

    @Override
    public void login(final SshTarget sshTarget, final String username, @Nullable final String password) throws AuthException {

        Assert.notNull(sshTarget, TARGET_NULL_ERROR_MESSAGE);
        Assert.hasText(username, USERNAME_NULL_ERROR_MESSAGE);

        try (final ClientSession session = sshClient.connect(username, sshTarget.getAddress(), sshTarget.getPort())
                                                    .verify(sshDefaults.getConnectTimeout(), TimeUnit.SECONDS)
                                                    .getClientSession()) {

            session.addPasswordIdentity(password);
            session.setServerKeyVerifier(verifier);

            // TODO create MSCV KeyPair and save it into database

            final AuthFuture authentication = session.auth().verify(sshDefaults.getAuthTimeout());

            if(!authentication.isDone()) {
                authentication.cancel();
                throw new AuthException("SSH login failure: timeout during ssh authentication!");
            } else if(authentication.isFailure()) {
                final Throwable t = authentication.getException();
                throw new AuthException("SSH login failure: " + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
            }

            session.createExecChannel(null);


        } catch (final IOException e) {
            logger.error("Unable to login to {} with user {} and provided password: {}: {}",
                         sshTarget, username, e.getClass().getSimpleName(), e.getMessage());
            throw new AuthException("SSH login failure: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public void executeScripts(final Queue<Script> scriptsQueue, final SshTarget sshTarget) {

    }

    @Override
    public Class<SshTarget> getType() {
        return SshTarget.class;
    }
}

package eu.arrowhead.core.mscv.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.security.KeyPairFileStorage;
import eu.arrowhead.core.mscv.service.MscvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.Validation.TARGET_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.USERNAME_NULL_ERROR_MESSAGE;

public class ShellBasedSshExecutionHandler implements ExecutionHandler<SshTarget> {

    private static final String AUTHORIZED_KEYS = "~/.ssh/authorized_keys";
    private final Logger logger = LogManager.getLogger();
    private final MscvDefaults.SshDefaults sshDefaults;
    private final UpdatingAcceptAllKeyVerifier acceptAllKeyVerifier;
    private final KeyPairFileStorage keyPairFileStorage;
    private final SshClient sshClient;

    @Autowired
    public ShellBasedSshExecutionHandler(final MscvDefaults mscvDefaults,
                                         final UpdatingAcceptAllKeyVerifier acceptAllKeyVerifier,
                                         final KeyPairFileStorage keyPairFileStorage, final SshClient sshClient) {
        super();
        this.sshDefaults = mscvDefaults.getSsh();
        this.acceptAllKeyVerifier = acceptAllKeyVerifier;
        this.keyPairFileStorage = keyPairFileStorage;
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
            session.setServerKeyVerifier(acceptAllKeyVerifier);

            final AuthFuture authentication = session.auth()
                                                     .verify(sshDefaults.getAuthTimeout(), TimeUnit.SECONDS);

            if (!authentication.isDone()) {
                authentication.cancel();
                throw new AuthException("SSH login failure: timeout during ssh authentication!");
            } else if (authentication.isFailure()) {
                final Throwable t = authentication.getException();
                throw new AuthException("SSH login failure: " + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
            }

            copySshIdentity(session);
            session.disconnect(0, "exit");

        } catch (final IOException | InvalidKeySpecException e) {
            logger.error("Unable to login to {} with user {} and provided password. Reason: {}: {}",
                         sshTarget, username, e.getClass().getSimpleName(), e.getMessage());
            throw new AuthException("SSH login failure: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public boolean verifyPasswordlessLogin(final SshTarget sshTarget) {
        Assert.notNull(sshTarget, TARGET_NULL_ERROR_MESSAGE);

        try (final ClientSession session = sshClient.connect(sshTarget.getUsername(), sshTarget.getAddress(), sshTarget.getPort())
                                                    .verify(sshDefaults.getConnectTimeout(), TimeUnit.SECONDS)
                                                    .getClientSession()) {

            final AuthFuture authentication = session.auth()
                                                     .verify(sshDefaults.getAuthTimeout(), TimeUnit.SECONDS);

            if (!authentication.isDone()) {
                authentication.cancel();
                throw new AuthException("SSH login failure: timeout during ssh authentication!");
            } else if (authentication.isFailure()) {
                final Throwable t = authentication.getException();
                logger.warn("Unable to login to {}@{}:{} - {}", sshTarget.getUsername(), sshTarget.getAddress(),
                            sshTarget.getPort(), t.getMessage());
                return false;
            }

            session.disconnect(0, "exit");
            return true;

        } catch (final IOException e) {
            logger.error("Unable to login to {} with user {} and provided password. Reason: {}: {}",
                         sshTarget, sshTarget.getUsername(), e.getClass().getSimpleName(), e.getMessage());
            throw new AuthException("SSH login failure: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public void performVerification(final VerificationResult intermediateResult, final Collection<VerificationResultDetail> resultDetails)
            throws MscvException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Class<SshTarget> getType() {
        return SshTarget.class;
    }

    private void copySshIdentity(final ClientSession session) throws IOException, InvalidKeySpecException {

        final String sshIdentity = keyPairFileStorage.getPublicKeyAsSshIdentityString();
        final int userPart = sshIdentity.lastIndexOf("mscv");
        final String sshPublicKey = sshIdentity.substring(0, userPart).trim();

        final CharBuffer buffer = CharBuffer.allocate(1024);

        try (final ChannelShell channel = session.createShellChannel()) {
            channel.open().await(sshDefaults.getAuthTimeout(), TimeUnit.SECONDS);

            // once channel gets created, we open the streams
            try (final PrintWriter writeToHost = new PrintWriter(new OutputStreamWriter(channel.getInvertedIn()));
                 final BufferedReader readFromHost = new BufferedReader(new InputStreamReader(channel.getInvertedOut()));
                 final ByteArrayOutputStream err = new ByteArrayOutputStream()) {

                channel.setErr(err);

                channel.waitFor(EnumSet.of(ClientChannelEvent.STDOUT_DATA), 1000L);
                consumerConsole(readFromHost, buffer);

                writeToHost.println("mkdir -p ~/.ssh");
                writeToHost.flush();

                channel.waitFor(EnumSet.of(ClientChannelEvent.STDOUT_DATA), 1000L);
                consumerConsole(readFromHost, buffer);

                writeToHost.println("grep --color=never 'mscv' " + AUTHORIZED_KEYS);
                writeToHost.flush();
                channel.waitFor(EnumSet.of(ClientChannelEvent.STDOUT_DATA), 1000L);

                readFromHost.readLine(); // our own grep
                final String grepResult = readFromHost.readLine(); // grep result
                if (grepResult.contains(sshPublicKey)) {
                    logger.debug("Public key exists already. Nothing to do anymore");
                } else {
                    writeToHost.println("echo \"" + sshIdentity + "\" >>" + AUTHORIZED_KEYS);
                    writeToHost.println("chmod 600 " + AUTHORIZED_KEYS);
                    writeToHost.flush();

                    channel.waitFor(EnumSet.of(ClientChannelEvent.STDOUT_DATA), 1000L);
                    consumerConsole(readFromHost, buffer);
                }

                writeToHost.println("exit");
                writeToHost.flush();
                channel.waitFor(EnumSet.of(ClientChannelEvent.EXIT_SIGNAL, ClientChannelEvent.CLOSED), 1000L);

                logger.info("Successfully logged into {} to exchange keys", session.getConnectAddress());

            } catch (IOException e) {
                logger.error("Unhandled IOException during ssh identity exchange: {}", e.getMessage(), e);
                throw e;
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void consumerConsole(final BufferedReader readFromHost, final CharBuffer buffer) throws IOException {
        do {
            readFromHost.read(buffer);
            buffer.flip();
            if (buffer.hasRemaining()) { logger.debug(buffer.toString()); }
            buffer.clear();
        } while (readFromHost.ready());
    }
}

package eu.arrowhead.core.mscv.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Set;
import javax.annotation.PostConstruct;

import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.dto.shared.mscv.PublicKeyResponse;
import eu.arrowhead.core.mscv.MscvDefaults;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class KeyPairFileStorage {

    private final static Set<PosixFilePermission> RWX_DIRECTORY = PosixFilePermissions.fromString("rwx------");
    private final static Set<PosixFilePermission> RW_FILES = PosixFilePermissions.fromString("rw-------");

    private final Logger logger = LogManager.getLogger();
    private final KeyPairFileUtilities keyPairHandler;
    private final MscvDefaults.SshDefaults sshDefaults;
    private final CoreSystemRegistrationProperties coreProperties;

    // properties can't be immutable yet, so this can't be immutable either
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Autowired
    public KeyPairFileStorage(final KeyPairFileUtilities keyPairHandler,
                              final MscvDefaults mscvDefaults,
                              final CoreSystemRegistrationProperties coreProperties) {
        this.keyPairHandler = keyPairHandler;
        this.sshDefaults = mscvDefaults.getSsh();
        this.coreProperties = coreProperties;
    }

    @PostConstruct
    public void loadKeys()
            throws PKCSException, OperatorCreationException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        Assert.notNull(keyPairHandler, "KeyPairHandler must not be null");
        Assert.notNull(sshDefaults, "SshDefaults must not be null");

        Assert.notNull(sshDefaults.getPrivateKeyFile(), "Property private-key-file must not be null");
        Assert.notNull(sshDefaults.getPublicKeyFile(), "Property public-key-file must not be null");

        final File publicKeyPath = new File(sshDefaults.getPublicKeyFile());
        final File privateKeyPath = new File(sshDefaults.getPrivateKeyFile());
        final String password = sshDefaults.isEmptyPassword() ? null : sshDefaults.getPrivateKeyPassword();

        if (sshDefaults.hasKeyPairFile()) {
            logger.info("Loading KeyPair file from {}", sshDefaults.getKeyPairFile());
            final File keyPairFile = new File(sshDefaults.getKeyPairFile());
            final KeyPair keyPair = keyPairHandler.readKeyPair(keyPairFile.getAbsolutePath(), password);
            setAndWriteKeyPair(keyPair, publicKeyPath, privateKeyPath, password);
        } else if (sshDefaults.hasPrivateKeyFile() && sshDefaults.hasPublicKeyFile()) {
            logger.info("Loading private and public key from existing files");
            this.privateKey = keyPairHandler.readPrivateKey(privateKeyPath.getAbsolutePath(), password);
            this.publicKey = keyPairHandler.readPublicKey(publicKeyPath.getAbsolutePath());
        } else {
            logger.info("Creating new KeyPair !");
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);

            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            setAndWriteKeyPair(keyPair, publicKeyPath, privateKeyPath, password);
        }
    }

    public PublicKeyResponse createPublicKeyResponse() throws IOException, InvalidKeySpecException {
        return new PublicKeyResponse(getPublicKeyAsBase64(), getPublicKeyAsSshIdentityString(), getPublicKeyAlgorithm(), getPublicKeyFormat());
    }

    public String getPublicKeyAsSshIdentityString() throws IOException, InvalidKeySpecException {
        return keyPairHandler.encodePublicKeySSH(publicKey, "mscv@" + coreProperties.getCoreSystemDomainName());
    }

    public String getPublicKeyAsBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getPublicKeyAlgorithm() {
        return publicKey.getAlgorithm();
    }

    public String getPublicKeyFormat() {
        return publicKey.getFormat();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    protected KeyPair getKeyPair() {
        return new KeyPair(publicKey, privateKey);
    }

    private void setAndWriteKeyPair(final KeyPair keyPair, final File publicKeyPath, final File privateKeyPath, final String password)
            throws IOException, InvalidKeySpecException {
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();

        if (!publicKeyPath.exists()) {
            createFiles(publicKeyPath);
        }

        if (!privateKeyPath.exists()) {
            createFiles(privateKeyPath);
        }

        logger.info("Writing public and private keys");
        keyPairHandler.writePublicKeySSH(publicKey, publicKeyPath.getAbsolutePath(), "MSCV@" + getClass().getSimpleName());
        keyPairHandler.writePrivateKeySSH(privateKey, privateKeyPath.getAbsolutePath(), password);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createFiles(final File file) throws IOException {
        final Path path = file.toPath().normalize();
        logger.info("Creating paths for file: {}",  path);
        if (SystemUtils.IS_OS_WINDOWS) {
            final File parent = Files.createDirectories(path.getParent()).toFile();
            parent.setWritable(true, true);
            parent.setReadable(true, true);
            parent.setExecutable(true, true);

            final File newFile = Files.createFile(path).toFile();
            newFile.setWritable(true, true);
            newFile.setReadable(true, true);
            newFile.setExecutable(false, false);
        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) {
            Files.createDirectories(path.getParent(), PosixFilePermissions.asFileAttribute(RWX_DIRECTORY));
            Files.createFile(path, PosixFilePermissions.asFileAttribute(RW_FILES));
        } else {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
    }
}

package eu.arrowhead.core.mscv.security;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.lang3.SystemUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class KeyPairFileUtilitiesTest {

    private static final String PASSWORD = "123456";
    private static final String KEYPAIR_PEM = "classpath:keys/mscv-keypair.pem"; // private key + certificate. "BEGIN ENCRYPTED PRIVATE KEY" and "BEGIN CERTIFICATE"
    private static final String PRIVATE_KEY = "classpath:keys/mscv-private.key"; // key in encrypted pem format. "BEGIN RSA PRIVATE KEY"
    private static final String PRIVATE_PKCS8 = "classpath:keys/mscv-private.pkcs8"; // key in encrypted pkcs8 format. "BEGIN ENCRYPTED PRIVATE KEY"
    private static final String PRIVATE_PLAIN_KEY = "classpath:keys/mscv-plain-private.key"; // key in pem format private key. "BEGIN RSA PRIVATE KEY"
    private static final String PUBLIC_PEM = "classpath:keys/mscv-public.pem"; // key in pem format. "BEGIN PUBLIC KEY".

    private final KeyPairFileUtilities handler = new KeyPairFileUtilities();
    private final ResourceLoader resourceLoader = new DefaultResourceLoader(KeyPairFileUtilities.class.getClassLoader());

    private PrivateKey plainPrivateKey;
    private PublicKey plainPublicKey;

    @Before
    public void initKeys()
            throws IOException, PKCSException, OperatorCreationException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        plainPrivateKey = handler.readPrivateKey(findResource(PRIVATE_PLAIN_KEY).getAbsolutePath(), null);
        plainPublicKey = handler.readPublicKey(findResource(PUBLIC_PEM).getAbsolutePath());
    }

    @Test
    public void readPrivateKey_encrypted_pem() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(PRIVATE_KEY);
    }

    @Test
    public void readPrivateKey_pem() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(KEYPAIR_PEM);
    }

    @Test
    public void readPrivateKey_pkcs8() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(PRIVATE_PKCS8);
    }

    @Test
    public void readKeyPair_pem() throws IOException, OperatorCreationException, PKCSException {
        readKeyPairAndVerify(KEYPAIR_PEM);
    }

    @Test
    public void readPublicKey_pem() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        readPublicKeyAndVerify(PUBLIC_PEM);
    }

    @Test
    public void writePublicKey_ssh_pub() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        final File file = File.createTempFile("public", ".pem.tmp");
        try {
            handler.writePublicKeySSH(plainPublicKey, file.getAbsolutePath(), "MSCV@" + SystemUtils.getHostName());
            readPublicKeyAndVerify(file);
        } finally {
            Assert.assertTrue(file.delete());
        }
    }

    @Test
    public void writePrivateKey_plain_pem() throws IOException, PKCSException, OperatorCreationException {
        final File file = File.createTempFile("public", ".pem.tmp");
        try {
            handler.writePrivateKeyPlain(plainPrivateKey, file.getAbsolutePath());
            readPrivateKeyAndVerify(file, null);

            // implementation detects unencrypted file and parses it w/o password
            readPrivateKeyAndVerify(file, PASSWORD);
        } finally {
            Assert.assertTrue(file.delete());
        }
    }

    @Test
    public void writePrivateKey_encrypted_pem() throws IOException, PKCSException, OperatorCreationException {
        final File file = File.createTempFile("public", ".pem.tmp");
        try {
            handler.writePrivateKeyPEM(plainPrivateKey, file.getAbsolutePath(), "new password");
            readPrivateKeyAndVerify(file, "new password");

            try {
                readPrivateKeyAndVerify(file, PASSWORD);
                Assert.fail("The private key should have a different password ...");
            } catch (final Exception e) {
                // this is good and expected
            }
        } finally {
            Assert.assertTrue(file.delete());
        }
    }

    @Test
    public void writePrivateKey_encrypted_PKCS8() throws IOException, OperatorCreationException, PKCSException {
        final File file = File.createTempFile("public", ".key.tmp");
        try {
            handler.writePrivateKeyPKCS8(plainPrivateKey, file.getAbsolutePath(), "new password");
            readPrivateKeyAndVerify(file, "new password");

            try {
                readPrivateKeyAndVerify(file, PASSWORD);
                Assert.fail("The private key should have a different password ...");
            } catch (final Exception e) {
                // this is good and expected
            }
        } finally {
            Assert.assertTrue(file.delete());
        }
    }

    @Test
    public void writePublicKey_pub() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        final File file = File.createTempFile("public", ".pub.tmp");
        try {
            handler.writePublicKeyPEM(plainPublicKey, file.getAbsolutePath());
            readPublicKeyAndVerify(file);
        } finally {
            Assert.assertTrue(file.delete());
        }
    }

    private File findResource(final String resourceLocation) throws IOException {
        final Resource resource = resourceLoader.getResource(resourceLocation);
        return resource.getFile().getAbsoluteFile();
    }

    private void readPrivateKeyAndVerify(final File file, final String password) throws PKCSException, OperatorCreationException, IOException {
        final PrivateKey privateKey = handler.readPrivateKey(file.getAbsolutePath(), password);
        Assert.assertNotNull(privateKey);
        Assert.assertArrayEquals(plainPrivateKey.getEncoded(), privateKey.getEncoded());
    }

    private void readPrivateKeyAndVerify(final String filename) throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(findResource(filename), PASSWORD);
    }

    private void readPublicKeyAndVerify(final File file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        final PublicKey publicKey = handler.readPublicKey(file.getAbsolutePath());
        Assert.assertNotNull(publicKey);
        Assert.assertArrayEquals(plainPublicKey.getEncoded(), publicKey.getEncoded());
    }

    private void readPublicKeyAndVerify(final String filename) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        readPublicKeyAndVerify(findResource(filename));
    }

    private void readKeyPairAndVerify(final String filename) throws IOException, PKCSException, OperatorCreationException {
        final KeyPair keyPair = handler.readKeyPair(findResource(filename).getAbsolutePath(), PASSWORD);
        Assert.assertNotNull(keyPair);
        Assert.assertNotNull(keyPair.getPrivate());
        Assert.assertArrayEquals(plainPrivateKey.getEncoded(), keyPair.getPrivate().getEncoded());
        Assert.assertNotNull(keyPair.getPublic());
        Assert.assertArrayEquals(plainPublicKey.getEncoded(), keyPair.getPublic().getEncoded());
    }
}
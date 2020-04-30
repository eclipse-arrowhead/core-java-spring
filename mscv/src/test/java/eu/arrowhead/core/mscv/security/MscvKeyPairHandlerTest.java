package eu.arrowhead.core.mscv.security;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class MscvKeyPairHandlerTest {

    private static final String PASSWORD = "123456";
    private static final String KEYPAIR_P12 = "classpath:keys/mscv-keypair.p12"; // certificate key store
    private static final String KEYPAIR_PEM = "classpath:keys/mscv-keypair.pem"; // private key + certificate
    private static final String PRIVATE_KEY = "classpath:keys/mscv-private.key"; // key in encrypted pem format
    private static final String PRIVATE_PKCS8 = "classpath:keys/mscv-private.pkcs8"; // key in encrypted pkcs8 format
    private static final String PRIVATE_PVK = "classpath:keys/mscv-private.pvk"; // windows proprietary format
    private static final String PRIVATE_PLAIN_KEY = "classpath:keys/mscv-private-plain.key"; // key in pem format
    private static final String PUBLIC_PLAIN_PUB = "classpath:keys/mscv-public.pub"; // key in pem format

    private final MscvKeyPairHandler handler = new MscvKeyPairHandler();
    private final ResourceLoader resourceLoader = new DefaultResourceLoader(MscvKeyPairHandler.class.getClassLoader());

    private PrivateKey plainPrivateKey;
    private PublicKey plainPublicKey;

    @Before
    public void initKeys() throws IOException, PKCSException, OperatorCreationException {
        plainPrivateKey = handler.readPrivateKey(findResource(PRIVATE_PLAIN_KEY).getAbsolutePath(), null);
        plainPublicKey = handler.readPublicKey(findResource(PUBLIC_PLAIN_PUB).getAbsolutePath());
    }

    @Test
    public void readPrivateKey_key() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(PRIVATE_KEY);
    }

    @Test
    public void readPrivateKey_pem() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(KEYPAIR_PEM);
    }

    @Test
    @Ignore
    public void readPrivateKey_p12() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(KEYPAIR_P12);
    }

    @Test
    public void readPrivateKey_pkcs8() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(PRIVATE_PKCS8);
    }

    @Test
    @Ignore
    public void readPrivateKey_pvk() throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(PRIVATE_PVK);
    }

    @Test
    public void readKeyPair_pem() throws IOException, OperatorCreationException, PKCSException {
        readKeyPairAndVerify(KEYPAIR_PEM);
    }

    @Test
    @Ignore
    public void readKeyPair_p12() throws IOException, OperatorCreationException, PKCSException {
        readKeyPairAndVerify(KEYPAIR_P12);
    }

    @Test
    @Ignore
    public void readPublicKey_p12() throws IOException {
        readPublicKeyAndVerify(KEYPAIR_P12);
    }

    @Test
    public void readPublicKey_pub() throws IOException {
        readPublicKeyAndVerify(PUBLIC_PLAIN_PUB);
    }

    @Test
    public void writePrivateKeyPEM() throws IOException, PKCSException, OperatorCreationException {
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
    public void writePrivateKeyPKCS8() throws IOException, OperatorCreationException, PKCSException {
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
    public void writePublicKey_pub() throws IOException, OperatorCreationException, PKCSException {
        final File file = File.createTempFile("public", ".pub.tmp");
        try {
            handler.writePublicKey(plainPublicKey, file.getAbsolutePath());
            readPublicKeyAndVerify(file);
        } finally {
            Assert.assertTrue(file.delete());
        }
    }

    private File findResource(final String resourceLocation) throws IOException {
        final Resource resource = resourceLoader.getResource(resourceLocation);
        return resource.getFile();
    }

    private void readPrivateKeyAndVerify(final File file, final String password) throws PKCSException, OperatorCreationException, IOException {
        final PrivateKey privateKey = handler.readPrivateKey(file.getAbsolutePath(), password);
        Assert.assertNotNull(privateKey);
        Assert.assertArrayEquals(plainPrivateKey.getEncoded(), privateKey.getEncoded());
    }

    private void readPrivateKeyAndVerify(final String filename) throws PKCSException, OperatorCreationException, IOException {
        readPrivateKeyAndVerify(findResource(filename), PASSWORD);
    }

    private void readPublicKeyAndVerify(final File file) throws IOException {
        final PublicKey publicKey = handler.readPublicKey(file.getAbsolutePath());
        Assert.assertNotNull(publicKey);
        Assert.assertArrayEquals(plainPublicKey.getEncoded(), publicKey.getEncoded());
    }

    private void readPublicKeyAndVerify(final String filename) throws IOException {
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
package eu.arrowhead.core.mscv.security;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.springframework.lang.Nullable;

/* PVK and p12 not supported currently
 * see https://github.com/ebourg/jsign/blob/master/jsign-core/src/main/java/net/jsign/PVK.java for PVK.
 * p12 is a regular KeyStore.. see onboarding client.
 */
public class MscvKeyPairHandler {

    private static final String DEFAULT_ASN_ALGORITHM_IDENTIFIER = JceOpenSSLPKCS8EncryptorBuilder.AES_128_CBC;
    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES-128-CBC";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Logger logger = LogManager.getLogger();
    private final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

    public MscvKeyPairHandler() { super(); }

    /* best effort */
    public PrivateKey readPrivateKey(final String file, @Nullable final String password) throws IOException, OperatorCreationException, PKCSException {

        final char[] passwd = Objects.nonNull(password) ? password.toCharArray() : new char[0];

        final Object object = parsePEM(file);
        final PrivateKey privateKey;

        if (Objects.isNull(object)) {
            throw new IOException("Unable to find private key in file: " + file);
        } else if (object instanceof PEMEncryptedKeyPair) {
            privateKey = convertKeyPairPEM((PEMEncryptedKeyPair) object, passwd).getPrivate();
        } else if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
            privateKey = convertPrivateKeyPKCS8((PKCS8EncryptedPrivateKeyInfo) object, passwd);
        } else if (object instanceof PEMKeyPair) {
            privateKey = convertKeyPairPlain((PEMKeyPair) object).getPrivate();
        } else {
            logger.error("Unhandled object class: {}", object.getClass().getCanonicalName());
            throw new IOException("Unable to decode object: " + object.getClass().getCanonicalName());
        }

        return privateKey;
    }

    /* best effort */
    public KeyPair readKeyPair(final String file, @Nullable final String password) throws IOException, PKCSException, OperatorCreationException {

        final char[] passwd = Objects.nonNull(password) ? password.toCharArray() : new char[0];
        final KeyPair keyPair;

        try (final FileReader reader = new FileReader(file)) {
            final PEMParser pemParser = new PEMParser(reader);
            final Object object = pemParser.readObject();

            if (Objects.isNull(object)) {
                throw new IOException("Unable to find key pair in file: " + file);
            } else if (object instanceof PEMEncryptedKeyPair) {
                keyPair = convertKeyPairPEM((PEMEncryptedKeyPair) object, passwd);
            } else if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
                final PrivateKey privateKey = convertPrivateKeyPKCS8((PKCS8EncryptedPrivateKeyInfo) object, passwd);
                final PublicKey publicKey = parseUnknownObject(pemParser.readObject(), file);
                keyPair = new KeyPair(publicKey, privateKey);
            } else if (object instanceof PEMKeyPair) {
                keyPair = convertKeyPairPlain((PEMKeyPair) object);
            } else {
                logger.error("Unhandled object class: {}", object.getClass().getCanonicalName());
                throw new IOException("Unable to decode object: " + object.getClass().getCanonicalName());
            }
        }

        return keyPair;
    }

    /* best effort */
    public PublicKey readPublicKey(final String file) throws IOException {

        final Object object = parsePEM(file);
        return parseUnknownObject(object, file);
    }

    /* PEM encrypted */
    public void writePrivateKeyPEM(final PrivateKey privateKey, final String file, @Nullable final String password) throws IOException {

        final char[] passwd = Objects.nonNull(password) ? password.toCharArray() : new char[0];
        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);
            final PEMEncryptor encryptor = new JcePEMEncryptorBuilder(DEFAULT_ENCRYPTION_ALGORITHM)
                    .setProvider("BC")
                    .build(passwd);

            final PemObjectGenerator pemObjectGenerator = new JcaMiscPEMGenerator(privateKey, encryptor);
            pWrt.writeObject(pemObjectGenerator);
            pWrt.flush();
        }
    }

    /* PKCS8 encrypted */
    public void writePrivateKeyPKCS8(final PrivateKey privateKey, final String file, @Nullable final String password)
            throws IOException, OperatorCreationException {

        final char[] passwd = Objects.nonNull(password) ? password.toCharArray() : new char[0];
        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);
            final ASN1ObjectIdentifier algorithm = new ASN1ObjectIdentifier(DEFAULT_ASN_ALGORITHM_IDENTIFIER);
            final OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(algorithm)
                    .setProvider("BC")
                    .setPasssword(passwd)
                    .build();

            final PemObjectGenerator pemObjectGenerator = new JcaPKCS8Generator(privateKey, encryptor);
            pWrt.writeObject(pemObjectGenerator);
            pWrt.flush();
        }
    }

    /* plain PEM */
    public void writePublicKey(final PublicKey publicKey, final String file)
            throws IOException, OperatorCreationException {

        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);
            pWrt.writeObject(publicKey);
            pWrt.flush();
        }
    }

    private PublicKey parseUnknownObject(final Object object, final String file) throws IOException {
        final PublicKey publicKey;
        if (Objects.isNull(object)) {
            throw new IOException("Unable to find public key in file: " + file);
        } else if (object instanceof PEMKeyPair) {
            publicKey = convertKeyPairPlain((PEMKeyPair) object).getPublic();
        } else if (object instanceof SubjectPublicKeyInfo) {
            publicKey = convertPublicKeyInfo((SubjectPublicKeyInfo) object);
        } else if (object instanceof X509CertificateHolder) {
            final SubjectPublicKeyInfo subjectPublicKeyInfo = ((X509CertificateHolder) object).getSubjectPublicKeyInfo();
            publicKey = convertPublicKeyInfo(subjectPublicKeyInfo);
        } else {
            logger.error("Unhandled object class: {}", object.getClass().getCanonicalName());
            throw new IOException("Unable to decode object: " + object.getClass().getCanonicalName());
        }
        return publicKey;
    }

    private Object parsePEM(final String file) throws IOException {
        try (final FileReader reader = new FileReader(file)) {
            final PEMParser pemParser = new PEMParser(reader);
            return pemParser.readObject();
        }
    }

    /* plain test */
    private KeyPair convertKeyPairPlain(final PEMKeyPair keyPair) throws PEMException {
        return converter.getKeyPair(keyPair);
    }

    /* plain test */
    private PublicKey convertPublicKeyInfo(final SubjectPublicKeyInfo publicKeyInfo) throws PEMException {
        return converter.getPublicKey(publicKeyInfo);
    }

    /* PEM encrypted */
    private KeyPair convertKeyPairPEM(final PEMEncryptedKeyPair keyPair, final char[] password) throws IOException {
        // Encrypted key - we will use provided password
        final PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password);
        return converter.getKeyPair(keyPair.decryptKeyPair(decProv));
    }

    /* PKCS8 encrypted */
    private PrivateKey convertPrivateKeyPKCS8(final PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo, final char[] password)
            throws PKCSException, OperatorCreationException, IOException {
        // Encrypted key - we will use provided password
        final InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
        final PrivateKeyInfo privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(decProv);
        return converter.getPrivateKey(privateKeyInfo);
    }
}

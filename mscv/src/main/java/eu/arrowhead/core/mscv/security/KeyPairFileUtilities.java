package eu.arrowhead.core.mscv.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/* PVK and p12 not supported currently
 * see https://github.com/ebourg/jsign/blob/master/jsign-core/src/main/java/net/jsign/PVK.java for PVK.
 * p12 is a regular KeyStore.. see onboarding client.
 */
@Component
public class KeyPairFileUtilities {

    private static final String DEFAULT_ASN_ALGORITHM_IDENTIFIER = JceOpenSSLPKCS8EncryptorBuilder.AES_128_CBC;
    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES-128-CBC";
    private static final String OPENSSH_RSA_TYPE = "ssh-rsa";
    private static final String OPENSSH_DSA_TYPE = "ssh-dss";
    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_DSA = "DSA";
    private static final String PROVIDER_BC = "BC";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final Logger logger = LogManager.getLogger();
    private final JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter().setProvider(PROVIDER_BC);

    public KeyPairFileUtilities() { super(); }

    public PrivateKey readPrivateKeySSH(final String file) throws IOException, PKCSException, OperatorCreationException {
        return readPrivateKey(file.trim(), null);
    }

    /* best effort */
    public PrivateKey readPrivateKey(final String file, @Nullable final String password) throws IOException, OperatorCreationException, PKCSException {

        final char[] passwd = Objects.nonNull(password) ? password.toCharArray() : null;

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

        final char[] passwd = Objects.nonNull(password) ? password.toCharArray() : null;
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
    public PublicKey readPublicKey(final String file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        final Object object = parsePEM(file);
        if (Objects.nonNull(object)) { return parseUnknownObject(object, file); } else { return readOpenSshPublicKey(file); }
    }

    /* SSH private key */
    public void writePrivateKeySSH(final PrivateKey privateKey, final String file) throws IOException {
        writePrivateKeyPlain(privateKey, file);
    }

    /* SSH private key */
    public void writePrivateKeySSH(final PrivateKey privateKey, final String file, final String password) throws IOException {
        writePrivateKeyPEM(privateKey, file, password);
    }

    /* PEM plain */
    public void writePrivateKeyPlain(final PrivateKey privateKey, final String file) throws IOException {

        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);

            final PemObjectGenerator pemObjectGenerator = new JcaMiscPEMGenerator(privateKey);
            pWrt.writeObject(pemObjectGenerator);
            pWrt.flush();
        }
    }

    /* PEM encrypted */
    public void writePrivateKeyPEM(final PrivateKey privateKey, final String file, final String password) throws IOException {

        Assert.hasText(password, "Password must not be null");
        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);
            final PEMEncryptor encryptor = new JcePEMEncryptorBuilder(DEFAULT_ENCRYPTION_ALGORITHM)
                    .setProvider(PROVIDER_BC)
                    .build(password.toCharArray());

            final PemObjectGenerator pemObjectGenerator = new JcaMiscPEMGenerator(privateKey, encryptor);
            pWrt.writeObject(pemObjectGenerator);
            pWrt.flush();
        }
    }

    /* PKCS8 encrypted */
    public void writePrivateKeyPKCS8(final PrivateKey privateKey, final String file, final String password)
            throws IOException, OperatorCreationException {

        Assert.hasText(password, "Password must not be null");
        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);
            final ASN1ObjectIdentifier algorithm = new ASN1ObjectIdentifier(DEFAULT_ASN_ALGORITHM_IDENTIFIER);
            final OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(algorithm)
                    .setProvider(PROVIDER_BC)
                    .setPasssword(password.toCharArray())
                    .build();

            final PemObjectGenerator pemObjectGenerator = new JcaPKCS8Generator(privateKey, encryptor);
            pWrt.writeObject(pemObjectGenerator);
            pWrt.flush();
        }
    }

    /* plain PEM */
    public void writePublicKeyPEM(final PublicKey publicKey, final String file)
            throws IOException {

        try (final FileWriter writer = new FileWriter(file)) {
            final JcaPEMWriter pWrt = new JcaPEMWriter(writer);
            pWrt.writeObject(publicKey);
            pWrt.flush();
        }
    }

    public String encodePublicKeySSH(final PublicKey publicKey, final String user) throws IOException, InvalidKeySpecException {
        final String encodedKey;
        if (publicKey.getAlgorithm().equals(ALGORITHM_RSA)) {
            encodedKey = encodeRsaPublicKey((RSAPublicKey) publicKey, user);
        } else if (publicKey.getAlgorithm().equals(ALGORITHM_DSA)) {
            encodedKey = encodeDsaPublicKey((DSAPublicKey) publicKey, user);
        } else {
            throw new InvalidKeySpecException("Unknown public key encoding: " + publicKey.getAlgorithm());
        }
        return encodedKey;
    }

    public void writePublicKeySSH(final PublicKey publicKey, final String file, final String user)
            throws IOException, InvalidKeySpecException {

        try (final FileWriter writer = new FileWriter(file)) {
            writer.write(encodePublicKeySSH(publicKey, user));
            writer.flush();
        }
    }

    private String encodeRsaPublicKey(final RSAPublicKey key, final String user) throws IOException {
        final ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(byteOs);
        dos.writeInt(OPENSSH_RSA_TYPE.getBytes().length);
        dos.write(OPENSSH_RSA_TYPE.getBytes());
        dos.writeInt(key.getPublicExponent().toByteArray().length);
        dos.write(key.getPublicExponent().toByteArray());
        dos.writeInt(key.getModulus().toByteArray().length);
        dos.write(key.getModulus().toByteArray());
        final String encodedKey = Base64.getEncoder().encodeToString(byteOs.toByteArray());
        return OPENSSH_RSA_TYPE + " " + encodedKey + " " + user;
    }

    private String encodeDsaPublicKey(final DSAPublicKey key, final String user) throws IOException {
        final DSAParams params = key.getParams();
        final ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(byteOs);
        dos.writeInt(OPENSSH_DSA_TYPE.getBytes().length);
        dos.write(OPENSSH_DSA_TYPE.getBytes());
        dos.writeInt(params.getP().toByteArray().length);
        dos.write(params.getP().toByteArray());
        dos.writeInt(params.getQ().toByteArray().length);
        dos.write(params.getQ().toByteArray());
        dos.writeInt(params.getG().toByteArray().length);
        dos.write(params.getG().toByteArray());
        dos.writeInt(key.getY().toByteArray().length);
        dos.write(key.getY().toByteArray());
        final String encodedKey = Base64.getEncoder().encodeToString(byteOs.toByteArray());
        return OPENSSH_DSA_TYPE + " " + encodedKey + " " + user;
    }

    // can't find out how to do this with bouncy castle. OpenSSHPublicKeyUtil doesn't work
    private PublicKey readOpenSshPublicKey(final String file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            // SSH keys only have a single line
            final String line = reader.readLine();
            final String[] split = line.split(" ", 3);

            if (split.length < 2) {
                throw new InvalidKeySpecException("The file does not contain a single valid SSH public key");
            }

            final String type = split[0];
            final String base64Data = split[1];
            final byte[] bytes = Base64.getDecoder().decode(base64Data);

            try (final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {

                byte[] header = readElement(dis);
                final String pubKeyFormat = new String(header);
                if (!pubKeyFormat.equals(type)) {
                    throw new InvalidKeySpecException("Header type '" + type + "' does not match encoded format '" + pubKeyFormat + "'");
                }

                switch (type) {
                    case OPENSSH_RSA_TYPE:
                        byte[] publicExponent = readElement(dis);
                        byte[] modulus = readElement(dis);

                        final KeySpec rsaKeySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
                        final KeyFactory rsaKeyFactory = KeyFactory.getInstance(ALGORITHM_RSA, PROVIDER_BC);
                        return rsaKeyFactory.generatePublic(rsaKeySpec);
                    case OPENSSH_DSA_TYPE:
                        byte[] p = readElement(dis);
                        byte[] q = readElement(dis);
                        byte[] g = readElement(dis);
                        byte[] y = readElement(dis);
                        final KeySpec dssKeySpec = new DSAPublicKeySpec(new BigInteger(p), new BigInteger(q), new BigInteger(g), new BigInteger(y));
                        final KeyFactory dssKeyFactory = KeyFactory.getInstance(ALGORITHM_DSA, PROVIDER_BC);
                        return dssKeyFactory.generatePublic(dssKeySpec);
                    default:
                        throw new InvalidKeySpecException("Unsupported format: " + type);
                }
            }
        }
    }

    private byte[] readElement(DataInput dis) throws IOException {
        int len = dis.readInt();
        byte[] buf = new byte[len];
        dis.readFully(buf);
        return buf;
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
        return pemKeyConverter.getKeyPair(keyPair);
    }

    /* plain test */
    private PublicKey convertPublicKeyInfo(final SubjectPublicKeyInfo publicKeyInfo) throws PEMException {
        return pemKeyConverter.getPublicKey(publicKeyInfo);
    }

    /* PEM encrypted */
    private KeyPair convertKeyPairPEM(final PEMEncryptedKeyPair keyPair, final char[] password) throws IOException {
        // Encrypted key - we will use provided password
        final PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password);
        return pemKeyConverter.getKeyPair(keyPair.decryptKeyPair(decProv));
    }

    /* PKCS8 encrypted */
    private PrivateKey convertPrivateKeyPKCS8(final PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo, final char[] password)
            throws PKCSException, OperatorCreationException, IOException {
        // Encrypted key - we will use provided password
        final InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
        final PrivateKeyInfo privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(decProv);
        return pemKeyConverter.getPrivateKey(privateKeyInfo);
    }
}

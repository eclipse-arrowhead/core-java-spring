package eu.arrowhead.common;

import eu.arrowhead.common.dto.internal.CertificateType;
import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNamesBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Component
public class SecurityUtilities {

    //=================================================================================================
    // members
    private final static String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    private final Logger logger = LogManager.getLogger(SecurityUtilities.class);

    private final KeyFactory keyFactory;
    private final KeyPairGenerator keyPairGenerator;
    private final SSLProperties sslProperties;

    //=================================================================================================
    // constructors
    @Autowired
    public SecurityUtilities(@Value("${security.key.algorithm:RSA}") final String keyFactoryAlgorithm,
                             @Value("${security.key.size:2048}") final Integer keySize,
                             final SSLProperties sslProperties) throws NoSuchAlgorithmException {
        this.sslProperties = sslProperties;
        keyFactory = KeyFactory.getInstance(keyFactoryAlgorithm);
        keyPairGenerator = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
        keyPairGenerator.initialize(keySize);
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static String getCertificateCNFromRequest(final HttpServletRequest request) {
        final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
        if (certificates != null && certificates.length != 0) {
            final X509Certificate cert = certificates[0];
            return Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
        }

        return null;
    }

    //-------------------------------------------------------------------------------------------------
    public String createEncodedCSR(final String baseCommonName, final KeyPair keyPair, final String host, final String address, final CertificateType type)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, OperatorCreationException {

        Assert.notNull(sslProperties.getKeyStore(), "KeyStore property must not be null!");
        Assert.hasText(sslProperties.getKeyStorePassword(), "KeyStore password property must not be null or empty!");

        logger.debug("Preparing Certificate Signing Request ...");
        // get cloud certificate for cloudName and operator
        final KeyStore keyStore = KeyStore.getInstance(sslProperties.getKeyStore().getFile(), sslProperties.getKeyStorePassword().toCharArray());
        final X509Certificate cloudCertFromKeyStore = Utilities.getCloudCertFromKeyStore(keyStore);

        // "CN=<commonName>.<cloudName>.<operator>.arrowhead.eu, OU=<operator>, O=arrowhead, C=eu"
        final String cloudName = getCloudName(cloudCertFromKeyStore.getSubjectDN());
        //final String operator = getOperatorName(cloudCertFromKeyStore.getSubjectDN());
        //final String organization = getOrganization(cloudCertFromKeyStore.getSubjectDN());
        //final String country = getCountry(cloudCertFromKeyStore.getSubjectDN());
        //final String commonName = String.format("CN=%s.%s, OU=%s, O=%s, C=%s",
        //                                        baseCommonName, cloudName, operator, organization, country);
        final String commonName = String.format("CN=%s.%s.%s", baseCommonName, type.getCommonNamePart(), cloudName);
        final X500Name x500Name = new X500Name(commonName);

        logger.debug("Building and Signing Certificate Signing Request for {}", x500Name);
        // create certificate signing request
        final JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500Name, keyPair.getPublic());
        final GeneralNamesBuilder namesBuilder = new GeneralNamesBuilder();

        namesBuilder.addName(new GeneralName(GeneralName.dNSName, "localhost"));
        namesBuilder.addName(new GeneralName(GeneralName.dNSName, baseCommonName));
        namesBuilder.addName(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));

        if (Objects.nonNull(host)) {
            namesBuilder.addName(new GeneralName(GeneralName.dNSName, host));
        }
        if (Objects.nonNull(host)) {
            namesBuilder.addName(new GeneralName(GeneralName.iPAddress, address));
        }

        final Extension extAlternativeNames = new Extension(Extension.subjectAlternativeName, false, new DEROctetString(namesBuilder.build()));
        builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, new Extensions(extAlternativeNames));

        final JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM);
        final ContentSigner contentSigner = contentSignerBuilder.build(keyPair.getPrivate());
        final PKCS10CertificationRequest csr = builder.build(contentSigner);

        return Base64.getEncoder().encodeToString(csr.getEncoded());
    }

    //-------------------------------------------------------------------------------------------------
    private KeyPair generateKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }

    //-------------------------------------------------------------------------------------------------
    private KeyPair getKeyPairFromBase64EncodedStrings(final String encodedPublicKey,
                                                      final String encodedPrivateKey) {
        final PublicKey publicKey = getPublicKeyFromBase64EncodedString(encodedPublicKey);
        final PrivateKey privateKey = getPrivateKeyFromBase64EncodedString(encodedPrivateKey);
        return new KeyPair(publicKey, privateKey);
    }

    //-------------------------------------------------------------------------------------------------
    public KeyPair extractKeyPair(final CertificateCreationRequestDTO creationRequestDTO) {
        final String commonName = creationRequestDTO.getCommonName();
        final String encodedPublicKey = creationRequestDTO.getPublicKey();
        final String encodedPrivateKey = creationRequestDTO.getPrivateKey();

        Assert.isTrue(Utilities.notEmpty(commonName), "CommonName is null or blank");
        final KeyPair keyPair;

        if (Objects.nonNull(encodedPrivateKey) && Objects.nonNull(encodedPublicKey)) {
            keyPair = getKeyPairFromBase64EncodedStrings(encodedPublicKey, encodedPrivateKey);
        }
        else {
            keyPair = generateKeyPair();
        }

        return keyPair;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private PublicKey getPublicKeyFromBase64EncodedString(final String encodedKey) {
        Assert.isTrue(Utilities.notEmpty(encodedKey), "Encoded key is null or blank");

        final byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return generatePublicKeyFromByteArray(keyBytes);
    }

    //-------------------------------------------------------------------------------------------------
    private PrivateKey getPrivateKeyFromBase64EncodedString(final String encodedKey) {
        Assert.isTrue(Utilities.notEmpty(encodedKey), "Encoded key is null or blank");

        final byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return generatePrivateKeyFromByteArray(keyBytes);
    }

    //-------------------------------------------------------------------------------------------------
    private String getCloudName(final Principal principal) {
        return Utilities.getCertCNFromSubject(principal.getName());
    }

    //-------------------------------------------------------------------------------------------------
    private String getOperatorName(final Principal principal) {
        return extractCNPart(principal, 2);
    }

    //-------------------------------------------------------------------------------------------------
    private String getOrganization(final Principal principal) {
        return extractCNPart(principal, 1);
    }

    //-------------------------------------------------------------------------------------------------
    private String getCountry(final Principal principal) {
        return extractCNPart(principal, 0);
    }

    //-------------------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------------------
    private String extractCNPart(final Principal principal, final int tailIndex) {
        Assert.notNull(principal, "Principal must not be null");
        final String fullCN = principal.getName();
        Assert.hasText(fullCN, "Empty common name is not allowed");

        final String[] strings = fullCN.split("\\.");

        if (tailIndex >= strings.length) {
            throw new IllegalArgumentException("Internal error: Unable to extract information from cloud certificate");
        }

        return strings[strings.length - (tailIndex + 1)];
    }

    //-------------------------------------------------------------------------------------------------
    private PublicKey generatePublicKeyFromByteArray(final byte[] keyBytes) {
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (final InvalidKeySpecException ex) {
            logger.error("getPublicKey: X509 keySpec could not be created from the decoded bytes.");
            throw new AuthException("Public key decoding failed due wrong input key", ex);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private PrivateKey generatePrivateKeyFromByteArray(final byte[] keyBytes) {
        try {
            return keyFactory.generatePrivate(new X509EncodedKeySpec(keyBytes));
        } catch (final InvalidKeySpecException ex) {
            logger.error("getPublicKey: X509 keySpec could not be created from the decoded bytes.");
            throw new AuthException("Public key decoding failed due wrong input key", ex);
        }
    }
}

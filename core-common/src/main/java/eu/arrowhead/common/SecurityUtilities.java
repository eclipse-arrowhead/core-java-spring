/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common;

import java.io.ByteArrayInputStream;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import javax.servlet.http.HttpServletRequest;

import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.KeyPairDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import org.apache.http.HttpStatus;
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
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.http.server.ServletServerHttpRequest;

@Component
public class SecurityUtilities {

    //=================================================================================================
    // members
	
    private final static String SIGNATURE_ALGORITHM = "SHA256WithRSA";
    private static final String X509_FORMAT = "X.509";
    private static final String PKCS8_FORMAT = "PKCS#8";
    private static final String PKCS1_FORMAT = "PKCS#1";

    private final Logger logger = LogManager.getLogger(SecurityUtilities.class);

    private final KeyFactory keyFactory;
    private final KeyPairGenerator keyPairGenerator;
    private final SSLProperties sslProperties;

    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
	@Autowired
    public SecurityUtilities(@Value("${security.key.algorithm:RSA}") final String keyFactoryAlgorithm,
                             @Value("${security.key.size:2048}") final Integer keySize,
                             final SSLProperties sslProperties) throws NoSuchAlgorithmException {
        Assert.hasText(keyFactoryAlgorithm, "keyFactoryAlgorithm must not be null");
        Assert.notNull(keySize, "keyFactoryAlgorithm keySize not be null");
        Assert.notNull(sslProperties, "sslProperties must not be null");
        this.sslProperties = sslProperties;
        keyFactory = KeyFactory.getInstance(keyFactoryAlgorithm);
        keyPairGenerator = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
        keyPairGenerator.initialize(keySize);
    }

    //-------------------------------------------------------------------------------------------------
    public static String getCertificateCNFromRequest(final HttpServletRequest request) {
        Assert.notNull(request, "request must not be null");
        final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
        if (certificates != null && certificates.length != 0) {
            for(final X509Certificate cert : certificates) {
                final String fullCN = Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
                if(Objects.isNull(fullCN)) { continue; }

                final String[] strings = fullCN.split("\\.");
                if(strings.length != 5) { continue; }

                return fullCN;
            }
        }

        return null;
    }

    //-------------------------------------------------------------------------------------------------
    public static String getCertificateCNFromServerRequest(final ServletServerHttpRequest request) {
        Assert.notNull(request, "request must not be null");
        final HttpServletRequest servletRequest = request.getServletRequest();

        return getCertificateCNFromRequest(servletRequest);
    }

    //-------------------------------------------------------------------------------------------------
    private static String getCommonNameFromCloudCertificate(final Principal principal) {
        Assert.notNull(principal, "Principal must not be null");
        return Utilities.getCertCNFromSubject(principal.getName());
    }

    //-------------------------------------------------------------------------------------------------
    private static String getOperatorNameFromCloudCertificate(final Principal principal) {
        return extractCNPart(principal, 2);
    }

    //-------------------------------------------------------------------------------------------------
    private static String getOrganizationFromCloudCertificate(final Principal principal) {
        return extractCNPart(principal, 1);
    }

    //-------------------------------------------------------------------------------------------------
    private static String getCountryFromCloudCertificate(final Principal principal) {
        return extractCNPart(principal, 0);
    }

    //-------------------------------------------------------------------------------------------------
    private static String extractCNPart(final Principal principal, final int tailIndex) {
        Assert.notNull(principal, "Principal must not be null");
        Assert.isTrue(tailIndex >= 0, "tailIndex must not be negative");
        final String fullCN = principal.getName();
        Assert.hasText(fullCN, "Empty common name is not allowed");

        final String[] strings = fullCN.split("\\.");

        if (tailIndex >= strings.length) {
            throw new IllegalArgumentException("Internal error: Unable to extract information from cloud certificate");
        }

        return strings[strings.length - (tailIndex + 1)];
    }

    //-------------------------------------------------------------------------------------------------
    public void authenticateCertificate(final HttpServletRequest httpServletRequest, final CertificateType minimumStrength) {
        Assert.notNull(httpServletRequest, "httpServletRequest must not be null");
        Assert.notNull(minimumStrength, "minimumStrength must not be null");

        final X509Certificate[] certificates = (X509Certificate[]) httpServletRequest.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);

        if (sslProperties.isSslEnabled()) {
            if (Objects.nonNull(certificates) && certificates.length > 0) {

                final String clientCN = getCertificateCNFromRequest(httpServletRequest);
                final String requestTarget = Utilities.stripEndSlash(httpServletRequest.getRequestURL().toString());

                authenticateCertificate(clientCN, requestTarget, minimumStrength);
            } else {
                logger.debug("No client certificate given!");
                throw new AuthException("Client certificate in needed!");
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    public void authenticateCertificate(final String clientCN, final String requestTarget, final CertificateType minimumStrength) {
        Assert.notNull(clientCN, "clientCN must not be null");
        Assert.notNull(requestTarget, "requestTarget must not be null");
        Assert.notNull(minimumStrength, "minimumStrength must not be null");

        if (sslProperties.isSslEnabled()) {

            final CertificateType type = CertificateType.getTypeFromCN(clientCN);
            if (!type.hasMinimumStrength(minimumStrength)) {
                logger.debug("{} is not a valid common name, access denied!", clientCN);
                throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    public KeyStore getKeyStore() {
        try {
            final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
            keystore.load(sslProperties.getKeyStore()
                                       .getInputStream(),
                          sslProperties.getKeyStorePassword()
                                       .toCharArray());
            return keystore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new ServiceConfigurationError("Cannot open keystore: " + e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    public String createCertificateSigningRequest(final String baseCommonName, final KeyPair keyPair, final CertificateType type,
                                                  final String host, final String address)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, OperatorCreationException {
        Assert.hasText(baseCommonName, "baseCommonName must not be null or empty!");
        Assert.notNull(keyPair, "keyPair must not be null");
        Assert.notNull(sslProperties.getKeyStore(), "KeyStore property must not be null!");
        Assert.hasText(sslProperties.getKeyStorePassword(), "KeyStore password property must not be null or empty!");
        Assert.notNull(type, "type must not be null");
        // host and address are optional

        logger.debug("Preparing Certificate Signing Request ...");
        // get cloud certificate for cloudName and operator
        final X509Certificate cloudCertFromKeyStore = Utilities.getCloudCertFromKeyStore(getKeyStore());

        // "CN=<commonName>.<type>.<cloudName>.<operator>.arrowhead.eu, OU=<operator>, O=arrowhead, C=eu"
        final String cloudName = getCommonNameFromCloudCertificate(cloudCertFromKeyStore.getSubjectDN());
        final String operator = getOperatorNameFromCloudCertificate(cloudCertFromKeyStore.getSubjectDN());
        final String organization = getOrganizationFromCloudCertificate(cloudCertFromKeyStore.getSubjectDN());
        final String country = getCountryFromCloudCertificate(cloudCertFromKeyStore.getSubjectDN());
        final String commonName = String.format("CN=%s.%s, OU=%s, O=%s, C=%s", type.appendTypeToCN(baseCommonName), cloudName,
                                                operator, organization, country);
        final X500Name x500Name = new X500Name(commonName);

        logger.debug("Building and Signing Certificate Signing Request for {}", x500Name);
        // create certificate signing request
        final JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500Name, keyPair.getPublic());
        final GeneralNamesBuilder namesBuilder = new GeneralNamesBuilder();

        namesBuilder.addName(new GeneralName(GeneralName.dNSName, "localhost"));
        namesBuilder.addName(new GeneralName(GeneralName.dNSName, baseCommonName));
        namesBuilder.addName(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));

        if (StringUtils.hasText(host)) {
            namesBuilder.addName(new GeneralName(GeneralName.dNSName, host));
        }
        if (StringUtils.hasText(address)) {
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
    public void extractAndSetPublicKey(final CertificateCreationResponseDTO creationResponseDTO) {
        Assert.notNull(creationResponseDTO, "creationResponseDTO must not be null");

        try {
            final byte[] certificateBytes = Base64Utils.decodeFromString(creationResponseDTO.getCertificate());

            final Certificate certificate;

            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certificateBytes)) {
                final CertificateFactory certificateFactory = CertificateFactory.getInstance(creationResponseDTO.getCertificateFormat());
                certificate = certificateFactory.generateCertificate(byteArrayInputStream);
            } catch (final CertificateException e) {
                logger.error("Unable to generate certificate from Base64: {}", creationResponseDTO.getCertificate());
                throw new ArrowheadException("Unable to decode signed certificate", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }

            final PublicKey publicKey = certificate.getPublicKey();
            creationResponseDTO.setKeyPairDTO(encodePublicKey(publicKey));

        } catch (final IOException e) {
            logger.fatal("ByteArrayInputStream should never throw an IOException, but it just did : {}", e.getMessage());
            throw new ArrowheadException(e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    public KeyPair extractOrGenerateKeyPair(final CertificateCreationRequestDTO creationRequestDTO) {
        Assert.notNull(creationRequestDTO, "creationRequestDTO must not be null");

        final String commonName = creationRequestDTO.getCommonName();
        Assert.isTrue(Utilities.notEmpty(commonName), "CommonName is null or blank");

        final KeyPairDTO keyPairDTO = creationRequestDTO.getKeyPairDTO();
        KeyPair keyPair;

        if (Objects.nonNull(keyPairDTO) &&
            Objects.nonNull(keyPairDTO.getPublicKey()) &&
            Objects.nonNull(keyPairDTO.getPrivateKey())) {

            final String encodedPublicKey = keyPairDTO.getPublicKey();
            final String encodedPrivateKey = keyPairDTO.getPrivateKey();
            final String keyFormat = keyPairDTO.getKeyFormat();


            if (Objects.nonNull(keyFormat)) {
                keyPair = getKeyPairFromBase64EncodedStringsForFormat(encodedPublicKey, encodedPrivateKey, keyFormat);
            } else {
                try {
                    keyPair = getKeyPairFromBase64EncodedStringsForFormat(encodedPublicKey, encodedPrivateKey, PKCS8_FORMAT);
                } catch (final AuthException e1) {
                    try {
                        keyPair = getKeyPairFromBase64EncodedStringsForFormat(encodedPublicKey, encodedPrivateKey, PKCS1_FORMAT);
                    } catch (final AuthException e2) {
                        keyPair = getKeyPairFromBase64EncodedStringsForFormat(encodedPublicKey, encodedPrivateKey, X509_FORMAT);
                    }
                }
            }
        } else {
            keyPair = generateKeyPair();
        }

        return keyPair;
    }

    //-------------------------------------------------------------------------------------------------
    public KeyPairDTO encodePublicKey(final PublicKey publicKey) {
        Assert.notNull(publicKey, "PublicKey must not be null");
        final String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return new KeyPairDTO(publicKey.getAlgorithm(), publicKey.getFormat(), encodedPublicKey, null);
    }

    //-------------------------------------------------------------------------------------------------
    public KeyPairDTO encodeKeyPair(final KeyPair keyPair) {
        Assert.notNull(keyPair, "KeyPair must not be null");
        final KeyPairDTO dto = encodePublicKey(keyPair.getPublic());
        if (Objects.nonNull(keyPair.getPrivate())) { dto.setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())); }
        return dto;
    }
    
    //=================================================================================================
	// assistant methods

    //-------------------------------------------------------------------------------------------------
    private KeyPair generateKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }

    //-------------------------------------------------------------------------------------------------
    private KeyPair getKeyPairFromBase64EncodedStringsForFormat(final String encodedPublicKey,
                                                                final String encodedPrivateKey,
                                                                final String keyFormat) {

        Assert.notNull(keyFormat, "KeyFormat must not be null");

        final byte[] publicKeyBytes = Base64.getDecoder().decode(encodedPublicKey);
        final byte[] privateKeyBytes = Base64.getDecoder().decode(encodedPrivateKey);
        final EncodedKeySpec publicKeySpec;
        final EncodedKeySpec privateKeySpec;

        // public key must be x509
        publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        if (keyFormat.equalsIgnoreCase(X509_FORMAT)) {
            privateKeySpec = new X509EncodedKeySpec(privateKeyBytes);
        } else if (keyFormat.equalsIgnoreCase(PKCS8_FORMAT)) {
            privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        } else if (keyFormat.equalsIgnoreCase(PKCS1_FORMAT)) {
            final byte[] pkcs8Bytes = convertPkcs1ToPkcs8(privateKeyBytes);
            privateKeySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
        } else {
            logger.error("getKeyPairFromBase64EncodedStringsForFormat: Unknown or unsupported key format.");
            throw new AuthException("Unknown or unsupported key format.");
        }

        return generateKeyPairFromSpec(privateKeySpec, publicKeySpec);
    }

    //-------------------------------------------------------------------------------------------------
	private KeyPair generateKeyPairFromSpec(final EncodedKeySpec privateKeySpec, final EncodedKeySpec publicKeySpec) {
        final PublicKey publicKey;
        final PrivateKey privateKey;

        try {
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (final InvalidKeySpecException ex) {
            logger.warn("generateKeyPairFromSpec: Unable to generate public key from key spec format '{}'.",
                        publicKeySpec.getFormat());
            throw new AuthException("Public key decoding failed due to wrong key format", ex);
        }

        try {
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (final InvalidKeySpecException ex) {
            logger.warn("generateKeyPairFromSpec: Unable to generate private key from key spec format '{}'.",
                        privateKeySpec.getFormat());
            throw new AuthException("Private key decoding failed due to wrong key format", ex);
        }
        return new KeyPair(publicKey, privateKey);
    }

    //-------------------------------------------------------------------------------------------------
	private byte[] convertPkcs1ToPkcs8(final byte[] pkcs1Bytes) {
        // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        final int pkcs1Length = pkcs1Bytes.length;
        final int totalLength = pkcs1Length + 22;
        final byte[] pkcs8Header = new byte[]{
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
                0x2, 0x1, 0x0, // Integer (0)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
        };
        return join(pkcs8Header, pkcs1Bytes);
    }

    //-------------------------------------------------------------------------------------------------
	private byte[] join(final byte[] byteArray1, final byte[] byteArray2) {
        final byte[] bytes = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, bytes, byteArray1.length, byteArray2.length);
        return bytes;
    }
}

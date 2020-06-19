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

import javax.servlet.http.HttpServletRequest;
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
        Assert.hasText(keyFactoryAlgorithm,"keyFactoryAlgorithm must not be null");
        Assert.notNull(keySize,"keyFactoryAlgorithm keySize not be null");
        Assert.notNull(sslProperties,"sslProperties must not be null");
        this.sslProperties = sslProperties;
        keyFactory = KeyFactory.getInstance(keyFactoryAlgorithm);
        keyPairGenerator = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
        keyPairGenerator.initialize(keySize);
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static String getCertificateCNFromRequest(final HttpServletRequest request) {
        Assert.notNull(request,"request must not be null");
        final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
        if (certificates != null && certificates.length != 0) {
            final X509Certificate cert = certificates[0];
            return Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
        }

        return null;
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
        Assert.notNull(httpServletRequest,"httpServletRequest must not be null");
        Assert.notNull(minimumStrength,"minimumStrength must not be null");

        final X509Certificate[] certificates = (X509Certificate[]) httpServletRequest.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);

        if (sslProperties.isSslEnabled()) {
            if (Objects.nonNull(certificates) && certificates.length > 0) {

                final X509Certificate cert = certificates[0];
                final String clientCN = Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
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
        Assert.notNull(clientCN,"clientCN must not be null");
        Assert.notNull(requestTarget,"requestTarget must not be null");
        Assert.notNull(minimumStrength,"minimumStrength must not be null");

        if (sslProperties.isSslEnabled()) {

            final CertificateType type = CertificateType.getTypeFromCN(clientCN);
            if (!type.hasMinimumStrength(minimumStrength)) {
                logger.debug("{} is not a valid common name, access denied!", clientCN);
                throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
            }
        }
    }

    //=================================================================================================
    // assistant methods

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
        final KeyStore keyStore = KeyStore.getInstance(sslProperties.getKeyStore().getFile(), sslProperties.getKeyStorePassword().toCharArray());
        final X509Certificate cloudCertFromKeyStore = Utilities.getCloudCertFromKeyStore(keyStore);

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
        final KeyPair keyPair;

        if (Objects.nonNull(keyPairDTO) &&
                Objects.nonNull(keyPairDTO.getPublicKey()) &&
                Objects.nonNull(keyPairDTO.getPrivateKey())) {

            final String encodedPublicKey = keyPairDTO.getPublicKey();
            final String encodedPrivateKey = keyPairDTO.getPrivateKey();

            keyPair = getKeyPairFromBase64EncodedStrings(encodedPublicKey, encodedPrivateKey);
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

    //-------------------------------------------------------------------------------------------------
    private PublicKey getPublicKeyFromBase64EncodedString(final String encodedKey) {
        Assert.isTrue(Utilities.notEmpty(encodedKey), "Encoded key is null or blank");

        final byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return generatePublicKeyFromByteArray(keyBytes);
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
    private PrivateKey getPrivateKeyFromBase64EncodedString(final String encodedKey) {
        Assert.isTrue(Utilities.notEmpty(encodedKey), "Encoded key is null or blank");

        final byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return generatePrivateKeyFromByteArray(keyBytes);
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

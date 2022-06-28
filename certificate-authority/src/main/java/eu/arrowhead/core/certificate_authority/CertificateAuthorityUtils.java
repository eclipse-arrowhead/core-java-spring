/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.ServiceConfigurationError;

public class CertificateAuthorityUtils {

    private static final Logger logger = LogManager.getLogger(CertificateAuthorityService.class);

    private static final String PROVIDER = "BC";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    static KeyStore getCertificateAuthorityKeyStore(SSLProperties sslProperties) {
        if (sslProperties == null) {
            throw new ServiceConfigurationError("sslProperties cannot be null");
        }
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

    static KeyStore getCloudKeyStore(CAProperties caProperties) {
        if (caProperties == null) {
            throw new ServiceConfigurationError("caProperties cannot be null");
        }
        try {
            final KeyStore keystore = KeyStore.getInstance(caProperties.getCloudKeyStoreType());
            keystore.load(caProperties.getCloudKeyStorePath()
                                      .getInputStream(),
                          caProperties.getCloudKeyStorePassword()
                                      .toCharArray());
            return keystore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new ServiceConfigurationError("Cannot open cloud keystore: " + e.getMessage());
        }
    }

    static void verifyCertificateSigningRequest(CertificateSigningRequestDTO request, String requesterCN,
                                                CAProperties caProperties) {
        if (request == null) {
            logger.error("CertificateSigningRequest cannot be null");
            throw new InvalidParameterException("CertificateSigningRequest cannot be null");
        }

        if (Utilities.isEmpty(request.getEncodedCSR())) {
            logger.error("CertificateSigningRequest cannot be empty");
            throw new InvalidParameterException("CertificateSigningRequest cannot be empty");
        }

        if (Utilities.isEmpty(requesterCN)) {
            logger.error("CertificateSigningRequest requester common name cannot be empty");
            throw new InvalidParameterException("CertificateSigningRequest requester common name cannot be empty");
        }

        if (caProperties == null) {
            logger.error("CaProperties cannot be empty");
            throw new InvalidParameterException("CaProperties cannot be empty");
        }

        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime validBefore = Utilities.parseUTCStringToLocalZonedDateTime(request.getValidBefore());
        if (validBefore != null) {
            final ZonedDateTime validBeforeLimit = getValidBeforeLimit(now, caProperties);
            if (validBefore.compareTo(validBeforeLimit) > 0) {
                final String msg = "Validity range parameter exceeds maximum validity range: validBefore (limit: "
                        + validBeforeLimit + ", got: " + validBefore + ")";
                logger.error(msg);
                throw new InvalidParameterException(msg);
            }
        }

        final ZonedDateTime validAfter = Utilities.parseUTCStringToLocalZonedDateTime(request.getValidAfter());
        if (validAfter != null) {
            final ZonedDateTime validAfterLimit = getValidAfterLimit(now, caProperties);
            if (validAfter.compareTo(validAfterLimit) < 0) {
                final String msg = "Validity range parameter exceeds maximum validity range: validAfter (limit: "
                        + validAfterLimit + ", got: " + validAfter + ")";
                logger.error(msg);
                throw new InvalidParameterException(msg);
            }
        }
    }

    static JcaPKCS10CertificationRequest decodePKCS10CSR(CertificateSigningRequestDTO csr) {
        if (csr == null) {
            throw new BadPayloadException("csr cannot be null");
        }
        if (Utilities.isEmpty(csr.getEncodedCSR())) {
            throw new BadPayloadException("encodedCsr cannot be empty");
        }
        try {
            final byte[] csrBytes = Base64.getDecoder()
                                          .decode(csr.getEncodedCSR());
            return new JcaPKCS10CertificationRequest(csrBytes);
        } catch (Exception e) {
            logger.error("Failed to parse request as a PKCS10 CSR, because: " + e.getMessage());
            throw new BadPayloadException("Failed to parse request as a PKCS10 CSR", e);
        }
    }

    static X509Certificate decodeCertificate(final String encodedCert) {
        if (Utilities.isEmpty(encodedCert)) {
            throw new BadPayloadException("encodedCert cannot be empty");
        }
        try {
            final byte[] requestBytes = Base64.getDecoder()
                                              .decode(encodedCert);
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final ByteArrayInputStream in = new ByteArrayInputStream(requestBytes);
            return (X509Certificate) factory.generateCertificate(in);
        } catch (CertificateException | IllegalArgumentException ex) {
            throw new InvalidParameterException("Cannot parse certificate, because: " + ex.getMessage(), ex);
        }
    }

    static String encodeCertificate(X509Certificate certificate) {
        if (certificate == null) {
            throw new InvalidParameterException("certificate cannot be null");
        }
        try {
            return Base64.getEncoder()
                         .encodeToString(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new AuthException("Certificate encoding failed! (" + e.getMessage() + ")", e);
        }
    }

    static String getCloudCommonName(X509Certificate cloudCertificate) {
        if (cloudCertificate == null) {
            throw new ServiceConfigurationError("Cloud certificate cannot be null");
        }
        try {
            return getCommonName(cloudCertificate);
        } catch (InvalidParameterException e) {
            logger.error("Cannot get common name from cloud cert, because: " + e.getMessage());
            throw new ServiceConfigurationError("Cannot get common name from cloud cert.", e);
        }
    }

    static String getCommonName(X509Certificate certificate) {
        if (certificate == null) {
            throw new InvalidParameterException("certificate cannot be null");
        }
        try {
            final X500Name subject = new JcaX509CertificateHolder(certificate).getSubject();
            return getCommonName(subject);
        } catch (CertificateEncodingException e) {
            logger.error("Cannot get common name from cert, because: " + e.getMessage());
            throw new InvalidParameterException("Cannot get common name from cert.", e);
        }
    }

    static BigInteger getSerialNumber(X509Certificate certificate) {
        if (certificate == null) {
            throw new InvalidParameterException("certificate cannot be null");
        }
        try {
            return new JcaX509CertificateHolder(certificate).getSerialNumber();
        } catch (CertificateEncodingException e) {
            logger.error("Cannot get serial number from cert, because: " + e.getMessage());
            throw new InvalidParameterException("Cannot get serial number from cert, because: " + e.getMessage(), e);
        }
    }

    static String getIssuer(X509Certificate certificate) {
        if (certificate == null) {
            throw new InvalidParameterException("certificate cannot be null");
        }
        try {
            final X500Name issuerName = new JcaX509CertificateHolder(certificate).getIssuer();
            return getCommonName(issuerName);
        } catch (CertificateEncodingException e) {
            logger.error("Cannot get serial number from cert, because: " + e.getMessage());
            throw new InvalidParameterException("Cannot get serial number from cert, because: " + e.getMessage(), e);
        }
    }

    static String getCommonName(X500Name name) {
        if (name == null) {
            throw new InvalidParameterException("Name cannot be null");
        }
        final RDN cn = name.getRDNs(BCStyle.CN)[0];
        return IETFUtils.valueToString(cn.getFirst()
                                         .getValue());
    }

    static String getCommonName(JcaPKCS10CertificationRequest csr) {
        if (csr == null) {
            throw new BadPayloadException("CSR cannot be null");
        }

        return getCommonName(csr.getSubject());
    }

    static void checkCommonName(JcaPKCS10CertificationRequest csr, String cloudCN) {
        if (csr == null) {
            throw new BadPayloadException("CSR cannot be null");
        }
        if (Utilities.isEmpty(cloudCN)) {
            throw new BadPayloadException("CloudCN cannot be null");
        }

        final String clientCN = getCommonName(csr);
        if (!Utilities.isKeyStoreCNArrowheadValid(clientCN, cloudCN)) {
            throw new BadPayloadException(
                    "Certificate request does not have a valid common name! Valid common name: {systemName}."
                            + cloudCN);
        }
    }

    static void checkProtectedCommonName(JcaPKCS10CertificationRequest csr, final String cloudCN,
                                         final String requestedBy) {
        if (csr == null || Utilities.isEmpty(cloudCN) || Utilities.isEmpty(requestedBy)) {
            throw new InvalidParameterException("Cannot check protected common names: null or empty params");
        }
        final String commonName = getCommonName(csr);
        checkProtectedCommonName(commonName, cloudCN, requestedBy);
    }

    static void checkProtectedCommonName(final String commonName, final String cloudCN, final String requestedBy) {
        if (Utilities.isEmpty(commonName) || Utilities.isEmpty(cloudCN) || Utilities.isEmpty(requestedBy)) {
            throw new InvalidParameterException("Cannot check protected common names: null or empty params");
        }
        final List<String> protectedCommonNames = getProtectedCommonNames(cloudCN);

        if (protectedCommonNames.contains(commonName)) {
            if (!requestedBy.equals("sysop." + cloudCN)) {
                throw new InvalidParameterException(
                        "Only sysop allowed to request a certificate with protected common name");
            }
        }
    }

    static void checkCsrSignature(JcaPKCS10CertificationRequest csr) {
        if (csr == null) {
            throw new BadPayloadException("csr cannot be null");
        }
        try {
            final ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
                    .setProvider(PROVIDER)
                    .build(csr.getSubjectPublicKeyInfo());
            if (!csr.isSignatureValid(verifierProvider)) {
                throw new BadPayloadException("Certificate request has invalid signature! (key pair does not match)");
            }
        } catch (OperatorCreationException | PKCSException e) {
            throw new BadPayloadException("Encapsulated " + e.getClass()
                                                             .getCanonicalName() + ": " + e.getMessage(), e);
        }
    }

    static PublicKey getClientKey(JcaPKCS10CertificationRequest csr) {
        if (csr == null) {
            throw new BadPayloadException("csr cannot be null");
        }
        try {
            return csr.getPublicKey();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // This should not be possible after a successful signature verification
            throw new DataNotFoundException("Extracting the public key from the CSR failed (" + e.getMessage() + ")",
                    e);
        }
    }

    static ZonedDateTime getValidAfterLimit(ZonedDateTime now, CAProperties caProperties) {
        Assert.notNull(now, "now cannot be null");
        Assert.notNull(caProperties, "caProperties cannot be null");
        return now.minusMinutes(caProperties.getCertValidityNegativeOffsetMinutes());
    }

    static ZonedDateTime getValidBeforeLimit(ZonedDateTime now, CAProperties caProperties) {
        Assert.notNull(now, "now cannot be null");
        Assert.notNull(caProperties, "caProperties cannot be null");
        return now.plusMinutes(caProperties.getCertValidityPositiveOffsetMinutes());
    }

    static ZonedDateTime getValidAfter(ZonedDateTime requested, ZonedDateTime now, CAProperties caProperties) {
        final ZonedDateTime validAfterLimit = getValidAfterLimit(now, caProperties);
        if (requested == null) {
            return validAfterLimit;
        }

        return (ZonedDateTime) max(requested, validAfterLimit);
    }

    static ZonedDateTime getValidBefore(ZonedDateTime requested, ZonedDateTime now, CAProperties caProperties) {
        final ZonedDateTime validBeforeLimit = getValidBeforeLimit(now, caProperties);
        if (requested == null) {
            return validBeforeLimit;
        }

        return (ZonedDateTime) min(requested, validBeforeLimit);
    }

    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    static <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) < 0 ? a : b;
    }


    static X509Certificate buildCertificate(JcaPKCS10CertificationRequest csr, PrivateKey cloudPrivateKey,
                                            X509Certificate cloudCertificate, CAProperties caProperties, SecureRandom random) {
        Assert.notNull(csr, "csr cannot be null");
        Assert.notNull(cloudPrivateKey, "cloudPrivateKey cannot be null");
        Assert.notNull(cloudCertificate, "cloudCertificate cannot be null");
        Assert.notNull(caProperties, "caProperties cannot be null");
        Assert.notNull(random, "random cannot be null");

        final ZonedDateTime now = ZonedDateTime.now();
        final Date validFrom = Date.from(getValidAfterLimit(now, caProperties).toInstant());
        final Date validUntil = Date.from(getValidBeforeLimit(now, caProperties).toInstant());

        logger.debug("Setting validity from='{}' to='{}'", validFrom, validUntil);
        return buildCertificate(csr, cloudPrivateKey, cloudCertificate, validFrom, validUntil, random);
    }

    static X509Certificate buildCertificate(JcaPKCS10CertificationRequest csr, PrivateKey cloudPrivateKey,
                                            X509Certificate cloudCertificate, Date validFrom, Date validUntil, SecureRandom random) {
        Assert.notNull(csr, "csr cannot be null");
        Assert.notNull(cloudPrivateKey, "cloudPrivateKey cannot be null");
        Assert.notNull(cloudCertificate, "cloudCertificate cannot be null");
        Assert.notNull(validFrom, "validFrom cannot be null");
        Assert.notNull(validUntil, "validUntil cannot be null");
        Assert.notNull(random, "random cannot be null");

        final BigInteger serial = new BigInteger(32, random);
        final PublicKey clientKey = getClientKey(csr);

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(cloudCertificate, serial, validFrom,
                validUntil, csr.getSubject(), clientKey);

        addCertificateExtensions(builder, csr, clientKey, cloudCertificate);

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(PROVIDER);
        try {
            return new JcaX509CertificateConverter().setProvider(PROVIDER)
                                                    .getCertificate(builder.build(signerBuilder.build(cloudPrivateKey)));
        } catch (CertificateException e) {
            throw new BadPayloadException("Certificate encoding failed! (" + e.getMessage() + ")", e);
        } catch (OperatorCreationException e) {
            throw new BadPayloadException("Certificate signing failed! (" + e.getMessage() + ")", e);
        }
    }

    static List<String> buildEncodedCertificateChain(X509Certificate clientCertificate,
                                                     X509Certificate cloudCertificate, X509Certificate rootCertificate) {
        Assert.notNull(clientCertificate, "clientCertificate cannot be null");
        Assert.notNull(cloudCertificate, "cloudCertificate cannot be null");
        Assert.notNull(rootCertificate, "rootCertificate cannot be null");
        final ArrayList<String> encodedCertificateChain = new ArrayList<>();
        encodedCertificateChain.add(encodeCertificate(clientCertificate));
        encodedCertificateChain.add(encodeCertificate(cloudCertificate));
        encodedCertificateChain.add(encodeCertificate(rootCertificate));
        return encodedCertificateChain;
    }

    /**
     * Adding the following extensions to the new certificate:
     * <ol>
     * <li>The subject alternative name makes possible to use the certificate for
     * accessing a host via IP address or hostname</li>
     *
     * <li>The subject key identifier provides a hashed value that should uniquely
     * identify the public key</li>
     *
     * <li>The authority key identifier provides a hashed value that should uniquely
     * identify the issuer of the certificate</li>
     *
     * <li>And this basic constraint is for forbidding issuing other certificates
     * under this certificate</li>
     * </ol>
     */
    static void addCertificateExtensions(X509v3CertificateBuilder builder, JcaPKCS10CertificationRequest csr,
                                         PublicKey clientKey, X509Certificate cloudCertificate) {
        Assert.notNull(builder, "builder cannot be null");
        Assert.notNull(csr, "csr cannot be null");
        Assert.notNull(clientKey, "clientKey cannot be null");
        Assert.notNull(cloudCertificate, "cloudCertificate cannot be null");
        try {
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

            final List<GeneralName> subjectAlternativeNames = getSubjectAlternativeNames(csr);
            builder.addExtension(Extension.subjectAlternativeName, false,
                    new GeneralNames(subjectAlternativeNames.toArray(new GeneralName[]{})));
            builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(clientKey));
            builder.addExtension(Extension.authorityKeyIdentifier, false,
                    extUtils.createAuthorityKeyIdentifier(cloudCertificate));
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        } catch (NoSuchAlgorithmException | CertIOException | CertificateEncodingException e) {
            throw new InvalidParameterException(
                    "Appending extensions to the certificate failed! (" + e.getMessage() + ")", e);
        }
    }

    static List<GeneralName> getSubjectAlternativeNames(JcaPKCS10CertificationRequest csr) {
        if (csr == null) {
            throw new InvalidParameterException("CSR cannot be null");
        }

        List<GeneralName> alternativeNames = new ArrayList<>();
        for (final Attribute attribute : csr.getAttributes()) {
            if (attribute == null) {
                continue;
            }
            if (attribute.getAttrType()
                         .equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                final Extensions extensions = Extensions.getInstance(attribute.getAttrValues()
                                                                              .getObjectAt(0));
                final GeneralNames generalNames = GeneralNames.fromExtensions(extensions,
                        Extension.subjectAlternativeName);
                if (generalNames != null && generalNames.getNames() != null && generalNames.getNames().length > 0) {
                    for (final GeneralName name : generalNames.getNames()) {
                        if (name.getTagNo() == GeneralName.dNSName || name.getTagNo() == GeneralName.iPAddress) {
                            alternativeNames.add(name);
                        }
                    }
                }
            }
        }

        return alternativeNames;
    }

    public static String sha256(final String data) {
        if (Utilities.isEmpty(data)) {
            throw new InvalidParameterException("Data to hash cannot be empty");
        }
        return new String(DigestUtils.sha256(data));
    }

    public static String getRequesterCommonName(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new InvalidParameterException("httpServiceRequest cannot be null");
        }
        final X509Certificate[] clientCerts = (X509Certificate[]) httpServletRequest
                .getAttribute("javax.servlet.request.X509Certificate");
        if (clientCerts == null || clientCerts.length < 1) {
            throw new InvalidParameterException("Unexpected client cert");
        }
        final String requestedByCN = getCommonName(clientCerts[0]);
        return requestedByCN;
    }

    public static List<String> getProtectedCommonNames(final String cloudCommonName) {
        if (Utilities.isEmpty(cloudCommonName)) {
            throw new InvalidParameterException("CloudCommonNAme cannot be empty");
        }

        List<String> names = new ArrayList<>();
        names.add("sysop." + cloudCommonName);

        final CoreSystem[] systems = CoreSystem.values();
        for (CoreSystem system : systems) {
            final String sysName = system.name()
                                         .toLowerCase();

            names.add(sysName + "." + cloudCommonName);

            if (sysName.contains("_")) {
                names.add(sysName.replace("_", "-") + "." + cloudCommonName);
            } else if (sysName.contains("-")) {
                names.add(sysName.replace("-", "_") + "." + cloudCommonName);
            }
        }

        return names;
    }

    private CertificateAuthorityUtils() {
    }
}

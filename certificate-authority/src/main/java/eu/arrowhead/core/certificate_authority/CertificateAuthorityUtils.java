package eu.arrowhead.core.certificate_authority;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.ServiceConfigurationError;

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

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;

class CertificateAuthorityUtils {

    private static final Logger logger = LogManager.getLogger(CertificateAuthorityService.class);

    private static final String PROVIDER = "BC";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    static JcaPKCS10CertificationRequest decodePKCS10CSR(CertificateSigningRequestDTO csr) {
        try {
            final byte[] csrBytes = Base64.getDecoder().decode(csr.getEncodedCSR());
            return new JcaPKCS10CertificationRequest(csrBytes);
        } catch (Exception e) {
            logger.error("Failed to parse request as a PKCS10 CSR, because: " + e.getMessage());
            throw new BadPayloadException("Failed to parse request as a PKCS10 CSR", e);
        }
    }

    static String encodeCertificate(X509Certificate certificate) {
        try {
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (CertificateEncodingException | NullPointerException e) {
            throw new AuthException("Certificate encoding failed! (" + e.getMessage() + ")", e);
        }
    }

    static String getCloudCommonName(X509Certificate cloudCertificate) {
        try {
            final X500Name subject = new JcaX509CertificateHolder(cloudCertificate).getSubject();
            final RDN cn = subject.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException | NullPointerException e) {
            logger.error("Cannot get common name from cloud cert, because: " + e.getMessage());
            throw new ServiceConfigurationError("Cannot get common name from cloud cert.", e);
        }
    }

    static void checkCommonName(JcaPKCS10CertificationRequest csr, String cloudCN) {
        if (csr == null) {
            throw new BadPayloadException("CSR cannot be null");
        }
        if (cloudCN == null || cloudCN.isEmpty()) {
            throw new BadPayloadException("CloudCN cannot be null");
        }
        final String clientCN = Utilities.getCertCNFromSubject(csr.getSubject().toString());
        if (!Utilities.isKeyStoreCNArrowheadValid(clientCN, cloudCN)) {
            throw new BadPayloadException(
                    "Certificate request does not have a valid common name! Valid common name: {systemName}."
                            + cloudCN);
        }
    }

    static void checkCsrSignature(JcaPKCS10CertificationRequest csr) {
        try {
            final ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
                    .setProvider(PROVIDER)
                    .build(csr.getSubjectPublicKeyInfo());
            if (!csr.isSignatureValid(verifierProvider)) {
                throw new BadPayloadException("Certificate request has invalid signature! (key pair does not match)");
            }
        } catch (OperatorCreationException | PKCSException | NullPointerException e) {
            throw new BadPayloadException("Encapsulated " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
        }
    }

    static PublicKey getClientKey(JcaPKCS10CertificationRequest csr) {
        try {
            return csr.getPublicKey();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NullPointerException e) {
            // This should not be possible after a successful signature verification
            throw new DataNotFoundException("Extracting the public key from the CSR failed (" + e.getMessage() + ")",
                    e);
        }
    }

    static X509Certificate buildCertificate(JcaPKCS10CertificationRequest csr, PrivateKey cloudPrivateKey,
            X509Certificate cloudCertificate, Date validFrom, Date validUntil, SecureRandom random) {

        final ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        final BigInteger serial = BigInteger.valueOf(now.toInstant().toEpochMilli())
                .multiply(BigInteger.valueOf(random.nextLong()));

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
        try {
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

            final List<GeneralName> subjectAlternativeNames = getSubjectAlternativeNames(csr);
            builder.addExtension(Extension.subjectAlternativeName, false,
                    new GeneralNames(subjectAlternativeNames.toArray(new GeneralName[] {})));
            builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(clientKey));
            builder.addExtension(Extension.authorityKeyIdentifier, false,
                    extUtils.createAuthorityKeyIdentifier(cloudCertificate));
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        } catch (NoSuchAlgorithmException | CertIOException | CertificateEncodingException | NullPointerException e) {
            throw new InvalidParameterException("Appending extensions to the certificate failed! (" + e.getMessage() + ")", e);
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
            if (attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                final Extensions extensions = Extensions.getInstance(attribute.getAttrValues().getObjectAt(0));
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

    private CertificateAuthorityUtils() {
    }
}

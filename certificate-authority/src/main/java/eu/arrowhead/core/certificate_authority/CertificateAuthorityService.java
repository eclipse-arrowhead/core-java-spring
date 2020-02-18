package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.ServiceConfigurationError;

@Service
public class CertificateAuthorityService {

    @Autowired
    private SSLProperties sslProperties;

    @Autowired
    private ApplicationContext appContext;

    private SecureRandom random;
    private KeyStore keyStore;

    @PostConstruct
    private void init() {
        random = new SecureRandom();
        random.reseed();
        keyStore = getKeyStore();
    }

    public String getCloudCommonName() {
        try {
            final X500Name issuer = new JcaX509CertificateHolder(getServerCertificate(appContext)).getIssuer();
            final RDN cn = issuer.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException e) {
            throw new ServiceConfigurationError("Cannot get cloud common name from server cert.", e);
        }
    }

    public CertificateSigningResponseDTO signCertificate(CertificateSigningRequestDTO request) {
        JcaPKCS10CertificationRequest csr = decodePKCS10CSR(request);
        checkCommonName(csr, getCloudCommonName());
        checkCsrSignature(csr);

        final PrivateKey cloudPrivateKey = Utilities.getPrivateKey(keyStore, getCloudCommonName(), sslProperties.getKeyPassword());
        final X509Certificate serverCertificate = getServerCertificate(appContext);
        final X509Certificate clientCertificate = buildCertificate(csr.getSubject(), getClientKey(csr), cloudPrivateKey, serverCertificate);

        final String encodedClientCertificate = encodeCertificate(clientCertificate);
        final String encodedServerCertificate = encodeCertificate(serverCertificate);

        return new CertificateSigningResponseDTO(encodedClientCertificate + " " + encodedServerCertificate);
    }

    private KeyStore getKeyStore() {
        try {
            final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
            keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
            return keystore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new ServiceConfigurationError("Cannot open keystore: " + e.getMessage());
        }
    }

    private JcaPKCS10CertificationRequest decodePKCS10CSR(CertificateSigningRequestDTO csr) {
        try {
            final byte[] csrBytes = Base64.getDecoder().decode(csr.getCertificatePem());
            return new JcaPKCS10CertificationRequest(csrBytes);
        } catch (IOException ex) {
            throw new BadPayloadException("Failed to parse request as a PKCS10 CSR", ex);
        }
    }

    private String encodeCertificate(X509Certificate certificate) {
        try {
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new AuthException("Certificate encoding failed! (" + e.getMessage() + ")", e);
        }
    }

    private void checkCommonName(JcaPKCS10CertificationRequest csr, String cloudCN) {
        final String clientCN = Utilities.getCertCNFromSubject(csr.getSubject().toString());
        if (!Utilities.isKeyStoreCNArrowheadValid(clientCN, cloudCN)) {
            throw new BadPayloadException("Certificate request does not have a valid common name! Valid common name: {systemName}." + cloudCN);
        }
    }

    private void checkCsrSignature(JcaPKCS10CertificationRequest csr) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BC").build(csr.getSubjectPublicKeyInfo());
            if (!csr.isSignatureValid(verifierProvider)) {
                throw new AuthException("Certificate request has invalid signature! (key pair does not match)");
            }
        } catch (OperatorCreationException | PKCSException e) {
            throw new AuthException("Encapsulated " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
        }
    }

    private PublicKey getClientKey(JcaPKCS10CertificationRequest csr) {
        try {
            return csr.getPublicKey();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            //This should not be possible after a successful signature verification
            throw new DataNotFoundException("Extracting the public key from the CSR failed (" + e.getMessage() + ")", e);
        }
    }

    private X509Certificate getServerCertificate(final ApplicationContext appContext) {
        @SuppressWarnings("unchecked") final Map<String, Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
        return (X509Certificate) context.get(CommonConstants.SERVER_CERTIFICATE);
    }

    private X509Certificate buildCertificate(X500Name csrSubject, PublicKey clientKey, PrivateKey cloudPrivateKey, X509Certificate serverCertificate) {
        final ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        final Date validFrom = Date.from(now.minusSeconds(5).toInstant());
        final Date validUntil = Date.from(now.plusYears(1).toInstant());

        final BigInteger serial = BigInteger.valueOf(now.toInstant().toEpochMilli()).multiply(BigInteger.valueOf(random.nextLong()));

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(serverCertificate, serial, validFrom, validUntil, csrSubject, clientKey);

        /* Adding the following extensions to the new certificate:
           1) The subject key identifier provides a hashed value that should uniquely identify the public key
           2) The authority key identifier provides a hashed value that should uniquely identify the issuer of the certificate
           3) And this basic constraint is for forbidding issuing other certificates under this certificate
        */
        try {
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(clientKey));
            builder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(serverCertificate));
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        } catch (NoSuchAlgorithmException | CertIOException | CertificateEncodingException e) {
            throw new AuthException("Appending extensions to the certificate failed! (" + e.getMessage() + ")", e);
        }


        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA").setProvider("BC");
        try {
            return new JcaX509CertificateConverter().setProvider("BC")
                    .getCertificate(builder.build(signerBuilder.build(cloudPrivateKey)));
        } catch (CertificateException e) {
            throw new AuthException("Certificate encoding failed! (" + e.getMessage() + ")", e);
        } catch (OperatorCreationException e) {
            throw new AuthException("Certificate signing failed! (" + e.getMessage() + ")", e);
        }
    }
}

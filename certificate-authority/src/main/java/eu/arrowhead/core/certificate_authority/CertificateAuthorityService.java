package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class CertificateAuthorityService {

    private static final Logger logger = LogManager.getLogger(CertificateAuthorityService.class);

    @Autowired
    private SSLProperties sslProperties;

    private SecureRandom random;
    private KeyStore keyStore;

    private X509Certificate rootCertificate;
    private X509Certificate cloudCertificate;

    @PostConstruct
    private void init() {
        random = new SecureRandom();
        random.reseed();
        keyStore = getKeyStore();

        rootCertificate = Utilities.getRootCertFromKeyStore(keyStore);
        cloudCertificate = Utilities.getCloudCertFromKeyStore(keyStore);
    }

    public String getCloudCommonName() {
        try {
            final X500Name subject = new JcaX509CertificateHolder(cloudCertificate).getSubject();
            final RDN cn = subject.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException e) {
            throw new ServiceConfigurationError("Cannot get cloud common name from server cert.", e);
        }
    }

    public CertificateSigningResponseDTO signCertificate(CertificateSigningRequestDTO request) {
        final JcaPKCS10CertificationRequest csr = CertificateAuthorityUtils.decodePKCS10CSR(request);
        CertificateAuthorityUtils.checkCommonName(csr, getCloudCommonName());
        CertificateAuthorityUtils.checkCsrSignature(csr);

        logger.info("Signing certificate for " + csr.getSubject().toString() + "...");

        final PrivateKey cloudPrivateKey = Utilities.getPrivateKey(keyStore, getCloudCommonName(),
                sslProperties.getKeyPassword());

        final X509Certificate clientCertificate = buildCertificate(csr, cloudPrivateKey, cloudCertificate);
        final List<String> encodedCertificateChain = buildEncodedCertificateChain(clientCertificate);

        return new CertificateSigningResponseDTO(encodedCertificateChain);
    }

    private KeyStore getKeyStore() {
        try {
            final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
            keystore.load(sslProperties.getKeyStore().getInputStream(),
                    sslProperties.getKeyStorePassword().toCharArray());
            return keystore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new ServiceConfigurationError("Cannot open keystore: " + e.getMessage());
        }
    }

    private List<String> buildEncodedCertificateChain(X509Certificate clientCertificate) {
        final ArrayList<String> encodedCertificateChain = new ArrayList<>();
        encodedCertificateChain.add(CertificateAuthorityUtils.encodeCertificate(clientCertificate));
        encodedCertificateChain.add(CertificateAuthorityUtils.encodeCertificate(cloudCertificate));
        encodedCertificateChain.add(CertificateAuthorityUtils.encodeCertificate(rootCertificate));
        return encodedCertificateChain;
    }

    private X509Certificate buildCertificate(JcaPKCS10CertificationRequest csr, PrivateKey cloudPrivateKey,
            X509Certificate cloudCertificate) {
        final ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        final Date validFrom = Date.from(now.minusSeconds(5).toInstant());
        final Date validUntil = Date.from(now.plusYears(1).toInstant());

        return CertificateAuthorityUtils.buildCertificate(csr, cloudPrivateKey, cloudCertificate, validFrom, validUntil,
                random);
    }
}

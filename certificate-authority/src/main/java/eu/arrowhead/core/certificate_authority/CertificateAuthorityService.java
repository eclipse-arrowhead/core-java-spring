package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ServiceConfigurationError;

@Service
public class CertificateAuthorityService {

    private static final Logger logger = LogManager.getLogger(CertificateAuthorityService.class);

    @Autowired
    private SSLProperties sslProperties;

    @Autowired
    private CAProperties caProperties;

    private SecureRandom random;
    private KeyStore keyStore;

    private X509Certificate rootCertificate;
    private X509Certificate cloudCertificate;
    private String cloudCommonName;

    @PostConstruct
    private void init() {
        random = new SecureRandom();
        //random.reseed();
        keyStore = getKeyStore();

        rootCertificate = Utilities.getRootCertFromKeyStore(keyStore);
        cloudCertificate = Utilities.getCloudCertFromKeyStore(keyStore);
        cloudCommonName = CertificateAuthorityUtils.getCloudCommonName(cloudCertificate);
    }

    public String getCloudCommonName() {
        return cloudCommonName;
    }

    public CertificateSigningResponseDTO signCertificate(CertificateSigningRequestDTO request) {
        verifyCertificateSigningRequest(request);

        final JcaPKCS10CertificationRequest csr = CertificateAuthorityUtils.decodePKCS10CSR(request);
        CertificateAuthorityUtils.checkCommonName(csr, cloudCommonName);
        CertificateAuthorityUtils.checkCsrSignature(csr);

        logger.info("Signing certificate for " + csr.getSubject().toString() + "...");

        final PrivateKey cloudPrivateKey = Utilities.getPrivateKey(keyStore, cloudCommonName,
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

    private static void verifyCertificateSigningRequest(CertificateSigningRequestDTO request) {
        if (request == null) {
            logger.error("CertificateSigningRequest cannot be null");
            throw new InvalidParameterException("CertificateSigningRequest cannot be null");
        }

        final String encodedCSR = request.getEncodedCSR();

        if (encodedCSR == null || encodedCSR.isEmpty()) {
            logger.error("CertificateSigningRequest cannot be empty");
            throw new InvalidParameterException("CertificateSigningRequest cannot be empty");
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
        final Date validFrom = Date.from(
                now.minus(Duration.ofMillis(caProperties.getCertValidityPositiveOffsetMillis())).toInstant());
        final Date validUntil = Date.from(
                now.plus(Duration.ofMillis(caProperties.getCertValidityPositiveOffsetMillis())).toInstant());

        return CertificateAuthorityUtils.buildCertificate(csr, cloudPrivateKey, cloudCertificate, validFrom, validUntil,
                random);
    }
}

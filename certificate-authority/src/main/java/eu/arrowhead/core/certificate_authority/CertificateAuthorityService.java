package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.core.certificate_authority.database.CATrustedKeyDBService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

@Service
public class CertificateAuthorityService {

    private static final Logger logger = LogManager.getLogger(CertificateAuthorityService.class);

    @Autowired
    private SSLProperties sslProperties;

    @Autowired
    private CAProperties caProperties;

    @Autowired
    private CATrustedKeyDBService trustedKeyDbService;

    private SecureRandom random;
    private KeyStore keyStore;

    private X509Certificate rootCertificate;
    private X509Certificate cloudCertificate;
    private String cloudCommonName;

    @PostConstruct
    private void init() {
        random = new SecureRandom();
        keyStore = CertificateAuthorityUtils.getKeyStore(sslProperties);

        rootCertificate = Utilities.getRootCertFromKeyStore(keyStore);
        cloudCertificate = Utilities.getCloudCertFromKeyStore(keyStore);
        cloudCommonName = CertificateAuthorityUtils.getCloudCommonName(cloudCertificate);
    }

    public String getCloudCommonName() {
        return cloudCommonName;
    }

    public CertificateSigningResponseDTO signCertificate(CertificateSigningRequestDTO request) {
        CertificateAuthorityUtils.verifyCertificateSigningRequest(request);

        final JcaPKCS10CertificationRequest csr = CertificateAuthorityUtils.decodePKCS10CSR(request);
        CertificateAuthorityUtils.checkCommonName(csr, cloudCommonName);
        CertificateAuthorityUtils.checkCsrSignature(csr);

        logger.info("Signing certificate for " + csr.getSubject().toString() + "...");

        final PrivateKey cloudPrivateKey = Utilities.getCloudPrivateKey(keyStore, cloudCommonName,
                                                                        sslProperties.getKeyPassword());

        final X509Certificate clientCertificate = CertificateAuthorityUtils.buildCertificate(csr, cloudPrivateKey,
                cloudCertificate, caProperties, random);
        final List<String> encodedCertificateChain = CertificateAuthorityUtils
                .buildEncodedCertificateChain(clientCertificate, cloudCertificate, rootCertificate);

        return new CertificateSigningResponseDTO(encodedCertificateChain);
    }

    public TrustedKeyCheckResponseDTO checkTrustedKey(TrustedKeyCheckRequestDTO request) {
        return trustedKeyDbService.isTrustedKeyValidNow(request);
    }

    public TrustedKeysResponseDTO getTrustedKeys(int page, int size, Direction direction, String sortField) {
        return trustedKeyDbService.getTrustedKeyEntries(page, size, direction, sortField);
    }

    public void addTrustedKey(AddTrustedKeyRequestDTO request) {
        trustedKeyDbService.addTrustedKey(request);
    }

    public void deleteTrustedKey(long id) {
       trustedKeyDbService.deleteTrustedKey(id);
    }
}

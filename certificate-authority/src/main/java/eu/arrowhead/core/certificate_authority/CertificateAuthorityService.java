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
import eu.arrowhead.common.database.entity.CaCertificate;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.AddTrustedKeyResponseDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckResponseDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.internal.IssuedCertificateStatus;
import eu.arrowhead.common.dto.internal.IssuedCertificatesResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.certificate_authority.database.CACertificateDBService;
import eu.arrowhead.core.certificate_authority.database.CATrustedKeyDBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class CertificateAuthorityService {

    private static final Logger logger = LogManager.getLogger(CertificateAuthorityService.class);

    @Autowired
    private CAProperties caProperties;

    @Autowired
    private CACertificateDBService certificateDbService;

    @Autowired
    private CATrustedKeyDBService trustedKeyDbService;

    private SecureRandom random;
    private KeyStore cloudKeyStore;

    private X509Certificate rootCertificate;
    private X509Certificate cloudCertificate;
    private String cloudCommonName;

    @PostConstruct
    protected void init() { // protected for testing
        random = new SecureRandom();
        cloudKeyStore = CertificateAuthorityUtils.getCloudKeyStore(caProperties);

        rootCertificate = Utilities.getRootCertFromKeyStore(cloudKeyStore);
        cloudCertificate = Utilities.getCloudCertFromKeyStore(cloudKeyStore);
        cloudCommonName = CertificateAuthorityUtils.getCloudCommonName(cloudCertificate);
    }

    public String getCloudCommonName() {
        return cloudCommonName;
    }

    public CertificateCheckResponseDTO checkCertificate(CertificateCheckRequestDTO request) {
        if (request == null) {
            throw new InvalidParameterException("CertificateCheckRequestDTO cannot be null");
        }
        if (Utilities.isEmpty(request.getCertificate())) {
            throw new InvalidParameterException("Certificate cannot be null");
        }

        final X509Certificate cert = CertificateAuthorityUtils.decodeCertificate(request.getCertificate());
        final String certCN = CertificateAuthorityUtils.getCommonName(cert);
        final BigInteger certSerial = CertificateAuthorityUtils.getSerialNumber(cert);

        try {
            if (!cert.getIssuerX500Principal().equals(cloudCertificate.getSubjectX500Principal())) {
                throw new DataNotFoundException("Certificate is not issued by this cloud");
            }

            return certificateDbService.isCertificateValidNow(certSerial);
        } catch (DataNotFoundException ex) {
            final ZonedDateTime endOfValidity = ZonedDateTime.ofInstant(cert.getNotAfter().toInstant(), ZoneId.systemDefault());
            final String endOfValidityString = Utilities.convertZonedDateTimeToUTCString(endOfValidity);
            final String now = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
            return new CertificateCheckResponseDTO(request.getVersion(), now, endOfValidityString, certCN,
                                                   certSerial, IssuedCertificateStatus.UNKNOWN);
        }
    }

    public CertificateSigningResponseDTO signCertificate(CertificateSigningRequestDTO request, String requesterCN) {
        CertificateAuthorityUtils.verifyCertificateSigningRequest(request, requesterCN, caProperties);

        final JcaPKCS10CertificationRequest csr = CertificateAuthorityUtils.decodePKCS10CSR(request);
        CertificateAuthorityUtils.checkCommonName(csr, cloudCommonName);
        CertificateAuthorityUtils.checkProtectedCommonName(csr, cloudCommonName, requesterCN);
        CertificateAuthorityUtils.checkCsrSignature(csr);

        logger.info("Signing certificate for " + csr.getSubject().toString() + "...");

        final PrivateKey cloudPrivateKey = Utilities.getCloudPrivateKey(cloudKeyStore, cloudCommonName,
                                                                        caProperties.getCloudKeyPassword());

        final X509Certificate clientCertificate = CertificateAuthorityUtils.buildCertificate(csr, cloudPrivateKey,
                                                                                             cloudCertificate, caProperties, random);
        final List<String> encodedCertificateChain = CertificateAuthorityUtils
                .buildEncodedCertificateChain(clientCertificate, cloudCertificate, rootCertificate);

        final String clientCommonName = CertificateAuthorityUtils.getCommonName(csr);
        final BigInteger serialNumber = CertificateAuthorityUtils.getSerialNumber(clientCertificate);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime validBefore = CertificateAuthorityUtils.getValidBefore(
                Utilities.parseUTCStringToLocalZonedDateTime(request.getValidBefore()), now, caProperties);
        final ZonedDateTime validAfter = CertificateAuthorityUtils.getValidAfter(
                Utilities.parseUTCStringToLocalZonedDateTime(request.getValidAfter()), now, caProperties);

        final CaCertificate caCert = certificateDbService.saveCertificateInfo(clientCommonName, serialNumber, requesterCN,
                                                                              validAfter, validBefore);

        return new CertificateSigningResponseDTO(caCert.getId(), encodedCertificateChain);
    }

    public IssuedCertificatesResponseDTO getCertificates(int page, int size, Direction direction, String sortField) {
        return certificateDbService.getCertificateEntries(page, size, direction, sortField);
    }

    public boolean revokeCertificate(long id, String requestedByCN) {
        return certificateDbService.revokeCertificate(id, requestedByCN);
    }

    public TrustedKeyCheckResponseDTO checkTrustedKey(TrustedKeyCheckRequestDTO request) {
        return trustedKeyDbService.isTrustedKeyValidNow(request);
    }

    public TrustedKeysResponseDTO getTrustedKeys(int page, int size, Direction direction, String sortField) {
        return trustedKeyDbService.getTrustedKeyEntries(page, size, direction, sortField);
    }

    public AddTrustedKeyResponseDTO addTrustedKey(AddTrustedKeyRequestDTO request) {
        return trustedKeyDbService.addTrustedKey(request);
    }

    public void deleteTrustedKey(long id) {
        trustedKeyDbService.deleteTrustedKey(id);
    }
}

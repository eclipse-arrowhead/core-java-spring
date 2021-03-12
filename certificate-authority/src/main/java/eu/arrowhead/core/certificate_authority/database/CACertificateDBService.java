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

package eu.arrowhead.core.certificate_authority.database;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.CaCertificate;
import eu.arrowhead.common.database.repository.CaCertificateRepository;
import eu.arrowhead.common.dto.internal.CertificateCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.IssuedCertificateStatus;
import eu.arrowhead.common.dto.internal.IssuedCertificatesResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class CACertificateDBService {

    @Autowired
    private CaCertificateRepository caCertificateRepository;

    private final Logger logger = LogManager.getLogger(CACertificateDBService.class);

    @Transactional(rollbackFor = ArrowheadException.class)
    public CaCertificate saveCertificateInfo(final String commonName, final BigInteger serial, final String requesterCN,
                                             ZonedDateTime validAfter, final ZonedDateTime validBefore) {
        logger.debug("saveCertificateInfo started...");

        try {
            if (Utilities.isEmpty(commonName)) {
                throw new InvalidParameterException("commonName cannot be empty");
            }
            if (serial == null) {
                throw new InvalidParameterException("serial cannot be empty");
            }
            if (Utilities.isEmpty(requesterCN)) {
                throw new InvalidParameterException("requesterCN cannot be empty");
            }
            if (validBefore == null) {
                throw new InvalidParameterException("validBefore cannot be null");
            }

            if (validAfter == null) {
                validAfter = ZonedDateTime.now();
            }

            if (validBefore.isBefore(validAfter)) {
                throw new InvalidParameterException("validBefore must be later than validAfter");
            }
            logger.debug("saveCertificateInfo for " + commonName);

            final CaCertificate certificate = new CaCertificate(commonName, serial, requesterCN, validAfter, validBefore);
            return caCertificateRepository.saveAndFlush(certificate);
        } catch (final InvalidParameterException ex) {
            logger.debug(ex.getMessage(), ex);
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public IssuedCertificatesResponseDTO getCertificateEntries(final int page, final int size,
                                                               final Direction direction, final String sortField) {
        logger.debug("getCertificateEntries started...");
        try {
            final int validatedPage = Math.max(page, 0);
            final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
            final Direction validatedDirection = direction == null ? Direction.ASC : direction;
            final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID
                    : sortField.trim();

            if (!CaCertificate.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
                throw new InvalidParameterException(
                        "Sortable field with reference '" + validatedSortField + "' is not available");
            }

            final PageRequest request = PageRequest.of(validatedPage, validatedSize, validatedDirection,
                    validatedSortField);
            final Page<CaCertificate> queryResult = caCertificateRepository.findAll(request);
            return DTOConverter.convertCaCertificateListToIssuedCertificatesResponseDTO(queryResult);
        } catch (final InvalidParameterException ex) {
            logger.debug(ex.getMessage(), ex);
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public CertificateCheckResponseDTO isCertificateValidNow(final BigInteger serial) {
        logger.debug("isCertificateValidNow started...");

        if (serial == null) {
            throw new InvalidParameterException("serial cannot be null");
        }
        try {
            final Optional<CaCertificate> queryResult = caCertificateRepository.findBySerial(serial);

            if (queryResult.isPresent()) {
                final CaCertificate cert = queryResult.get();
                final ZonedDateTime now = ZonedDateTime.now();
                final CertificateCheckResponseDTO responseDTO = new CertificateCheckResponseDTO();
                responseDTO.setVersion(1);
                responseDTO.setProducedAt(Utilities.convertZonedDateTimeToUTCString(now));
                responseDTO.setCommonName(cert.getCommonName());
                responseDTO.setSerialNumber(cert.getSerial());
                responseDTO.setStatus(IssuedCertificateStatus.UNKNOWN);

                if (now.isAfter(cert.getValidAfter()) && now.isBefore(cert.getValidBefore())) {
                    if (cert.getRevokedAt() != null) {
                        responseDTO.setStatus(IssuedCertificateStatus.REVOKED);
                        responseDTO.setEndOfValidity(Utilities.convertZonedDateTimeToUTCString(cert.getRevokedAt()));
                    } else {
                        responseDTO.setStatus(IssuedCertificateStatus.GOOD);
                        responseDTO.setEndOfValidity(Utilities.convertZonedDateTimeToUTCString(cert.getValidBefore()));
                    }
                } else {
                    responseDTO.setEndOfValidity(Utilities.convertZonedDateTimeToUTCString(now));
                }

                return responseDTO;
            } else {
                throw new DataNotFoundException("Cannot find trusted certificate");
            }
        } catch (DataNotFoundException ex) {
            logger.debug(ex.getMessage(), ex);
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public boolean revokeCertificate(long id, String createdBy) {
        logger.debug("revokeCertificate started...");

        if (Utilities.isEmpty(createdBy)) {
            throw new InvalidParameterException("createdBy cannot be null");
        }
        try {
            return (caCertificateRepository.setRevokedById(id, ZonedDateTime.now(), createdBy) == 1);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
}

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
import eu.arrowhead.common.database.entity.CaTrustedKey;
import eu.arrowhead.common.database.repository.CaTrustedKeyRepository;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.AddTrustedKeyResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.certificate_authority.CertificateAuthorityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class CATrustedKeyDBService {

    @Autowired
    private CaTrustedKeyRepository caTrustedKeyRepository;

    private final Logger logger = LogManager.getLogger(CATrustedKeyDBService.class);

    @Transactional(rollbackFor = ArrowheadException.class)
    public TrustedKeysResponseDTO getTrustedKeyEntries(final int page, final int size, final Direction direction,
                                                       final String sortField) {
        logger.debug("getTrustedKeyEntries started...");

        final int validatedPage = Math.max(page, 0);
        final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
        final Direction validatedDirection = direction == null ? Direction.ASC : direction;
        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID
                : sortField.trim();

        if (!CaTrustedKey.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException(
                    "Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            final PageRequest request = PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
            final Page<CaTrustedKey> queryResult = caTrustedKeyRepository.findAll(request);
            return DTOConverter.convertCaTrustedKeyListToTrustedKeysResponseDTO(queryResult);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public AddTrustedKeyResponseDTO addTrustedKey(AddTrustedKeyRequestDTO request) {
        logger.debug("addTrustedKey started...");
        try {
            if (request == null) {
                throw new InvalidParameterException("request cannot be null");
            }
            if (Utilities.isEmpty(request.getPublicKey())) {
                throw new InvalidParameterException("public key cannot be null");
            }
            if (Utilities.isEmpty(request.getDescription())) {
                throw new InvalidParameterException("description cannot be null");
            }

            final String validAfterString = request.getValidAfter();
            final String validBeforeString = request.getValidBefore();
            if (Utilities.isEmpty(validAfterString)) {
                throw new InvalidParameterException("validAfter cannot be empty");
            }
            if (Utilities.isEmpty(validBeforeString)) {
                throw new InvalidParameterException("validBefore cannot be empty");
            }
            final ZonedDateTime validAfter = Utilities.parseUTCStringToLocalZonedDateTime(validAfterString);
            final ZonedDateTime validBefore =  Utilities.parseUTCStringToLocalZonedDateTime(validBeforeString);
            if (validAfter.isAfter(validBefore)) {
                throw new InvalidParameterException("Invalid validity range: validAfter must have a value before validBefore");
            }

            final CaTrustedKey trustedKey = new CaTrustedKey();
            trustedKey.setPublicKey(request.getPublicKey());
            trustedKey.setHash(CertificateAuthorityUtils.sha256(request.getPublicKey()));
            trustedKey.setDescription(request.getDescription());
            trustedKey.setValidAfter(validAfter);
            trustedKey.setValidBefore(validBefore);

            final ZonedDateTime now = ZonedDateTime.now();
            trustedKey.setCreatedAt(now);
            trustedKey.setUpdatedAt(now);

            logger.info("Adding a trusted key: " + trustedKey.getDescription());

            final CaTrustedKey caTrustedKey = caTrustedKeyRepository.saveAndFlush(trustedKey);

            return new AddTrustedKeyResponseDTO(caTrustedKey.getId(),
                    Utilities.convertZonedDateTimeToUTCString(caTrustedKey.getValidAfter()),
                    Utilities.convertZonedDateTimeToUTCString(caTrustedKey.getValidBefore()));
        } catch (final InvalidParameterException ex) {
            logger.debug(ex.getMessage(), ex);
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public TrustedKeyCheckResponseDTO isTrustedKeyValidNow(final TrustedKeyCheckRequestDTO request) {
        if (request == null || request.getPublicKey() == null) {
            throw new BadPayloadException("Invalid request for isTrustedKeyValidNow");
        }

        final String hash = CertificateAuthorityUtils.sha256(request.getPublicKey());

        try {
            final Optional<CaTrustedKey> trustedKeyResult = caTrustedKeyRepository.findByHash(hash);

            if (trustedKeyResult.isPresent()) {
                final CaTrustedKey trustedKey = trustedKeyResult.get();
                return new TrustedKeyCheckResponseDTO(trustedKey.getId(),
                        Utilities.convertZonedDateTimeToUTCString(trustedKey.getCreatedAt()),
                        trustedKey.getDescription());
            } else {
                throw new InvalidParameterException("Cannot find trusted certificate");
            }
        } catch (final InvalidParameterException ex) {
            logger.debug(ex.getMessage(), ex);
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public void deleteTrustedKey(long id) {
        if (id < 0) {
            throw new InvalidParameterException("Invalid trusted key id: " + id);
        }
        try {
            caTrustedKeyRepository.deleteById(id);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
}

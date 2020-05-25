package eu.arrowhead.core.certificate_authority.database;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.CaTrustedKey;
import eu.arrowhead.common.database.repository.CaTrustedKeyRepository;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.certificate_authority.CertificateAuthorityUtils;

@Service
public class CATrustedKeyDBService {

    @Autowired
    CaTrustedKeyRepository caTrustedKeyRepository;

    private final Logger logger = LogManager.getLogger(CATrustedKeyDBService.class);

    public TrustedKeysResponseDTO getTrustedKeyEntries(final int page, final int size, final Direction direction,
            final String sortField) {
        logger.debug("getTrustedKeyEntries started...");

        final int validatedPage = page < 0 ? 0 : page;
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

    public void addTrustedKey(AddTrustedKeyRequestDTO request) {
        logger.debug("addTrustedKey started...");

        CaTrustedKey trustedKey = new CaTrustedKey();

        try {
            trustedKey.setPublicKey(request.getPublicKey());
            trustedKey.setHash(CertificateAuthorityUtils.sha256(request.getPublicKey()));
            trustedKey.setDescription(request.getDescription());
            trustedKey.setValidAfter(request.getValidAfter());
            trustedKey.setValidBefore(request.getValidBefore());
        } catch (NullPointerException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new BadPayloadException("Invalid request for addTrustedKey: (" + ex.getMessage() + ")", ex);
        }

        if (trustedKey.getValidAfter().isAfter(trustedKey.getValidBefore())) {
            final String msg = "Invalid validity range: validAfter must have a value before validBefore";
            logger.debug(msg);
            throw new InvalidParameterException(msg);
        }

        ZonedDateTime now = ZonedDateTime.now();
        trustedKey.setCreatedAt(now);
        trustedKey.setUpdatedAt(now);

        logger.info("Adding a trusted key: " + trustedKey.getDescription());

        try {
            caTrustedKeyRepository.saveAndFlush(trustedKey);
        } catch (Exception e) {
            final String msg = "Cannot save Trusted Key";
            logger.debug(msg + ": " + e.getMessage(), e);
            throw new BadPayloadException(msg, e);
        }
    }

    public TrustedKeyCheckResponseDTO isTrustedKeyValidNow(final TrustedKeyCheckRequestDTO request) {
        if (request == null || request.getPublicKey() == null) {
            throw new BadPayloadException("Invalid request for isTrustedKeyValidNow");
        }

        final String hash = CertificateAuthorityUtils.sha256(request.getPublicKey());

        Optional<CaTrustedKey> trustedKeyResult = caTrustedKeyRepository.findByHash(hash);

        if (trustedKeyResult.isPresent()) {
            CaTrustedKey trustedKey = trustedKeyResult.get();
            return new TrustedKeyCheckResponseDTO(trustedKey.getId(), trustedKey.getCreatedAt(),
                    trustedKey.getDescription());
        } else {
            throw new InvalidParameterException("Cannot find trusted certificate");
        }
    }

    public void deleteTrustedKey(long id) {
        if (id < 0) {
            throw new InvalidParameterException("Invalid trusted key id: " + id);
        }
        caTrustedKeyRepository.deleteById(id);
    }
}

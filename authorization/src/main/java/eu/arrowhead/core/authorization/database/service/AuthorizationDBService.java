package eu.arrowhead.core.authorization.database.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.IntraCloudAuthorization;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.InterCloudAuthorizationRepository;
import eu.arrowhead.common.database.repository.IntraCloudAuthorizationRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.IntraCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class AuthorizationDBService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private IntraCloudAuthorizationRepository intraCloudAuthorizationRepository;
	
	@Autowired
	private InterCloudAuthorizationRepository interCloudAuthorizationRepository;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	private final Logger logger = LogManager.getLogger(AuthorizationDBService.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Page<IntraCloudAuthorization> getIntraCloudAuthorizationEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getIntraCloudAuthorizationEntries started..");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!IntraCloudAuthorization.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		try {
			return intraCloudAuthorizationRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationListResponseDTO getIntraCloudAuthorizationEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getIntraCloudAuthorizationEntriesResponse started..");
		final Page<IntraCloudAuthorization> intraCloudAuthorizationEntries = getIntraCloudAuthorizationEntries(page, size, direction, sortField);
		return DTOConverter.convertIntraCloudAuthorizationListToIntraCloudAuthorizationListResponseDTO(intraCloudAuthorizationEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorization getIntraCloudAuthorizationEntryById(final long id) {
		logger.debug("getIntraCloudAuthorizationEntryById started...");
		
		try {
			final Optional<IntraCloudAuthorization> find = intraCloudAuthorizationRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("IntraCloudAuthorization with id of '" + id + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationResponseDTO getIntraCloudAuthorizationEntryByIdResponse(final long id) {
		logger.debug("getIntraCloudAuthorizationByIdEntryResponse started...");		
		final IntraCloudAuthorization intraCloudAuthorizationEntry = getIntraCloudAuthorizationEntryById(id);
		return DTOConverter.convertIntraCloudAuthorizationToIntraCloudAuthorizationResponseDTO(intraCloudAuthorizationEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeIntraCloudAuthorizationEntryById(final long id) {
		logger.debug("removeIntraCloudAuthorizationEntryById started..");
		
		try {
			if (!intraCloudAuthorizationRepository.existsById(id)) {
				throw new InvalidParameterException("IntraCloudAuthorization with id of '" + id + "' not exists");
			}
			intraCloudAuthorizationRepository.deleteById(id);
			intraCloudAuthorizationRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}	
}

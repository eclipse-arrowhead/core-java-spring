package eu.arrowhead.core.authorization.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.IntraCloudAuthorization;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
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
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public IntraCloudAuthorization createIntraCloudAuthorization(final long consumerId, final long providerId, final long serviceDefinitionId) {
		logger.debug("createIntraCloudAuthorization started..");
		
		final boolean consumerIdIsInvalid = consumerId < 1;
		final boolean providerIdIsInvalid = providerId < 1;
		final boolean serviceDefinitionIdIsInvalid = serviceDefinitionId < 1;
		try {
			if (consumerIdIsInvalid || providerIdIsInvalid || serviceDefinitionIdIsInvalid) {
				String exceptionMessage = "Following id parameters are invalid: ";
				exceptionMessage = consumerIdIsInvalid ? exceptionMessage : exceptionMessage + "consumerId" ;
				exceptionMessage = providerIdIsInvalid ? exceptionMessage : exceptionMessage + " providerId";
				exceptionMessage = serviceDefinitionIdIsInvalid ? exceptionMessage : exceptionMessage + " serviceDefinitionId";
				throw new InvalidParameterException(exceptionMessage);
			}	
			
			checkConstraintsOfIntraCloudAuthorizationTable(consumerId, providerId, serviceDefinitionId);
			
			final Optional<System> consumer = systemRepository.findById(consumerId);
			final Optional<System> provider = systemRepository.findById(providerId);
			final Optional<ServiceDefinition> serviceDefinition = serviceDefinitionRepository.findById(serviceDefinitionId);
			if (consumer.isEmpty() || provider.isEmpty() || serviceDefinition.isEmpty()) {
				String exceptionMessage = "Following entities are not availables: ";
				exceptionMessage = consumer.isEmpty() ? exceptionMessage + "consumer with id: " + consumerId : exceptionMessage;
				exceptionMessage = provider.isEmpty() ? exceptionMessage + " provider with id: " + providerId : exceptionMessage;
				exceptionMessage = serviceDefinition.isEmpty() ? exceptionMessage + " serviceDefinition with id: " + serviceDefinitionId : exceptionMessage;
				throw new InvalidParameterException(exceptionMessage);
			}
			
			final IntraCloudAuthorization intraCloudAuthorization = new IntraCloudAuthorization(consumer.get(), provider.get(), serviceDefinition.get());
			return intraCloudAuthorizationRepository.saveAndFlush(intraCloudAuthorization);
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public IntraCloudAuthorizationResponseDTO createIntraCloudAuthorizationResponse(final long consumerId, final long providerId, final long serviceDefinitionId) {
		logger.debug("createIntraCloudAuthorizationResponse started..");
		final IntraCloudAuthorization entry = createIntraCloudAuthorization(consumerId, providerId, serviceDefinitionId);
		return DTOConverter.convertIntraCloudAuthorizationToIntraCloudAuthorizationResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public IntraCloudAuthorizationListResponseDTO createBulkIntraCloudAuthorizationResponse(final long consumerId, final List<Long> providerIds, final List<Long> serviceDefinitionIds) {
		logger.debug("createBulkIntraCloudAuthorizationResponse started..");
		
		if (consumerId < 1) {
			throw new InvalidParameterException("Consumer id can't be null and must be greater than 0.");
		}
		for (final Long id : providerIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("Provider id can't be null and must be greater than 0.");
			}
		}
		for (final Long id : serviceDefinitionIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("SerdviceDefinition id can't be null and must be greater than 0.");
			}
		}
				
		final List<IntraCloudAuthorization> savedEntries = new ArrayList<>(providerIds.size() * serviceDefinitionIds.size());
		for (final Long providerId : providerIds) {
			for (final Long serviceId : serviceDefinitionIds) {
				try {
					final IntraCloudAuthorization savedIntraCloudAuthorization = createIntraCloudAuthorization(consumerId, providerId, serviceId);			
					savedEntries.add(savedIntraCloudAuthorization);
				} catch (final InvalidParameterException ex) {
					logger.debug(ex.getMessage(), ex);
				}
			}
		}
		
		final Page<IntraCloudAuthorization> savedEntriesPage = new PageImpl<IntraCloudAuthorization>(savedEntries);
		return DTOConverter.convertIntraCloudAuthorizationListToIntraCloudAuthorizationListResponseDTO(savedEntriesPage);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfIntraCloudAuthorizationTable(final long consumerId, final long providerId, final long serviceDefinitionId) {
		logger.debug("checkConstraintsOfIntraCloudAuthorizationTable started..");
		
		try {
			final Optional<IntraCloudAuthorization> optional = intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(consumerId, providerId, serviceDefinitionId);
			if (optional.isPresent()) {
				throw new InvalidParameterException("IntraCloudAuthorization entry with this" +  consumerId + ", " + providerId + " and " + serviceDefinitionId + " already exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}

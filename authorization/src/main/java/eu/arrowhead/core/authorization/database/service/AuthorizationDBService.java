package eu.arrowhead.core.authorization.database.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckResponseDTO;
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
		logger.debug("getIntraCloudAuthorizationEntries started...");
		
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
		logger.debug("getIntraCloudAuthorizationEntriesResponse started...");
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
		logger.debug("removeIntraCloudAuthorizationEntryById started...");
		
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
		logger.debug("createIntraCloudAuthorization started...");
		
		final boolean consumerIdIsInvalid = consumerId < 1;
		final boolean providerIdIsInvalid = providerId < 1;
		final boolean serviceDefinitionIdIsInvalid = serviceDefinitionId < 1;
		try {
			if (consumerIdIsInvalid || providerIdIsInvalid || serviceDefinitionIdIsInvalid) {
				String exceptionMessage = "Following id parameters are invalid:";
				exceptionMessage = consumerIdIsInvalid ? exceptionMessage : exceptionMessage + " consumerId," ;
				exceptionMessage = providerIdIsInvalid ? exceptionMessage : exceptionMessage + " providerId,";
				exceptionMessage = serviceDefinitionIdIsInvalid ? exceptionMessage : exceptionMessage + " serviceDefinitionId,";
				exceptionMessage = exceptionMessage.substring(0, exceptionMessage.length()-1);
				throw new InvalidParameterException(exceptionMessage);
			}	
			
			final Optional<System> consumer = systemRepository.findById(consumerId);
			final Optional<System> provider = systemRepository.findById(providerId);
			final Optional<ServiceDefinition> serviceDefinition = serviceDefinitionRepository.findById(serviceDefinitionId);
			if (consumer.isEmpty() || provider.isEmpty() || serviceDefinition.isEmpty()) {
				String exceptionMessage = "Following entities are not available:";
				exceptionMessage = consumer.isEmpty() ? exceptionMessage + " consumer with id: " + consumerId + "," : exceptionMessage;
				exceptionMessage = provider.isEmpty() ? exceptionMessage + " provider with id: " + providerId + "," : exceptionMessage;
				exceptionMessage = serviceDefinition.isEmpty() ? exceptionMessage + " serviceDefinition with id: " + serviceDefinitionId + "," : exceptionMessage;
				exceptionMessage = exceptionMessage.substring(0, exceptionMessage.length()-1);
				throw new InvalidParameterException(exceptionMessage);
			}
			checkConstraintsOfIntraCloudAuthorizationTable(consumer.get(), provider.get(), serviceDefinition.get());
			
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
		logger.debug("createIntraCloudAuthorizationResponse started...");
		final IntraCloudAuthorization entry = createIntraCloudAuthorization(consumerId, providerId, serviceDefinitionId);
		return DTOConverter.convertIntraCloudAuthorizationToIntraCloudAuthorizationResponseDTO(entry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<IntraCloudAuthorization> createBulkIntraCloudAuthorization(final long consumerId, final Set<Long> providerIds, final Set<Long> serviceDefinitionIds) {
		logger.debug("createBulkIntraCloudAuthorization started...");
		
		if (consumerId < 1) {
			throw new InvalidParameterException("Consumer id can't be null and must be greater than 0.");
		}
		if (providerIds == null || providerIds.isEmpty()) {
			throw new InvalidParameterException("providerIds list is empty");
		}
		if (serviceDefinitionIds == null || serviceDefinitionIds.isEmpty()) {
			throw new InvalidParameterException("serviceDefinitionIds list is empty");
		}
		if (providerIds.size() > 1 && serviceDefinitionIds.size() > 1) {
			throw new InvalidParameterException("providerIds list or serviceDefinitionIds list should contain only one element, but both contain more");
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
		
		try {
			
			final Optional<System> consumerOpt = systemRepository.findById(consumerId);
			System consumer;
			if (consumerOpt.isPresent()) {
				consumer = consumerOpt.get();
			} else {
				throw new InvalidParameterException("Consumer system with id of " + consumerId + " not exists");
			}
			
			if (providerIds.size() <= serviceDefinitionIds.size()) {
				
				//Case: One provider with more or one service
				final Long providerId = providerIds.iterator().next();
				final Optional<System> providerOpt = systemRepository.findById(providerId);
				System provider;
				if (providerOpt.isPresent()) {
					final List<IntraCloudAuthorization> toBeSaved = new ArrayList<>(serviceDefinitionIds.size());
					provider = providerOpt.get();
					for (final Long serviceId : serviceDefinitionIds) {
						final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
						ServiceDefinition service;
						if (serviceOpt.isPresent()) {
							service = serviceOpt.get();
							try {
								checkConstraintsOfIntraCloudAuthorizationTable(consumer, provider, service);
								toBeSaved.add(new IntraCloudAuthorization(consumer, provider, service));
							} catch (final InvalidParameterException ex) {
								//not throwing towards as in this bulk operation case should be only a warning
								logger.debug(ex.getMessage());
							}
						} else {
							//not throwing exception as in this bulk operation case should be only a warning
							logger.debug("ServiceDefinition with id of " + serviceId + " not exists");
						}
					}
					final List<IntraCloudAuthorization> savedEntries = intraCloudAuthorizationRepository.saveAll(toBeSaved);
					intraCloudAuthorizationRepository.flush();
					return savedEntries;
				} else {
					throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
				}
				
			} else {
				
				//Case: One service with more or one provider
				final Long serviceId = serviceDefinitionIds.iterator().next();
				final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
				if (serviceOpt.isPresent()) {
					final List<IntraCloudAuthorization> toBeSaved = new ArrayList<>(providerIds.size());
					final ServiceDefinition service = serviceOpt.get();
					for (final Long providerId : providerIds) {
						final Optional<System> providerOpt = systemRepository.findById(providerId);
						if (providerOpt.isPresent()) {
							final System provider = providerOpt.get();
							try {
								checkConstraintsOfIntraCloudAuthorizationTable(consumer, provider, service);
								toBeSaved.add(new IntraCloudAuthorization(consumer, provider, service));
							} catch (final InvalidParameterException ex) {
								//not throwing towards as in this bulk operation case should be only a warning
								logger.debug(ex.getMessage());
							}
						} else {
							//not throwing exception as in this bulk operation case should be only a warning
							logger.debug("Provider system with id of " + providerId + " not exists");
						}
					}
					final List<IntraCloudAuthorization> savedEntries = intraCloudAuthorizationRepository.saveAll(toBeSaved);
					intraCloudAuthorizationRepository.flush();
					return savedEntries;
				} else {
					throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
				}
			}
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public IntraCloudAuthorizationListResponseDTO createBulkIntraCloudAuthorizationResponse(final long consumerId, final Set<Long> providerIds, final Set<Long> serviceDefinitionIds) {
		logger.debug("createBulkIntraCloudAuthorization started...");
		final List<IntraCloudAuthorization> entries = createBulkIntraCloudAuthorization(consumerId, providerIds, serviceDefinitionIds);
		final Page<IntraCloudAuthorization> entryPage = new PageImpl<IntraCloudAuthorization>(entries);
		return DTOConverter.convertIntraCloudAuthorizationListToIntraCloudAuthorizationListResponseDTO(entryPage);
	}
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationCheckResponseDTO checkIntraCloudAuthorizationRequest(final long consumerId, final long serviceDefinitionId, final List<Long> providerIds) {
		logger.debug("checkIntraCloudAuthorizationRequest started...");
				
		final Map<Long, Boolean> providerIdAuthorizationState = new HashMap<>();
		try {
			final boolean isConsumerIdInvalid = consumerId < 1 || !systemRepository.existsById(consumerId);
			final boolean isServiceDefinitionIdInvalid = serviceDefinitionId < 1 || !serviceDefinitionRepository.existsById(serviceDefinitionId);
			final boolean isProviderListEmpty = providerIds == null || providerIds.isEmpty();
			if (isConsumerIdInvalid || isServiceDefinitionIdInvalid || isProviderListEmpty) {
				String exceptionMsg = "Following parameters are invalid:";
				exceptionMsg = isConsumerIdInvalid ? exceptionMsg + " consumer id," : exceptionMsg;
				exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " serviceDefinition id," : exceptionMsg;
				exceptionMsg = isProviderListEmpty ? exceptionMsg + " empty providerId list," : exceptionMsg;
				exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length()-1);
				throw new InvalidParameterException(exceptionMsg);
			}
			
			for (final Long providerId : providerIds) {
				if (providerId == null || providerId < 1 || !systemRepository.existsById(providerId)) {
					logger.debug("Invalid provider id: {}", providerId);
				} else {
					final Optional<IntraCloudAuthorization> optional = intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(consumerId, providerId, serviceDefinitionId);
					providerIdAuthorizationState.put(providerId, optional.isPresent());			
				}
			}
			
			if (providerIdAuthorizationState.isEmpty()) {
				throw new InvalidParameterException("Have no valid id in providerId list");
			}			
			return new IntraCloudAuthorizationCheckResponseDTO(consumerId, serviceDefinitionId, providerIdAuthorizationState);
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfIntraCloudAuthorizationTable(final System consumer, final System provider, final ServiceDefinition serviceDefinition) {
		logger.debug("checkConstraintsOfIntraCloudAuthorizationTable started...");
		
		try {
			final Optional<IntraCloudAuthorization> optional = intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(consumer, provider, serviceDefinition);
			if (optional.isPresent()) {
				throw new InvalidParameterException("IntraCloudAuthorization entry with consumer id" +  consumer.getId() + ", provider id " + provider.getId() + " and serviceDefinitionId" + serviceDefinition.getId() + " already exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}
package eu.arrowhead.core.orchestrator.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestByIdDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class OrchestratorStoreDBService {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	@Autowired
	private OrchestratorStoreRepository orchestratorStoreRepository;
	
	@Autowired
	private ServiceRegistryRepository serviceRegistryRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;

	private static final String LESS_THEN_ONE_ERROR_MESAGE= " must be greater then zero.";
	private static final String NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE = "The following shortable field  is not available : ";
	private static final String NOT_IN_DB_ERROR_MESAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESAGE = " is empty or null";
	private static final String NULL_ERROR_MESAGE = " is null";
	private static final String ORCHESTRATORSTORE_REQUESTBYIDDTO_VALIDATION_EXCEPTION_MESSAGE = "Exception in OrchestratorStoreRequestByIdDTO validation, entry not going to be added to save list." ;
	private static final String VIOLATES_UNIQUECONSTRAINT = " violates uniqueConstraint rules";
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO getOrchestratorStoreById(final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started...");
		
		try {
			final Optional<OrchestratorStore> orchestratorStoreOption = orchestratorStoreRepository.findById(orchestratorStoreId);
			if (orchestratorStoreOption.isEmpty()){
				throw new InvalidParameterException("OrchestratorStore with id " + orchestratorStoreId + " not found.");		
			}		
			return DTOConverter.convertOrchestratorStoreToOrchestratorStoreResponseDTO(orchestratorStoreOption.get());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoreEntriesResponse(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		try {
			return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStoreEntriesResponse(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		try {
			return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAllByPriority(CommonConstants.TOP_PRIORITY, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoresByConsumerResponse(final int page,
			final int size, final Direction direction, final String sortField, final long consumerSystemId,
			final long serviceDefinitionId) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		try {
			
			if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
				throw new InvalidParameterException(NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
			}
			
			if ( consumerSystemId < 1) {
				throw new InvalidParameterException("ConsumerSystemId " + LESS_THEN_ONE_ERROR_MESAGE);
			}
			final Optional<System> consumerOption = systemRepository.findById(consumerSystemId);
				if ( consumerOption.isEmpty() ) {
					throw new InvalidParameterException("ConsumerSystemId " + NOT_IN_DB_ERROR_MESAGE);
				}
					
			if ( serviceDefinitionId < 1) {
				throw new InvalidParameterException("ServiceDefinitionId " + LESS_THEN_ONE_ERROR_MESAGE);
			}
			final Optional<ServiceDefinition> serviceDefinitionOption = serviceDefinitionRepository.findById(serviceDefinitionId);
				if ( serviceDefinitionOption.isEmpty() ) {
					throw new InvalidParameterException("ServiceDefinitionId " + NOT_IN_DB_ERROR_MESAGE);
				}
					
				return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(consumerSystemId, serviceDefinitionId, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

//	//-------------------------------------------------------------------------------------------------	
//	public OrchestratorStoreListResponseDTO createOrchestratorStoresResponse(
//			final List<OrchestratorStoreRequestDTO> request) {
//		logger.debug("createOrchestratorStoresResponse started...");
//		
//		try {
//			final Set<OrchestratorStore> validatedOrchestratorStoreEtrysToSave = validateOrchestratorStoreRequestList(request);
//			
//			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.saveAll(validatedOrchestratorStoreEtrysToSave));
//			
//		} catch (final Exception ex) {
//			logger.debug(ex.getMessage(), ex);
//			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//			
//		}
//		
//	}
	
	//-------------------------------------------------------------------------------------------------	
	public OrchestratorStoreListResponseDTO createOrchestratorStoresByIdResponse(
			final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("createOrchestratorStoresResponse started...");
		
		try {
			final Set<OrchestratorStore> validatedOrchestratorStoreEtrysToSave = validateOrchestratorStoreRequestByIdList(request);
			
			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.saveAll(validatedOrchestratorStoreEtrysToSave));
			
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
	private Set<OrchestratorStore> validateOrchestratorStoreRequestByIdList(
		final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("validateOrchestratorStoreRequestByIdList started...");
		
		if (request == null || request.isEmpty()) {
			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
	
		final List<OrchestratorStore> temporalOrchestratorList = new ArrayList(request.size());
		for (final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO : request) {
			
			final OrchestratorStore validatedOrchestratorStore = validateOrchestratorStoreRequestById(orchestratorStoreRequestByIdDTO);
			if (validatedOrchestratorStore != null) {
				temporalOrchestratorList.add(validatedOrchestratorStore);
			}
		}
		
		return Set.copyOf(temporalOrchestratorList);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore validateOrchestratorStoreRequestById(
			OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO) {
		logger.debug("validateOrchestratorStoreRequestById started...");
		
		try {
			if (orchestratorStoreRequestByIdDTO.getConsumerSystemId() == null) {
				throw new InvalidParameterException("ConsumerSystem " + NULL_ERROR_MESAGE);
			}
			System validConsumerSystem = validateSystemId(orchestratorStoreRequestByIdDTO.getConsumerSystemId()); 
			
			if (orchestratorStoreRequestByIdDTO.getProviderSystemId() == null) {
				throw new InvalidParameterException("ProviderSystem " + NULL_ERROR_MESAGE);
			}
			System validProviderSystem = validateSystemId(orchestratorStoreRequestByIdDTO.getProviderSystemId());
			
			if (orchestratorStoreRequestByIdDTO.getServiceDefinitionId() == null) {
				throw new InvalidParameterException("ServiceDefinition " + NULL_ERROR_MESAGE);
			}
			ServiceDefinition validServiceDefinition = validateServiceDefinitionId(orchestratorStoreRequestByIdDTO.getServiceDefinitionId());
			
			if (orchestratorStoreRequestByIdDTO.getPriority() == null) {
				throw new InvalidParameterException("Priority " + NULL_ERROR_MESAGE);
			} 
			int validPriority = validatePriority(orchestratorStoreRequestByIdDTO.getPriority(), validConsumerSystem.getId(), validServiceDefinition.getId());
			
			checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority(validConsumerSystem.getId(), validServiceDefinition.getId(), validPriority);
			checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId(validConsumerSystem.getId(), validServiceDefinition.getId(), validProviderSystem.getId());

			Cloud validProviderCloud = validateProviderCloudId(orchestratorStoreRequestByIdDTO.getCloudId());
			
			return new OrchestratorStore(
					validServiceDefinition,
					validConsumerSystem,
					validProviderSystem,
					validProviderCloud,
					validPriority,
					orchestratorStoreRequestByIdDTO.getAttribute(),
					null,
					null);
		
		} catch (Exception e) {
			logger.debug( ORCHESTRATORSTORE_REQUESTBYIDDTO_VALIDATION_EXCEPTION_MESSAGE + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private System validateSystemId(Long systemId) {
		logger.debug("validateSystemId started...");
		
		if (systemId == null) {
			throw new InvalidParameterException("System " + NULL_ERROR_MESAGE);
		}
		
		if (systemId < 1) {
			throw new InvalidParameterException("System " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		
		Optional<System> systemOptional = systemRepository.findById(systemId);
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by id" + systemId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return systemOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition validateServiceDefinitionId(Long serviceDefinitionId) {
		logger.debug("validateServiceDefinitionId started...");
		
		if (serviceDefinitionId == null) {
			throw new InvalidParameterException("ServiceDefinition " + NULL_ERROR_MESAGE);
		}
		
		if (serviceDefinitionId < 1) {
			throw new InvalidParameterException("ServiceDefinition " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		
		Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findById(serviceDefinitionId);
		if (serviceDefinitionOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinition by id: " + serviceDefinitionId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return serviceDefinitionOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority(long consumerSystemId, long serviceDefinitionId, int priority) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority started...");
		
		Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerIdAndServiceDefinitionIdAndPriority( consumerSystemId, serviceDefinitionId, priority);
		if (orchestratorStoreOptional.isPresent()) {
			throw new InvalidParameterException("OrchestratorStore checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority " + VIOLATES_UNIQUECONSTRAINT );
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private void checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId(long consumerSystemId, long serviceDefinitionId, long providerSystemId) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId started...");
		
		Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerIdAndServiceDefinitionIdAndProviderId( consumerSystemId, serviceDefinitionId, providerSystemId);
		if (orchestratorStoreOptional.isPresent()) {
			throw new InvalidParameterException("OrchestratorStore checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId " + VIOLATES_UNIQUECONSTRAINT );
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private Cloud validateProviderCloudId(Long cloudId) {
		logger.debug("validateProviderCloudId started...");
		
		if (cloudId == null) {
			return null;
		}

		Optional<Cloud> cloudOptional = cloudRepository.findById(cloudId);
		if (cloudOptional.isEmpty()) {
			throw new InvalidParameterException("Cloud by id :" + cloudId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return cloudOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private int validatePriority(Integer priority, long consumerSystemId, long serviceDefinitionId ) {
		logger.debug("validatePriority started...");
		
		if(priority != null && priority < 1) {
			throw new InvalidParameterException("Priority " + LESS_THEN_ONE_ERROR_MESAGE );
		}
		
		Optional<List<OrchestratorStore>> orchestratorStoreOptionalList = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(consumerSystemId, serviceDefinitionId);
		if (orchestratorStoreOptionalList.isEmpty()) {
			return CommonConstants.TOP_PRIORITY;
		}else {
			return orchestratorStoreOptionalList.get().size() + 1;	
		}		
	}
	
//	//-------------------------------------------------------------------------------------------------	
//	private Set<OrchestratorStore> validateOrchestratorStoreRequestList(
//			final List<OrchestratorStoreRequestDTO> request) {
//		logger.debug("validateOrchestratorStoreRequestList started...");
//		
//		if (request == null || request.isEmpty()) {
//			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
//		}
//		
//		final List<OrchestratorStore> temporalOrchestratorList = new ArrayList(request.size());
//		for (final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO : request) {
//			
//			final OrchestratorStore validatedOrchestratorStore = validateOrchestratorStoreRequest(orchestratorStoreRequestDTO);
//			temporalOrchestratorList.add(validatedOrchestratorStore);
//		}
//		
//		return Set.copyOf(temporalOrchestratorList);
//	}

//	//-------------------------------------------------------------------------------------------------
//	
//	private OrchestratorStore validateOrchestratorStoreRequest(
//			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO) {
//		logger.debug("validateOrchestratorStoreRequest started ...");
//		
//		if (orchestratorStoreRequestDTO.getConsumerSystemDTO == null) {
//			throw new InvalidParameterException("ConsumerSystem " + NULL_ERROR_MESAGE);
//		}
//		System validConsumerSystem = validateSystem(orchestratorStoreRequestDTO.getConsumerSystemDTO()); 
//		
//		if (orchestratorStoreRequestDTO.getProviderSystemDTO() == null) {
//			throw new InvalidParameterException("ProviderSystem " + NULL_ERROR_MESAGE);
//		}
//		System validProviderSystem = validateSystem(orchestratorStoreRequestDTO.getProviderSystemDTO());
//		
//		if (orchestratorStoreRequestDTO.getServiceDefinition() == null) {
//			throw new InvalidParameterException("ServiceDefinition " + NULL_ERROR_MESAGE);
//		}
//		ServiceDefinition validatedServiceDefinition = validateServiceDefinition(orchestratorStoreRequestDTO.getServiceDefinition());
//		
//		if (orchestratorStoreRequestDTO.getPriority() == null) {
//			throw new InvalidParameterException("Priority " + NULL_ERROR_MESAGE);
//		} 
//		int validatedPriority = orchestratorStoreRequestDTO.getPriority();
//		
//		
//		
//		return null;
//	}
//
//	//-------------------------------------------------------------------------------------------------
//	private System validateSystem(final SystemRequestDTO systemDTO) {
//		logger.debug("validateSystem started ...");
//		
//		if (Utilities.isEmpty(systemDTO.getSystemName())) {
//			throw new InvalidParameterException("System name is null or empty");
//		}
//		
//		if (Utilities.isEmpty(systemDTO.getAddress())) {
//			throw new InvalidParameterException("System address is null or empty");
//		}
//		
//		if () {
//			
//		}
//		if (port. < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
//			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
//		}
//		
//		final String validatedSystemName = systemName.trim().toLowerCase();
//		final String validatedAddress = address.trim().toLowerCase();
//		final String validatedAuthenticationInfo = authenticationInfo;
//		
//		checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);
//		
//		return new System(validatedSystemName, validatedAddress, port, validatedAuthenticationInfo);
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	private void checkConstraintsOfSystemTable(final String validatedSystemName, final String validatedAddress, final int validatedPort) {
//		logger.debug("checkConstraintsOfSystemTable started...");
//		
//		try {
//			final Optional<System> find = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedAddress, validatedPort);
//			if (find.isPresent()) {
//				throw new InvalidParameterException("System with name: " + validatedSystemName + ", address: " + validatedAddress +	", port: " + validatedPort + " already exists.");
//			}
//		} catch (final InvalidParameterException ex) {
//			throw ex;
//		} catch (final Exception ex) {
//			logger.debug(ex.getMessage(), ex);
//			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//		}
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	private System validateNonNullSystemParameters(final String systemName, final String address, final int port, final String authenticationInfo) {
//		logger.debug("validateNonNullSystemParameters started...");
//		
//		if (Utilities.isEmpty(systemName)) {
//			throw new InvalidParameterException("System name is null or empty");
//		}
//		
//		if (Utilities.isEmpty(address)) {
//			throw new InvalidParameterException("System address is null or empty");
//		}
//		
//		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
//			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
//		}
//		
//		final String validatedSystemName = systemName.trim().toLowerCase();
//		final String validatedAddress = address.trim().toLowerCase();
//		final String validatedAuthenticationInfo = authenticationInfo;
//		
//		checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);
//		
//		return new System(validatedSystemName, validatedAddress, port, validatedAuthenticationInfo);
//	}
	
}

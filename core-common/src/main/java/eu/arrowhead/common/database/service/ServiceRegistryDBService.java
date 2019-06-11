package eu.arrowhead.common.database.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemListResponseDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class ServiceRegistryDBService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ServiceRegistryRepository serviceRegistryRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Autowired
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Autowired
	private ServiceRegistryInterfaceConnectionRepository serviceRegistryInterfaceConnectionRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	private final Logger logger = LogManager.getLogger(ServiceRegistryDBService.class);
	
	private static final String COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE = "Could not delete System, with given parameters";
	private static final String PORT_RANGE_ERROR_MESSAGE = "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getSystemById(final long systemId) {		
		logger.debug(" getSystemById started ...");
		
		final Optional<System> systemOption = systemRepository.findById(systemId);
		
		if (!systemOption.isPresent()){
			throw new InvalidParameterException("");		
		}
		
		try {
			return DTOConverter.convertSystemToSystemResponseDTO(systemOption.get());			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public SystemListResponseDTO getSystemEntries(final int page, final int size, final Direction direction, final String sortField) {		
		logger.debug(" getSystemEntries started ...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return DTOConverter.convertSystemEntryListToSystemListResponseDTO(systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public System createSystem(final String systemName, final String address, final int port, final String authenticationInfo) {		
		logger.debug(" createSystem started ...");
		
		final System system = validateNonNullSystemParameters(systemName, address, port, authenticationInfo);
		
		try {
			return systemRepository.saveAndFlush(system);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public SystemResponseDTO createSystemResponse(final String validatedSystemName, final String validatedAddress, final Integer validatedPort,
			final String validatedAuthenticationInfo) {
		logger.debug(" createSystemResponse started ...");
		
		return DTOConverter.convertSystemToSystemResponseDTO(createSystem(validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public SystemResponseDTO updateSystemResponse(final long validatedSystemId, final String validatedSystemName, final String validatedAddress,
												  final int validatedPort, final String validatedAuthenticationInfo) {
		return DTOConverter.convertSystemToSystemResponseDTO(updateSystem(validatedSystemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public System updateSystem(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo) {	
		logger.debug(" updateSystem started ...");
		
		final long validatedSystemId = validateSystemId(systemId);
		final int validatedPort = validateSystemPort(port);
		final String validatedSystemName = validateSystemParamString(systemName);
		final String validatedAddress = validateSystemParamString(address);
		final String validatedAuthenticationInfo = authenticationInfo;
		
		final Optional<System> systemOptional = systemRepository.findById(validatedSystemId);
		if (!systemOptional.isPresent()) {
			throw new InvalidParameterException("No system with id : " + validatedSystemId);
		}
		
		final System system = systemOptional.get();
		
		if (checkSystemIfUniqueValidationNeeded(system, validatedSystemName, validatedAddress, validatedPort)) {
			checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, validatedPort);
		}
		
		system.setSystemName(validatedSystemName);
		system.setAddress(validatedAddress);
		system.setPort(validatedPort);
		system.setAuthenticationInfo(validatedAuthenticationInfo);
		
		try {
			return systemRepository.saveAndFlush(system);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeSystemById(final long id) {		
		logger.debug(" removeSystemById started ...");
		
		if (!systemRepository.existsById(id)) {
			throw new InvalidParameterException(COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE);
		}	
		try {
			systemRepository.deleteById(id);
			systemRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public SystemResponseDTO mergeSystemResponse(final long validatedSystemId, final String validatedSystemName,
												 final String validatedAddress, final Integer validatedPort, final String validatedAuthenticationInfo) {		
		logger.debug(" mergeSystemResponse started ...");

		return DTOConverter.convertSystemToSystemResponseDTO(mergeSystem(validatedSystemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public System mergeSystem(final long systemId, final String systemName, final String address, final Integer port, final String authenticationInfo) {		
		logger.debug(" mergeSystem started ...");
		
		final long validatedSystemId = validateSystemId(systemId);
		final Integer validatedPort = validateAllowNullSystemPort(port);
		final String validatedSystemName = validateAllowNullSystemParamString(systemName);
		final String validatedAddress = validateAllowNullSystemParamString(address);
		final String validatedAuthenticationInfo = authenticationInfo;
		
		final Optional<System> systemOptional = systemRepository.findById(validatedSystemId);
		if (!systemOptional.isPresent()) {
			throw new InvalidParameterException("No system with id : " + validatedSystemId);
		}
		
		final System system = systemOptional.get();
		
		if (checkSystemIfUniqueValidationNeeded(system, validatedSystemName, validatedAddress, validatedPort)) {
			checkConstraintsOfSystemTable(validatedSystemName != null ? validatedSystemName : system.getSystemName(),
										  validatedAddress != null ? validatedAddress : system.getAddress(),
										  validatedPort != null ? validatedPort.intValue() : system.getPort());
		}
		
		if (!Utilities.isEmpty(validatedSystemName)) {
			system.setSystemName(validatedSystemName);
		}
		
		if (!Utilities.isEmpty(validatedAddress)) {
			system.setAddress(validatedAddress);
		}
		
		if (validatedPort != null) {
			system.setPort(validatedPort);
		}
		
		if (!Utilities.isEmpty(validatedAuthenticationInfo)) {
			system.setAuthenticationInfo(validatedAuthenticationInfo);
		}
		
		try {
			return systemRepository.saveAndFlush(system);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinition getServiceDefinitionById(final long id) {
		logger.debug("getServiceDefinitionById started...");
		
		try {
			final Optional<ServiceDefinition> find = serviceDefinitionRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("Service definition with id of '" + id + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionResponseDTO getServiceDefinitionByIdResponse(final long id) {
		logger.debug("getServiceDefinitionByIdResponse started..");
		
		final ServiceDefinition serviceDefinitionEntry = getServiceDefinitionById(id);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<ServiceDefinition> getServiceDefinitionEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceDefinitionEntries started..");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!ServiceDefinition.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return serviceDefinitionRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
		
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionsListResponseDTO getServiceDefinitionEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceDefinitionEntriesResponse started..");
		final Page<ServiceDefinition> serviceDefinitionEntries = getServiceDefinitionEntries(page, size, direction, sortField);
		return DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinition createServiceDefinition (final String serviceDefinition) {
		logger.debug("createServiceDefinition started..");
		
		if (Utilities.isEmpty(serviceDefinition)) {
			throw new InvalidParameterException("serviceDefinition is null or blank");
		}
		
		final String validatedServiceDefinition = serviceDefinition.trim().toLowerCase();
		checkConstraintsOfServiceDefinitionTable(validatedServiceDefinition);
		final ServiceDefinition serviceDefinitionEntry = new ServiceDefinition(validatedServiceDefinition);
		try {
			return serviceDefinitionRepository.saveAndFlush(serviceDefinitionEntry);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinitionResponseDTO createServiceDefinitionResponse (final String serviceDefinition) {
		logger.debug("createServiceDefinitionResponse started..");
		
		final ServiceDefinition serviceDefinitionEntry = createServiceDefinition(serviceDefinition);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}
		
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinition updateServiceDefinitionById(final long id, final String serviceDefinition) {
		logger.debug("updateServiceDefinitionById started..");
		
		try {
			if (Utilities.isEmpty(serviceDefinition)) {
				throw new InvalidParameterException("serviceDefinition is null or blank");
			}
			
			final Optional<ServiceDefinition> find = serviceDefinitionRepository.findById(id);
			if (find.isPresent()) {				
				final String validatedServiceDefinition = serviceDefinition.trim().toLowerCase();
				final ServiceDefinition serviceDefinitionEntry = find.get();
				if (!validatedServiceDefinition.equals(serviceDefinitionEntry.getServiceDefinition())) {
					checkConstraintsOfServiceDefinitionTable(validatedServiceDefinition);
				}
				serviceDefinitionEntry.setServiceDefinition(validatedServiceDefinition);
				return serviceDefinitionRepository.saveAndFlush(serviceDefinitionEntry);
			} else {
				throw new InvalidParameterException("Service definition with id of '" + id + "' not exists");
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
	public ServiceDefinitionResponseDTO updateServiceDefinitionByIdResponse(final long id, final String serviceDefinition) {
		logger.debug("updateServiceDefinitionByIdResponse started..");
		
		final ServiceDefinition serviceDefinitionEntry = updateServiceDefinitionById(id, serviceDefinition);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeServiceDefinitionById(final long id) {
		logger.debug("removeServiceDefinitionById started..");

		if (!serviceDefinitionRepository.existsById(id)) {
			throw new InvalidParameterException("Service Definition with id '" + id + "' not exists");
		}
		
		try {
			serviceDefinitionRepository.deleteById(id);
			serviceDefinitionRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<ServiceRegistry> getServiceReqistryEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAllServiceReqistryEntries started..");
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = sortField == null ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (! ServiceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		try {
			return serviceRegistryRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeServiceRegistryEntryById(final long id) {
		logger.debug("removeServiceRegistryEntryById started..");
		if (!serviceRegistryRepository.existsById(id)) {
			throw new InvalidParameterException("Service Definition with id '" + id + "' not exists");
		}
		
		try {
			serviceRegistryRepository.deleteById(id);
			serviceRegistryRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeBulkOfServiceRegistryEntries(final Iterable<ServiceRegistry> entities) {
		logger.debug("removeBulkOfServiceRegistryEntries started..");
		try {
			serviceRegistryRepository.deleteInBatch(entities);
			serviceRegistryRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistryResponseDTO registerServiceResponse(final ServiceRegistryRequestDTO request) {
		Assert.notNull(request, "request is null.");
		//TODO: continue 
		checkServiceRegistryRequest(request);
		
		final String validatedServiceDefinition = request.getServiceDefinition().toLowerCase().trim();
		final String validatedProviderName = request.getProviderSystem().getSystemName().toLowerCase().trim();
		final String validatedProviderAddress = request.getProviderSystem().getAddress().toLowerCase().trim();
		final int validatedProviderPort = request.getProviderSystem().getPort().intValue();
		try {
			final Optional<ServiceDefinition> optServiceDefinition = serviceDefinitionRepository.findByServiceDefinition(validatedServiceDefinition);
			final ServiceDefinition serviceDefinition = optServiceDefinition.isPresent() ? optServiceDefinition.get() : createServiceDefinition(validatedServiceDefinition);
			
			final Optional<System> optProvider = systemRepository.findBySystemNameAndAddressAndPort(validatedProviderName, validatedProviderAddress, validatedProviderPort);
			final System provider = optProvider.isPresent() ? optProvider.get() : 
														createSystem(validatedProviderName, validatedProviderAddress, validatedProviderPort, request.getProviderSystem().getAuthenticationInfo());
														
			//TODO: continue 

			return null; //TODO: delete this
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private boolean checkSystemIfUniqueValidationNeeded(final System system, final String validatedSystemName, final String validatedAddress,
												  final Integer validatedPort) {		
		logger.debug(" removeSystemById started ...");
		
		final String actualSystemName = system.getSystemName();
		final String actualAddress = system.getAddress();
		final int actualPort = system.getPort();
		
		if (validatedSystemName != null && !actualSystemName.equalsIgnoreCase(validatedSystemName)) {
			return true;
		}
		
		if (validatedAddress != null && !actualAddress.equalsIgnoreCase(validatedAddress)) {
			return true;
		}
		
		if (validatedPort != null && actualPort != validatedPort.intValue()) {
			return true;
		}
		
		return false;	
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfSystemTable(final String validatedSystemName, final String validatedAddress, final int validatedPort) {
		logger.debug(" checkConstraintsOfSystemTable started ...");
		
		try {
			final Optional<System> find = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedAddress, validatedPort);
			if (find.isPresent()) {
				throw new InvalidParameterException("Service with name: " + validatedSystemName +
													", address: " + validatedAddress +
													", port: " + validatedPort + 
													" already exists.");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private System validateNonNullSystemParameters(final String systemName, final String address, final int port, final String authenticationInfo) {
		logger.debug(" validateNonNullSystemParameters started ...");
		
		if (Utilities.isEmpty(systemName)) {
			throw new  InvalidParameterException("System name is null or empty");
		}
		
		if (Utilities.isEmpty(address)) {
			throw new  InvalidParameterException("System address is null or empty");
		}
		
		final int validatedPort = port;
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
		}
		
		final String validatedSystemName = systemName.trim().toLowerCase();
		final String validatedAddress = address.trim().toLowerCase();
		final String validatedAuthenticationInfo = authenticationInfo;
		
		checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, validatedPort);
		
		return new System(validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}

	//-------------------------------------------------------------------------------------------------
	private String validateSystemParamString(final String param) {
		logger.debug(" validateSystemParamString started ...");
		
		if (Utilities.isEmpty(param)) {
			throw new InvalidParameterException("parameter null or empty");
		}
		
		return param.trim().toLowerCase();
	}

	//-------------------------------------------------------------------------------------------------
	private int validateSystemPort(final int port) {
		logger.debug(" validateSystemPort started ...");
		
		final int validatedPort = port;
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
		}
		
		return validatedPort;
	}

	//-------------------------------------------------------------------------------------------------
	private long validateSystemId(final long systemId) {
		logger.debug(" validateSystemId started ...");
		
		if (systemId < 1) {
			throw new IllegalArgumentException("System id must be greater then null");
		}
		
		return systemId;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Integer validateAllowNullSystemPort(final Integer port) {
		logger.debug(" validateAllowNullSystemPort started ...");
		
		final Integer validatedPort = port;
		if (validatedPort != null && (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX)) {
			throw new IllegalArgumentException(PORT_RANGE_ERROR_MESSAGE);
		}
		
		return validatedPort;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String validateAllowNullSystemParamString(final String param) {
		logger.debug(" validateAllowNullSystemParamString started ...");
		
		if (Utilities.isEmpty(param)) {
			return null;
		}
		
		return param.trim().toLowerCase();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfServiceDefinitionTable(final String serviceDefinition) {
		logger.debug(" checkConstraintsOfServiceDefinitionTable started ...");
		
		try {
			final Optional<ServiceDefinition> find = serviceDefinitionRepository.findByServiceDefinition(serviceDefinition);
			if (find.isPresent()) {
				throw new InvalidParameterException(serviceDefinition + " definition already exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryRequest(final ServiceRegistryRequestDTO request) {
		Assert.isTrue(!Utilities.isEmpty(request.getServiceDefinition()), "Service definition is not specified.");
		Assert.notNull(request.getProviderSystem(), "Provider system is not specified.");
		Assert.isTrue(!Utilities.isEmpty(request.getProviderSystem().getSystemName()), "Provider system name is not specified.");
		Assert.isTrue(!Utilities.isEmpty(request.getProviderSystem().getAddress()), "Provider system address is not specified.");
		Assert.notNull(request.getProviderSystem().getPort(), "Provider system port is not specified.");
		Assert.notNull(request.getInterfaces(), "Interfaces list is not specified.");
		Assert.isTrue(!request.getInterfaces().isEmpty(), "Interfaces is is empty.");
		
	}
}
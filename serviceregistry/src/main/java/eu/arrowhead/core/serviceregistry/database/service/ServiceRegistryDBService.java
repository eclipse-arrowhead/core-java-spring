package eu.arrowhead.core.serviceregistry.database.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryGrouppedResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemListResponseDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.serviceregistry.intf.ServiceInterfaceNameVerifier;

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
	
	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;
	
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
		logger.debug("getServiceReqistryEntries started..");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = sortField == null ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (!ServiceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
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
	public ServiceRegistryListResponseDTO getServiceReqistryEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceReqistryEntriesResponse started..");
		
		final Page<ServiceRegistry> serviceReqistryEntries = getServiceReqistryEntries(page, size, direction, sortField);
		return DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceReqistryEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryGrouppedResponseDTO getServiceReqistryEntriesForServiceRegistryGrouppedResponse() {
		logger.debug("getServiceReqistryEntriesForAutoCompleteDataResponse started..");
		
		final Page<ServiceRegistry> serviceReqistryEntries = getServiceReqistryEntries(-1, -1, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
		return DTOConverter.convertServiceRegistryEntriesToServiceRegistryGrouppedResponseDTO(serviceReqistryEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<ServiceRegistry> getServiceReqistryEntriesByServiceDefintion(final String serviceDefinition, final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceReqistryEntriesByServiceDefintion started..");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = sortField == null ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (!ServiceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(serviceDefinition);
			if (serviceDefinitionOptional.isPresent()) {
				return serviceRegistryRepository.findAllByServiceDefinition(serviceDefinitionOptional.get(), PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
			} else {
				throw new InvalidParameterException("Service with definition of '" + serviceDefinition + "'is not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceReqistryEntriesByServiceDefintionResponse(final String serviceDefinition, final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceReqistryEntriesByServiceDefintionResponse started..");
		
		final Page<ServiceRegistry> serviceReqistryEntriesByServiceDefintion = getServiceReqistryEntriesByServiceDefintion(serviceDefinition, page, size, direction, sortField);
		return DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceReqistryEntriesByServiceDefintion);		
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
		logger.debug("registerServiceResponse started...");
		Assert.notNull(request, "request is null.");
		checkServiceRegistryRequest(request);
		
		final String validatedServiceDefinition = request.getServiceDefinition().toLowerCase().trim();
		final String validatedProviderName = request.getProviderSystem().getSystemName().toLowerCase().trim();
		final String validatedProviderAddress = request.getProviderSystem().getAddress().toLowerCase().trim();
		final int validatedProviderPort = request.getProviderSystem().getPort().intValue();
		try {
			final Optional<ServiceDefinition> optServiceDefinition = serviceDefinitionRepository.findByServiceDefinition(validatedServiceDefinition);
			final ServiceDefinition serviceDefinition = optServiceDefinition.isPresent() ? optServiceDefinition.get() : createServiceDefinition(validatedServiceDefinition);
			
			final Optional<System> optProvider = systemRepository.findBySystemNameAndAddressAndPort(validatedProviderName, validatedProviderAddress, validatedProviderPort);
			System provider;
			if (optProvider.isPresent()) {
				provider = optProvider.get();
				if (!Objects.equals(request.getProviderSystem().getAuthenticationInfo(), provider.getAuthenticationInfo())) { // authentication info has changed
					provider.setAuthenticationInfo(request.getProviderSystem().getAuthenticationInfo());
					provider = systemRepository.saveAndFlush(provider);
				}
			} else {
				provider = createSystem(validatedProviderName, validatedProviderAddress, validatedProviderPort, request.getProviderSystem().getAuthenticationInfo());
			}
														
			final ZonedDateTime endOfValidity = Utilities.isEmpty(request.getEndOfValidity()) ? null : ZonedDateTime.parse(request.getEndOfValidity().trim());
			final String metadataStr = Utilities.map2Text(request.getMetadata());
			final int version = request.getVersion() == null ? 1 : request.getVersion().intValue();
			final ServiceRegistry srEntry = createServiceRegistry(serviceDefinition, provider, request.getServiceUri(), endOfValidity, request.getSecure(), metadataStr, version,
																  request.getInterfaces());
		
			return DTOConverter.convertServiceRegistryToServiceRegistryResponseDTO(srEntry);
		} catch (final DateTimeParseException ex) {
			logger.debug(ex.getMessage(), ex);
			throw new InvalidParameterException("End of validity is specified in the wrong format. See java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME for details.", ex);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistry createServiceRegistry(final ServiceDefinition serviceDefinition, final System provider, final String serviceUri, final ZonedDateTime endOfValidity,
												 final ServiceSecurityType securityType, final String metadataStr, final int version, final List<String> interfaces) {
		logger.debug("createServiceRegistry started...");
		Assert.notNull(serviceDefinition, "Service definition is not specified.");
		Assert.notNull(provider, "Provider is not specified.");
		
		checkConstraintOfSystemRegistryTable(serviceDefinition, provider);
		checkSRSecurityValue(securityType, provider.getAuthenticationInfo());
		checkSRServiceInterfacesList(interfaces);
		
		try {
			final ServiceSecurityType secure = securityType == null ? ServiceSecurityType.NOT_SECURE : securityType;
			final String validatedServiceUri = Utilities.isEmpty(serviceUri) ? null : serviceUri.trim();
			final ServiceRegistry srEntry = serviceRegistryRepository.save(new ServiceRegistry(serviceDefinition, provider, validatedServiceUri, endOfValidity, secure, metadataStr, version));
			final List<ServiceInterface> serviceInterfaces = findOrCreateServiceInterfaces(interfaces);
			for (final ServiceInterface serviceInterface : serviceInterfaces) {
				final ServiceRegistryInterfaceConnection connection = new ServiceRegistryInterfaceConnection(srEntry, serviceInterface);
				srEntry.getInterfaceConnections().add(connection);
			}
			serviceRegistryInterfaceConnectionRepository.saveAll(srEntry.getInterfaceConnections());
			
			return serviceRegistryRepository.saveAndFlush(srEntry);
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
				throw new InvalidParameterException("System with name: " + validatedSystemName +
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
		logger.debug("checkServiceRegistryRequest started...");
		Assert.isTrue(!Utilities.isEmpty(request.getServiceDefinition()), "Service definition is not specified.");
		Assert.notNull(request.getProviderSystem(), "Provider system is not specified.");
		Assert.isTrue(!Utilities.isEmpty(request.getProviderSystem().getSystemName()), "Provider system name is not specified.");
		Assert.isTrue(!Utilities.isEmpty(request.getProviderSystem().getAddress()), "Provider system address is not specified.");
		Assert.notNull(request.getProviderSystem().getPort(), "Provider system port is not specified.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSRServiceInterfacesList(final List<String> interfaces) {
		logger.debug("checkSRServiceInterfacesList started...");
		Assert.notNull(interfaces, "Interfaces list is not specified.");
		Assert.isTrue(!interfaces.isEmpty(), "Interfaces is is empty.");
		
		for (final String intf : interfaces) {
			Assert.isTrue(interfaceNameVerifier.isValid(intf), "Specified interface name is not valid: " + intf);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSRSecurityValue(final ServiceSecurityType type, final String providerSystemAuthenticationInfo) {
		logger.debug("checkSRSecurityValue started...");
		final ServiceSecurityType validatedType = type == null ? ServiceSecurityType.NOT_SECURE : type;
		Assert.isTrue((validatedType == ServiceSecurityType.NOT_SECURE && providerSystemAuthenticationInfo == null) ||
					  (validatedType != ServiceSecurityType.NOT_SECURE && providerSystemAuthenticationInfo != null), 
					  "Security type is in conflict with the availability of the authentication info.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkConstraintOfSystemRegistryTable(final ServiceDefinition serviceDefinition, final System provider) {
		logger.debug("checkConstraintOfSystemRegistryTable started ...");
		
		try {
			final Optional<ServiceRegistry> find = serviceRegistryRepository.findByServiceDefinitionAndSystem(serviceDefinition, provider);
			if (find.isPresent()) {
				throw new InvalidParameterException("Service Registry entry with provider: (" + provider.getSystemName() + ", " + provider.getAddress() + ":" + provider.getPort() +
													") and service definition: " + serviceDefinition.getServiceDefinition() + " already exists.");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceInterface> findOrCreateServiceInterfaces(final List<String> interfaces) {
		logger.debug("findOrCreateServiceInterfaces started ...");
		final List<ServiceInterface> result = new ArrayList<>(interfaces.size());
		final List<ServiceInterface> newInterfaces = new ArrayList<>();
		for (final String name : interfaces) {
			final String intfName = name.toUpperCase().trim();
			final Optional<ServiceInterface> optServiceInterface = serviceInterfaceRepository.findByInterfaceName(intfName);
			if (optServiceInterface.isPresent()) {
				result.add(optServiceInterface.get());
			} else {
				final ServiceInterface serviceInterface = new ServiceInterface(intfName);
				newInterfaces.add(serviceInterface);
			}
		}
		
		if (!newInterfaces.isEmpty()) {
			serviceInterfaceRepository.saveAll(newInterfaces);
			serviceInterfaceRepository.flush();
			result.addAll(newInterfaces);
		}
		
		return result;
	}
}
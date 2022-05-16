/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.serviceregistry.database.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.SSLProperties;
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
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceInterfacesListResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryGroupedResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.SpecialNetworkAddressTypeDetector;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;

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

	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private SpecialNetworkAddressTypeDetector networkAddressTypeDetector;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Autowired
	private SSLProperties sslProperties;

	@Value(CoreCommonConstants.$SERVICEREGISTRY_PING_TIMEOUT_WD)
	private int pingTimeout;

	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;
	
	private final Logger logger = LogManager.getLogger(ServiceRegistryDBService.class);

	private static final String COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE = "Could not delete System, with given parameters";
	private static final String PORT_RANGE_ERROR_MESSAGE = "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".";
	private static final String INVALID_FORMAT_ERROR_MESSAGE = " has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getSystemById(final long systemId) {
		logger.debug("getSystemById started...");

		try {
			final Optional<System> systemOption = systemRepository.findById(systemId);
			if (!systemOption.isPresent()){
				throw new InvalidParameterException("System with id " + systemId + " not found.");
			}

			return DTOConverter.convertSystemToSystemResponseDTO(systemOption.get());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public SystemListResponseDTO getSystemEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getSystemEntries started...");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

		if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}

		try {
			return DTOConverter.convertSystemEntryListToSystemListResponseDTO(systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public System createSystem(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {		
		logger.debug("createSystem started...");

		final System system = validateNonNullSystemParameters(systemName, address, port, authenticationInfo, metadata);

		try {
			return systemRepository.saveAndFlush(system);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public SystemResponseDTO createSystemResponse(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {
		logger.debug("createSystemResponse started...");

		return DTOConverter.convertSystemToSystemResponseDTO(createSystem(systemName, address, port, authenticationInfo, metadata));
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public SystemResponseDTO updateSystemResponse(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {
		logger.debug("updateSystemResponse started...");

		return DTOConverter.convertSystemToSystemResponseDTO(updateSystem(systemId, systemName, address, port, authenticationInfo, metadata));
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public System updateSystem(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) { 	
		logger.debug("updateSystem started...");

		final long validatedSystemId = validateSystemId(systemId);
		final int validatedPort = validateSystemPort(port);
		
		final String validatedSystemName = validateSystemParamString(systemName);
		if (!cnVerifier.isValid(validatedSystemName)) {
			throw new InvalidParameterException("System name" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		final String validatedAddress = networkAddressPreProcessor.normalize(address);
		networkAddressVerifier.verify(validatedAddress);
		final AddressType addressType = networkAddressTypeDetector.detectAddressType(validatedAddress);
		final String validatedAuthenticationInfo = authenticationInfo;


		try {
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
			system.setAddressType(addressType);
			system.setPort(validatedPort);
			system.setAuthenticationInfo(validatedAuthenticationInfo);
			system.setMetadata(Utilities.map2Text(metadata));

			return systemRepository.saveAndFlush(system);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeSystemById(final long id) {
		logger.debug("removeSystemById started...");

		try {
			if (!systemRepository.existsById(id)) {
				throw new InvalidParameterException(COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE);
			}

			systemRepository.deleteById(id);
			systemRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeSystemByNameAndAddressAndPort(final String systemName, final String address, final int port) {
		logger.debug("removeSystemByNameAndAddressAndPort started...");
		Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is not specified.");
		Assert.isTrue(!Utilities.isEmpty(address), "address is not specified.");
		Assert.notNull(port, "port is not specified");
		
		final String validatedSystemName = systemName.toLowerCase().trim();
		final String validatedSystemAddress = networkAddressPreProcessor.normalize(address);
		
		try {
			final Optional<System> optional = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedSystemAddress, port);
			if (optional.isEmpty()) {
				throw new InvalidParameterException("System not exists: " + validatedSystemName + " " + validatedSystemAddress + " " + port);
			} else {
				systemRepository.deleteById(optional.get().getId());
				systemRepository.flush();
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public SystemResponseDTO mergeSystemResponse(final long systemId, final String systemName, final String address, final Integer port, final String authenticationInfo, final Map<String,String> metadata) {		
		logger.debug("mergeSystemResponse started...");

		return DTOConverter.convertSystemToSystemResponseDTO(mergeSystem(systemId, systemName, address, port, authenticationInfo, metadata));
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public System mergeSystem(final long systemId, final String systemName, final String address, final Integer port, final String authenticationInfo, final Map<String,String> metadata) { 	
		logger.debug("mergeSystem started...");

		final long validatedSystemId = validateSystemId(systemId);
		final Integer validatedPort = validateAllowNullSystemPort(port);
		final String validatedSystemName = validateAllowNullSystemParamString(systemName);
		if (validatedSystemName != null && !cnVerifier.isValid(validatedSystemName)) {
			throw new InvalidParameterException("System name" + INVALID_FORMAT_ERROR_MESSAGE);
		}

		final String validatedAddress = networkAddressPreProcessor.normalize(address);
		if (!Utilities.isEmpty(validatedAddress)) {
			networkAddressVerifier.verify(validatedAddress);
		}
		final AddressType addressType = networkAddressTypeDetector.detectAddressType(validatedAddress);
		final String validatedAuthenticationInfo = authenticationInfo;

		try {
			final Optional<System> systemOptional = systemRepository.findById(validatedSystemId);
			if (!systemOptional.isPresent()) {
				throw new InvalidParameterException("No system with id : " + validatedSystemId);
			}

			final System system = systemOptional.get();

			if (checkSystemIfUniqueValidationNeeded(system, validatedSystemName, validatedAddress, validatedPort)) {
				checkConstraintsOfSystemTable(validatedSystemName != null ? validatedSystemName : system.getSystemName(),
											  !Utilities.isEmpty(validatedAddress) ? validatedAddress : system.getAddress(),
						validatedPort != null ? validatedPort.intValue() : system.getPort());
			}

			if (!Utilities.isEmpty(validatedSystemName)) {
				system.setSystemName(validatedSystemName);
			}

			if (!Utilities.isEmpty(validatedAddress)) {
				system.setAddress(validatedAddress);
				system.setAddressType(addressType);
			}

			if (validatedPort != null) {
				system.setPort(validatedPort);
			}

			if (!Utilities.isEmpty(validatedAuthenticationInfo)) {
				system.setAuthenticationInfo(validatedAuthenticationInfo);
			}

			if (metadata != null) {
				system.setMetadata(Utilities.map2Text(metadata));
			}
			
			return systemRepository.saveAndFlush(system);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void calculateSystemAddressTypeIfNecessary() {
		try {
			final List<System> systems = systemRepository.findByAddressTypeIsNull();
			for (final System system : systems) {
				final AddressType addressType = networkAddressTypeDetector.detectAddressType(system.getAddress());
				system.setAddressType(addressType);
			}
			
			systemRepository.saveAll(systems);
			systemRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
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
				throw new InvalidParameterException("Service definition with id of '" + id + "' does not exist");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionResponseDTO getServiceDefinitionByIdResponse(final long id) {
		logger.debug("getServiceDefinitionByIdResponse started...");

		final ServiceDefinition serviceDefinitionEntry = getServiceDefinitionById(id);
		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}

	//-------------------------------------------------------------------------------------------------
	public Page<ServiceDefinition> getServiceDefinitionEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceDefinitionEntries started...");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

		if (!ServiceDefinition.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}

		try {
			return serviceDefinitionRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionsListResponseDTO getServiceDefinitionEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceDefinitionEntriesResponse started...");
		final Page<ServiceDefinition> serviceDefinitionEntries = getServiceDefinitionEntries(page, size, direction, sortField);

		return DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinition createServiceDefinition(final String serviceDefinition) {
		logger.debug("createServiceDefinition started...");

		if (Utilities.isEmpty(serviceDefinition)) {
			throw new InvalidParameterException("serviceDefinition is null or blank");
		}

		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceDefinition)) {
			throw new InvalidParameterException("Service definition" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		final String validatedServiceDefinition = serviceDefinition.trim().toLowerCase();
		checkConstraintsOfServiceDefinitionTable(validatedServiceDefinition);
		final ServiceDefinition serviceDefinitionEntry = new ServiceDefinition(validatedServiceDefinition);
		try {
			return serviceDefinitionRepository.saveAndFlush(serviceDefinitionEntry);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinitionResponseDTO createServiceDefinitionResponse(final String serviceDefinition) {
		logger.debug("createServiceDefinitionResponse started...");

		final ServiceDefinition serviceDefinitionEntry = createServiceDefinition(serviceDefinition);

		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinition updateServiceDefinitionById(final long id, final String serviceDefinition) {
		logger.debug("updateServiceDefinitionById started..");

		if (Utilities.isEmpty(serviceDefinition)) {
			throw new InvalidParameterException("serviceDefinition is null or blank");
		}

		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceDefinition)) {
			throw new InvalidParameterException("Service definition" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		try {
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
				throw new InvalidParameterException("Service definition with id of '" + id + "' does not exist");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceDefinitionResponseDTO updateServiceDefinitionByIdResponse(final long id, final String serviceDefinition) {
		logger.debug("updateServiceDefinitionByIdResponse started...");

		final ServiceDefinition serviceDefinitionEntry = updateServiceDefinitionById(id, serviceDefinition);

		return DTOConverter.convertServiceDefinitionToServiceDefinitionResponseDTO(serviceDefinitionEntry);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeServiceDefinitionById(final long id) {
		logger.debug("removeServiceDefinitionById started...");

		try {
			if (!serviceDefinitionRepository.existsById(id)) {
				throw new InvalidParameterException("Service Definition with id '" + id + "' does not exist");
			}

			serviceDefinitionRepository.deleteById(id);
			serviceDefinitionRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistry getServiceRegistryEntryById(final long id) {
		logger.debug("getServiceRegistryEntryById started...");

		try {
			final Optional<ServiceRegistry> find = serviceRegistryRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("Service Registry with id of '" + id + "' does not exist");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryEntryByIdResponse(final long id) {
		logger.debug("getServiceRegistryEntryByIdResponse started...");

		return DTOConverter.convertServiceRegistryToServiceRegistryResponseDTO(getServiceRegistryEntryById(id));
	}

	//-------------------------------------------------------------------------------------------------
	public Page<ServiceRegistry> getServiceRegistryEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAllServiceRegistryEntries started...");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = sortField == null ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (!ServiceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}

		try {
			return serviceRegistryRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceRegistryEntriesResponse started...");

		final Page<ServiceRegistry> serviceRegistryEntries = getServiceRegistryEntries(page, size, direction, sortField);

		return DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntries);
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryGroupedResponseDTO getServiceRegistryDataForServiceRegistryGroupedResponse() {
		logger.debug("getServiceRegistryEntriesForServiceRegistryGroupedResponse started...");

		try {
			final List<ServiceDefinition> serviceDefinitionEntries = serviceDefinitionRepository.findAll();
			final List<System> systemEntries = systemRepository.findAll();
			final List<ServiceInterface> interfaceEntries = serviceInterfaceRepository.findAll();
			final List<ServiceRegistry> serviceRegistryEntries = serviceRegistryRepository.findAll();

			return DTOConverter.convertServiceRegistryDataToServiceRegistryGroupedResponseDTO(serviceDefinitionEntries, systemEntries, interfaceEntries, serviceRegistryEntries);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public Page<ServiceRegistry> getServiceRegistryEntriesByServiceDefinition(final String serviceDefinition, final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceRegistryEntriesByServiceDefinition started...");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "serviceDefinition is empty");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = sortField == null ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		if (!ServiceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}

		try {
			final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(serviceDefinition.toLowerCase().trim());
			if (serviceDefinitionOptional.isPresent()) {
				return serviceRegistryRepository.findAllByServiceDefinition(serviceDefinitionOptional.get(), PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
			} else {
				throw new InvalidParameterException("Service with definition of '" + serviceDefinition + "'is not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryEntriesByServiceDefinitionResponse(final String serviceDefinition, final int page, final int size, final Direction direction,
																							   final String sortField) {
		logger.debug("getServiceRegistryEntriesByServiceDefinitionResponse started...");

		final Page<ServiceRegistry> serviceRegistryEntriesByServiceDefinition = getServiceRegistryEntriesByServiceDefinition(serviceDefinition, page, size, direction, sortField);

		return DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(serviceRegistryEntriesByServiceDefinition);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistryResponseDTO registerServiceResponse(final ServiceRegistryRequestDTO request) {
		logger.debug("registerServiceResponse started...");
		Assert.notNull(request, "request is null.");
		checkServiceRegistryRequest(request);

		final String validatedServiceDefinition = request.getServiceDefinition().toLowerCase().trim();
		final String validatedProviderName = request.getProviderSystem().getSystemName().toLowerCase().trim();
		final String validatedProviderAddress = networkAddressPreProcessor.normalize(request.getProviderSystem().getAddress());
		networkAddressVerifier.verify(validatedProviderAddress);
		final int validatedProviderPort = request.getProviderSystem().getPort().intValue();
		final ServiceSecurityType validatedSecurityType = validateSRSecurityValue(request.getSecure());
		final String validatedServiceUri = request.getServiceUri() == null ? "" : request.getServiceUri().trim();

		try {
			final ServiceDefinition serviceDefinition = findOrCreateServiceDefinition(validatedServiceDefinition);
			final System provider = findOrCreateSystem(validatedProviderName, validatedProviderAddress, validatedProviderPort, request.getProviderSystem().getAuthenticationInfo(), request.getProviderSystem().getMetadata());

			final ZonedDateTime endOfValidity = Utilities.isEmpty(request.getEndOfValidity()) ? null : Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
			final String metadataStr = Utilities.map2Text(request.getMetadata());
			final int version = request.getVersion() == null ? Defaults.DEFAULT_VERSION : request.getVersion().intValue();
			final ServiceRegistry srEntry = createServiceRegistry(serviceDefinition, provider, validatedServiceUri, endOfValidity, validatedSecurityType, metadataStr, version, request.getInterfaces());

			return DTOConverter.convertServiceRegistryToServiceRegistryResponseDTO(srEntry);
		} catch (final DateTimeParseException ex) {
			logger.debug(ex.getMessage(), ex);
			throw new InvalidParameterException("End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.", ex);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistryResponseDTO updateServiceByIdResponse(final long id, final ServiceRegistryRequestDTO request) {
		logger.debug("updateServiceByIdResponse started...");
		Assert.notNull(request, "request is null.");
		Assert.isTrue(0 < id, "id is not greater than zero");

		checkServiceRegistryRequest(request);
		final ServiceSecurityType validatedSecurityType = validateSRSecurityValue(request.getSecure());

		try {
			ServiceRegistry srEntry;
			final Optional<ServiceRegistry> srEntryOptional = serviceRegistryRepository.findById(id);
			if (srEntryOptional.isPresent()) {
				srEntry = srEntryOptional.get();
			} else {
				throw new InvalidParameterException("Service Registry entry with id '" + id + "' does not exist");
			}

			final String validatedServiceDefinition = request.getServiceDefinition().toLowerCase().trim();
			final String validatedProviderName = request.getProviderSystem().getSystemName().toLowerCase().trim();
			final String validatedProviderAddress = networkAddressPreProcessor.normalize(request.getProviderSystem().getAddress());
			networkAddressVerifier.verify(validatedProviderAddress);
			final int validatedProviderPort = request.getProviderSystem().getPort().intValue();
			final String validatedServiceUri = request.getServiceUri() == null ? "" : request.getServiceUri().trim();

			final ServiceDefinition serviceDefinition = findOrCreateServiceDefinition(validatedServiceDefinition);
			final System provider = findOrCreateSystem(validatedProviderName, validatedProviderAddress, validatedProviderPort, request.getProviderSystem().getAuthenticationInfo(), request.getProviderSystem().getMetadata());
			final ZonedDateTime endOfValidity = Utilities.isEmpty(request.getEndOfValidity()) ? null : Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
			final String metadataStr = Utilities.map2Text(request.getMetadata());
			final int version = request.getVersion() == null ? 1 : request.getVersion().intValue();
			srEntry = updateServiceRegistry(srEntry, serviceDefinition, provider, validatedServiceUri, endOfValidity, validatedSecurityType, metadataStr, version, request.getInterfaces());

			return DTOConverter.convertServiceRegistryToServiceRegistryResponseDTO(srEntry);
		} catch (final DateTimeParseException ex) {
			logger.debug(ex.getMessage(), ex);
			throw new InvalidParameterException("End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.", ex);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3776")
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistryResponseDTO mergeServiceByIdResponse(final long id, final ServiceRegistryRequestDTO request) {
		logger.debug("mergeServiceByIdResponse started...");
		Assert.notNull(request, "request is null.");
		Assert.isTrue(0 < id, "id is not greater than zero");

		try {
			ServiceRegistry srEntry;
			final Optional<ServiceRegistry> srEntryOptional = serviceRegistryRepository.findById(id);
			if (srEntryOptional.isPresent()) {
				srEntry = srEntryOptional.get();
			} else {
				throw new InvalidParameterException("Service Registry entry with id '" + id + "' does not exist");
			}

			final String validatedServiceDefinition = !Utilities.isEmpty(request.getServiceDefinition()) ? request.getServiceDefinition().toLowerCase().trim() : srEntry.getServiceDefinition().getServiceDefinition();
			if (useStrictServiceDefinitionVerifier) {
				Assert.isTrue(cnVerifier.isValid(validatedServiceDefinition), "Service definition" + INVALID_FORMAT_ERROR_MESSAGE);
			}
			
			final String validatedProviderName = (request.getProviderSystem() != null && !Utilities.isEmpty(request.getProviderSystem().getSystemName())) ?
					request.getProviderSystem().getSystemName().toLowerCase().trim() :
					srEntry.getSystem().getSystemName();
			Assert.isTrue(cnVerifier.isValid(validatedProviderName), "Provider system name" + INVALID_FORMAT_ERROR_MESSAGE);		
																										   
			final String validatedProviderAddress = (request.getProviderSystem() != null && !Utilities.isEmpty(request.getProviderSystem().getAddress())) ?
																										   networkAddressPreProcessor.normalize(request.getProviderSystem().getAddress()) :
																										   srEntry.getSystem().getAddress();
			networkAddressVerifier.verify(validatedProviderAddress);
																										   
			final int validatedProviderPort = (request.getProviderSystem() != null && request.getProviderSystem().getPort() != null) ? request.getProviderSystem().getPort().intValue() :
																																	   srEntry.getSystem().getPort();

			final Optional<ServiceDefinition> optServiceDefinition = serviceDefinitionRepository.findByServiceDefinition(validatedServiceDefinition);
			final ServiceDefinition serviceDefinition = optServiceDefinition.isPresent() ? optServiceDefinition.get() : createServiceDefinition(validatedServiceDefinition);

			System provider;
			if (request.getProviderSystem() != null) {
				final Optional<System> optProvider = systemRepository.findBySystemNameAndAddressAndPort(validatedProviderName, validatedProviderAddress, validatedProviderPort);
				if (optProvider.isPresent()) {
					provider = optProvider.get();	
					boolean needSave = false;
					if (!Objects.equals(request.getProviderSystem().getAuthenticationInfo(), provider.getAuthenticationInfo())) { // authentication info has changed
						provider.setAuthenticationInfo(request.getProviderSystem().getAuthenticationInfo());
						needSave = true;
					}
					
					final String providerMetadataStr = Utilities.map2Text(request.getProviderSystem().getMetadata());
					if (!Objects.equals(providerMetadataStr, provider.getMetadata())) {
						provider.setMetadata(providerMetadataStr);
						needSave = true;
					}
					
					if (needSave) {
						provider = systemRepository.saveAndFlush(provider);
					}
				} else {
					provider = createSystem(validatedProviderName, validatedProviderAddress, validatedProviderPort, request.getProviderSystem().getAuthenticationInfo(), request.getProviderSystem().getMetadata());
				}
			} else {
				provider = srEntry.getSystem();
			}

			final ZonedDateTime endOfValidity = Utilities.isEmpty(request.getEndOfValidity()) ? srEntry.getEndOfValidity() : Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
			final String validatedMetadataStr = request.getMetadata() != null ? Utilities.map2Text(request.getMetadata()) : srEntry.getMetadata();
			final int validatedVersion = request.getVersion() != null ? request.getVersion().intValue() : srEntry.getVersion();

			final List<String> validatedInterfacesTemp = new ArrayList<>(srEntry.getInterfaceConnections().size());
			for (final ServiceRegistryInterfaceConnection serviceRegistryInterfaceConnection : srEntry.getInterfaceConnections()) {
				validatedInterfacesTemp.add(serviceRegistryInterfaceConnection.getServiceInterface().getInterfaceName());
			}

			final ServiceSecurityType validatedSecurityType = validateSRSecurityValue(request.getSecure());
			final String validatedServiceUri = request.getServiceUri() != null ? request.getServiceUri().trim() : srEntry.getServiceUri();
			final List<String> validatedInterfaces = request.getInterfaces() != null && !request.getInterfaces().isEmpty() ? request.getInterfaces() : validatedInterfacesTemp;

			srEntry = updateServiceRegistry(srEntry, serviceDefinition, provider, validatedServiceUri , endOfValidity,  validatedSecurityType, validatedMetadataStr, validatedVersion, validatedInterfaces);

			return DTOConverter.convertServiceRegistryToServiceRegistryResponseDTO(srEntry);
		} catch (final InvalidParameterException | IllegalArgumentException ex) {
			throw ex;
		} catch (final DateTimeParseException ex) {
			logger.debug(ex.getMessage(), ex);
			throw new InvalidParameterException("End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.", ex);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistry createServiceRegistry(final ServiceDefinition serviceDefinition, final System provider, final String serviceUri, final ZonedDateTime endOfValidity,
												 final ServiceSecurityType securityType, final String metadataStr, final int version, final List<String> interfaces) {
		logger.debug("createServiceRegistry started...");
		Assert.notNull(serviceDefinition, "Service definition is not specified.");
		if (useStrictServiceDefinitionVerifier) {
			Assert.isTrue(cnVerifier.isValid(serviceDefinition.getServiceDefinition()), "Service definition" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		Assert.notNull(provider, "Provider is not specified.");
		Assert.isTrue(cnVerifier.isValid(provider.getSystemName()), "Provider system name" + INVALID_FORMAT_ERROR_MESSAGE);
		try {
			networkAddressVerifier.verify(provider.getAddress());
		} catch (final InvalidParameterException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
		
		final String validatedServiceUri = Utilities.isEmpty(serviceUri) ? "" : serviceUri.trim();
		checkConstraintOfServiceRegistryTable(serviceDefinition, provider, validatedServiceUri);
		checkSRSecurityValue(securityType, provider.getAuthenticationInfo());
		checkSRServiceInterfacesList(interfaces);

		try {
			final ServiceSecurityType secure = securityType == null ? ServiceSecurityType.NOT_SECURE : securityType;
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
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceRegistry updateServiceRegistry(final ServiceRegistry srEntry, final ServiceDefinition serviceDefinition, final System provider, final String serviceUri,
												 final ZonedDateTime endOfValidity, final ServiceSecurityType securityType, final String metadataStr, final int version,
												 final List<String> interfaces) {
		logger.debug("updateServiceRegistry started...");
		Assert.notNull(srEntry, "ServiceRegistry Entry is not specified.");
		Assert.notNull(serviceDefinition, "Service definition is not specified.");
		if (useStrictServiceDefinitionVerifier) {
			Assert.isTrue(cnVerifier.isValid(serviceDefinition.getServiceDefinition()), "Service definition" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		Assert.notNull(provider, "Provider is not specified.");
		Assert.isTrue(cnVerifier.isValid(provider.getSystemName()), "Provider system name" + INVALID_FORMAT_ERROR_MESSAGE);
		try {
			networkAddressVerifier.verify(provider.getAddress());
		} catch (final InvalidParameterException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	
		final String validatedServiceUri = Utilities.isEmpty(serviceUri) ? "" : serviceUri.trim();
		if (checkServiceRegistryIfUniqueValidationNeeded(srEntry, serviceDefinition, provider, validatedServiceUri)) {
			checkConstraintOfServiceRegistryTable(serviceDefinition, provider, validatedServiceUri);			
		}

		checkSRSecurityValue(securityType, provider.getAuthenticationInfo());
		checkSRServiceInterfacesList(interfaces);

		return setModifiedValuesOfServiceRegistryEntryFields(srEntry, serviceDefinition, provider, validatedServiceUri, endOfValidity, securityType, metadataStr, version, interfaces);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeServiceRegistry(final String serviceDefinition, final String providerSystemName, final String providerSystemAddress, final int providerSystemPort, final String serviceUri) {
		logger.debug("removeServiceRegistry started...");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "Service definition is not specified.");
		Assert.isTrue(!Utilities.isEmpty(providerSystemName), "Provider system name is not specified.");
		Assert.isTrue(!Utilities.isEmpty(providerSystemAddress), "Provider system address is not specified.");

		final String validatedServiceDefinition = serviceDefinition.toLowerCase().trim();
		final String validatedSystemName = providerSystemName.toLowerCase().trim();
		final String validatedSystemAddress = networkAddressPreProcessor.normalize(providerSystemAddress);
		final String validatedServiceUri = serviceUri == null ? "" : serviceUri.trim();

		try {
			final Optional<ServiceDefinition> optServiceDefinition = serviceDefinitionRepository.findByServiceDefinition(validatedServiceDefinition);
			if (optServiceDefinition.isEmpty()) {
				throw new InvalidParameterException("No service exists with definition " + validatedServiceDefinition);
			}

			final Optional<System> optProviderSystem = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedSystemAddress, providerSystemPort);
			if (optProviderSystem.isEmpty()) {
				throw new InvalidParameterException("No system with name: " + validatedSystemName + ", address: " + validatedSystemAddress + ", port: " + providerSystemPort +
						" exists.");
			}

			final Optional<ServiceRegistry> optServiceRegistryEntry = serviceRegistryRepository.findByServiceDefinitionAndSystemAndServiceUri(optServiceDefinition.get(), optProviderSystem.get(), validatedServiceUri);
			if (optServiceRegistryEntry.isEmpty()) {
				throw new InvalidParameterException("No Service Registry entry with provider: (" + validatedSystemName + ", " + validatedSystemAddress + ":" + providerSystemPort +
													"), service definition: " + validatedServiceDefinition + " and service URI: " + validatedServiceUri  + " exists.");
			}

			removeServiceRegistryEntryById(optServiceRegistryEntry.get().getId());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({"squid:S3655", "squid:S3776"})
	public ServiceQueryResultDTO queryRegistry(final ServiceQueryFormDTO form) { 
		logger.debug("queryRegistry is started...");
		Assert.notNull(form, "Form is null.");
		Assert.isTrue(!Utilities.isEmpty(form.getServiceDefinitionRequirement()), "Service definition requirement is null or blank");

		final String serviceDefinitionRequirement = form.getServiceDefinitionRequirement().toLowerCase().trim();
		try {
			final Optional<ServiceDefinition> optServiceDefinition = serviceDefinitionRepository.findByServiceDefinition(serviceDefinitionRequirement);
			if (optServiceDefinition.isEmpty()) {
				// no service definition found
				logger.debug("Service definition not found: {}", serviceDefinitionRequirement);
				return DTOConverter.convertListOfServiceRegistryEntriesToServiceQueryResultDTO(null, 0);
			}

			final List<ServiceRegistry> providedServices = new ArrayList<>(serviceRegistryRepository.findByServiceDefinition(optServiceDefinition.get()));
			final int unfilteredHits = providedServices.size();
			logger.debug("Potential service providers before filtering: {}", unfilteredHits);
			if (providedServices.isEmpty()) {
				// no providers found
				return DTOConverter.convertListOfServiceRegistryEntriesToServiceQueryResultDTO(providedServices, unfilteredHits);
			}
			
			// filter on provider's address type
			if (form.getProviderAddressTypeRequirements() != null && !form.getProviderAddressTypeRequirements().isEmpty()) {
				final List<AddressType> normalizeAddressTypeRequirements = RegistryUtils.normalizeAddressTypes(form.getProviderAddressTypeRequirements());
				RegistryUtils.filterOnProviderAddressType(providedServices, normalizeAddressTypeRequirements);
			}

			// filter on interfaces
			if (form.getInterfaceRequirements() != null && !form.getInterfaceRequirements().isEmpty()) {
				final List<String> normalizedInterfaceRequirements = RegistryUtils.normalizeInterfaceNames(form.getInterfaceRequirements());
				RegistryUtils.filterOnInterfaces(providedServices, normalizedInterfaceRequirements);
			}

			// filter on security type
			if (!providedServices.isEmpty() && form.getSecurityRequirements() != null && !form.getSecurityRequirements().isEmpty()) {
				final List<ServiceSecurityType> normalizedSecurityTypes = RegistryUtils.normalizeSecurityTypes(form.getSecurityRequirements());
				RegistryUtils.filterOnSecurityType(providedServices, normalizedSecurityTypes);
			}

			// filter on version
			if (!providedServices.isEmpty()) {
				if (form.getVersionRequirement() != null) {
					RegistryUtils.filterOnVersion(providedServices, form.getVersionRequirement().intValue());
				} else if (form.getMinVersionRequirement() != null || form.getMaxVersionRequirement() != null) {
					final int minVersion = form.getMinVersionRequirement() == null ? 1 : form.getMinVersionRequirement().intValue();
					final int maxVersion = form.getMaxVersionRequirement() == null ? Integer.MAX_VALUE : form.getMaxVersionRequirement().intValue();
					RegistryUtils.filterOnVersion(providedServices, minVersion, maxVersion);
				}
			}

			// filter on metadata
			if (!providedServices.isEmpty() && form.getMetadataRequirements() != null && !form.getMetadataRequirements().isEmpty()) {
				final Map<String,String> normalizedMetadata = RegistryUtils.normalizeMetadata(form.getMetadataRequirements());
				RegistryUtils.filterOnMeta(providedServices, normalizedMetadata);
			}

			// filter on ping
			if (!providedServices.isEmpty() && form.getPingProviders()) {
				RegistryUtils.filterOnPing(providedServices, pingTimeout);
			}

			logger.debug("Potential service providers after filtering: {}", providedServices.size());

			return DTOConverter.convertListOfServiceRegistryEntriesToServiceQueryResultDTO(providedServices, unfilteredHits);
		} catch (final IllegalStateException e) {
			throw new InvalidParameterException("Invalid keys in the metadata requirements (whitespace only differences)");
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryResultListDTO multiQueryRegistry(final ServiceQueryFormListDTO forms) { 
		logger.debug("multiQueryRegistry is started...");
		Assert.notNull(forms, "Form list is null.");
		Assert.notNull(forms.getForms(), "Form list is null.");
		Assert.isTrue(!forms.getForms().isEmpty(), "Form list is empty.");

		final List<ServiceQueryResultDTO> result = new ArrayList<>(forms.getForms().size());
		for (final ServiceQueryFormDTO form : forms.getForms()) {
			final ServiceQueryResultDTO queryResult = queryRegistry(form);
			result.add(queryResult);
		}
		
		return new ServiceQueryResultListDTO(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeServiceRegistryEntryById(final long id) {
		logger.debug("removeServiceRegistryEntryById started...");

		try {
			if (!serviceRegistryRepository.existsById(id)) {
				throw new InvalidParameterException("Service Registry entry with id '" + id + "' does not exist");
			}

			serviceRegistryRepository.deleteById(id);
			serviceRegistryRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeBulkOfServiceRegistryEntries(final Iterable<ServiceRegistry> entities) {
		logger.debug("removeBulkOfServiceRegistryEntries started...");

		try {
			serviceRegistryRepository.deleteInBatch(entities);
			serviceRegistryRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getSystemByNameAndAddressAndPortResponse(final String systemName, final String address, final int port) {
		logger.debug("getSystemByNameAndAddressAndPortResponse started...");

		final int validatedPort = validateSystemPort(port);
		final String validatedSystemName = validateSystemParamString(systemName);
		final String validatedAddress = networkAddressPreProcessor.normalize(address);
		networkAddressVerifier.verify(validatedAddress);

		try {
			final Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedAddress, validatedPort);
			if (systemOptional.isEmpty()) {
				throw new InvalidParameterException("No system with name: " + validatedSystemName + ", address: " +  validatedAddress + " and port: " + validatedPort);
			}

			return DTOConverter.convertSystemToSystemResponseDTO(systemOptional.get());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public List<System> getSystemByName(final String systemName) {
		logger.debug("getSystemByName started...");

		final String name = validateSystemParamString(systemName);
		try {
			return systemRepository.findBySystemName(name);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public Page<ServiceInterface> getServiceInterfaceEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceInterfaceEntries started...");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

		if (!ServiceInterface.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}

		try {
			return serviceInterfaceRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterface getServiceInterfaceById(final long id) {
		logger.debug("getServiceInterfaceById started...");

		try {
			final Optional<ServiceInterface> find = serviceInterfaceRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("Service interface with id of '" + id + "' does not exist");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterfaceResponseDTO getServiceInterfaceByIdResponse(final long id) {
		logger.debug("getServiceInterfaceByIdResponse started...");

		final ServiceInterface serviceInterfaceEntry = getServiceInterfaceById(id);
		return DTOConverter.convertServiceInterfaceToServiceInterfaceResponseDTO(serviceInterfaceEntry);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceInterface createServiceInterface(final String serviceInterface) {
		logger.debug("createServiceInterface started...");

		if (Utilities.isEmpty(serviceInterface)) {
			throw new InvalidParameterException("serviceInterface is null or blank");
		}

		Assert.isTrue(interfaceNameVerifier.isValid(serviceInterface), "Specified interface name is not valid: " + serviceInterface);

		final String validatedServiceInterface = serviceInterface.trim().toUpperCase();
		checkConstraintsOfServiceInterfaceTable(validatedServiceInterface);
		final ServiceInterface serviceInterfaceEntry = new ServiceInterface(validatedServiceInterface);
		try {
			return serviceInterfaceRepository.saveAndFlush(serviceInterfaceEntry);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceInterfaceResponseDTO createServiceInterfaceResponse(final String serviceInterface) {
		logger.debug("createServiceInterfaceResponse started...");

		final ServiceInterface serviceInterfaceEntry = createServiceInterface(serviceInterface);

		return DTOConverter.convertServiceInterfaceToServiceInterfaceResponseDTO(serviceInterfaceEntry);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceInterface updateServiceInterfaceById(final long id, final String serviceInterface) {
		logger.debug("updateServiceInterfaceById started..");

		if (Utilities.isEmpty(serviceInterface)) {
			throw new InvalidParameterException("serviceInterface is null or blank");
		}

		Assert.isTrue(interfaceNameVerifier.isValid(serviceInterface), "Specified interface name is not valid: " + serviceInterface);

		try {
			final Optional<ServiceInterface> find = serviceInterfaceRepository.findById(id);
			if (find.isPresent()) {
				final String validatedServiceInterface = serviceInterface.trim().toUpperCase();
				final ServiceInterface serviceInterfaceEntry = find.get();
				if (!validatedServiceInterface.equals(serviceInterfaceEntry.getInterfaceName())) {
					checkConstraintsOfServiceInterfaceTable(validatedServiceInterface);
				}

				serviceInterfaceEntry.setInterfaceName(validatedServiceInterface);
				return serviceInterfaceRepository.saveAndFlush(serviceInterfaceEntry);
			} else {
				throw new InvalidParameterException("Service interface with id of '" + id + "' does not exist");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ServiceInterfaceResponseDTO updateServiceInterfaceByIdResponse(final long id, final String serviceInterface) {
		logger.debug("updateServiceInterfaceByIdResponse started...");

		final ServiceInterface serviceInterfaceEntry = updateServiceInterfaceById(id, serviceInterface);

		return DTOConverter.convertServiceInterfaceToServiceInterfaceResponseDTO(serviceInterfaceEntry);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeServiceInterfaceById(final long id) {
		logger.debug("removeServiceInterfaceById started...");

		try {
			if (!serviceInterfaceRepository.existsById(id)) {
				throw new InvalidParameterException("Service Interface with id '" + id + "' does not exist");
			}

			serviceInterfaceRepository.deleteById(id);
			serviceInterfaceRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterfacesListResponseDTO getServiceInterfaceEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getServiceInterfaceEntriesResponse started...");
		final Page<ServiceInterface> serviceInterfaceEntries = getServiceInterfaceEntries(page, size, direction, sortField);

		return DTOConverter.convertServiceInterfacesListToServiceInterfaceListResponseDTO(serviceInterfaceEntries);
	}

	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistry> getServiceRegistryEntriesByServiceDefinitonList(final List<String> serviceDefinitions) {
		logger.debug("getServiceRegistryEntriesByServiceDefinitonList started...");
		Assert.notNull(serviceDefinitions, "Service definition list is null");

		final List<ServiceRegistry> results = new ArrayList<>();
		for (final String definition : serviceDefinitions) {
			if (Utilities.isEmpty(definition)) {
				throw new InvalidParameterException("Service definition is empty or null");
			}

			final Optional<ServiceDefinition> opt = serviceDefinitionRepository.findByServiceDefinition(definition.trim().toLowerCase());
			if (opt.isPresent()) {
				results.addAll(serviceRegistryRepository.findByServiceDefinition(opt.get()));
			} else {
				logger.debug("Service with definition of '" + definition + "'is not exists");
			}
		}
		return results;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryEntriesByServiceDefinitonListResponse(final List<String> serviceDefinitions) {
		logger.debug("getServiceRegistryEntriesByServiceDefinitonListResponse started...");
		final List<ServiceRegistry> results = getServiceRegistryEntriesByServiceDefinitonList(serviceDefinitions);
		return DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(new PageImpl<ServiceRegistry>(results));
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryEntriesBySystemIdResponse(final long systemId) {
		final List<ServiceRegistry> result = getServiceRegistryEntriesBySystemId(systemId);
		return DTOConverter.convertServiceRegistryListToServiceRegistryListResponseDTO(new PageImpl<ServiceRegistry>(result));
	}

	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistry> getServiceRegistryEntriesBySystemId(final long systemId) {
		logger.debug("getServiceRegistryEntriesBySystemId started...");
		validateSystemId(systemId);

		try {
			final Optional<System> opt = systemRepository.findById(systemId);
			if (opt.isEmpty()) {
				throw new InvalidParameterException("No system with id: " + systemId + " exists");
			} else {
				return serviceRegistryRepository.findBySystem(opt.get());
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S1126")
	private boolean checkSystemIfUniqueValidationNeeded(final System system, final String validatedSystemName, final String validatedAddress, final Integer validatedPort) {
		logger.debug("checkSystemIfUniqueValidationNeeded started...");

		final String actualSystemName = system.getSystemName();
		final String actualAddress = system.getAddress();
		final int actualPort = system.getPort();

		if (validatedSystemName != null && !actualSystemName.equalsIgnoreCase(validatedSystemName)) {
			return true;
		}

		if (!Utilities.isEmpty(validatedAddress) && !actualAddress.equalsIgnoreCase(validatedAddress)) {
			return true;
		}

		if (validatedPort != null && actualPort != validatedPort.intValue()) {
			return true;
		}

		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfSystemTable(final String validatedSystemName, final String validatedAddress, final int validatedPort) {
		logger.debug("checkConstraintsOfSystemTable started...");

		try {
			final Optional<System> find = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName.toLowerCase().trim(), validatedAddress.toLowerCase().trim(), validatedPort);
			if (find.isPresent()) {
				throw new InvalidParameterException("System with name: " + validatedSystemName + ", address: " + validatedAddress +	", port: " + validatedPort + " already exists.");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private System validateNonNullSystemParameters(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {
		logger.debug("validateNonNullSystemParameters started...");

		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException("System name is null or empty");
		}

		if (!cnVerifier.isValid(systemName)) {
			throw new InvalidParameterException("System name" + INVALID_FORMAT_ERROR_MESSAGE);
		}

		final String validatedAddress = networkAddressPreProcessor.normalize(address);
		networkAddressVerifier.verify(validatedAddress);
		final AddressType addressType = networkAddressTypeDetector.detectAddressType(validatedAddress);
		
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
		}

		final String validatedSystemName = systemName.trim().toLowerCase();
		final String validatedAuthenticationInfo = authenticationInfo;

		checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);

		return new System(validatedSystemName, validatedAddress, addressType, port, validatedAuthenticationInfo, Utilities.map2Text(metadata));
	}

	//-------------------------------------------------------------------------------------------------
	private String validateSystemParamString(final String param) {
		logger.debug("validateSystemParamString started...");

		if (Utilities.isEmpty(param)) {
			throw new InvalidParameterException("parameter null or empty");
		}

		return param.trim().toLowerCase();
	}

	//-------------------------------------------------------------------------------------------------
	private int validateSystemPort(final int port) {
		logger.debug("validateSystemPort started...");

		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
		}

		return port;
	}

	//-------------------------------------------------------------------------------------------------
	private long validateSystemId(final long systemId) {
		logger.debug("validateSystemId started...");

		if (systemId < 1) {
			throw new IllegalArgumentException("System id must be greater than zero");
		}

		return systemId;
	}

	//-------------------------------------------------------------------------------------------------
	private Integer validateAllowNullSystemPort(final Integer port) {
		logger.debug("validateAllowNullSystemPort started...");

		if (port != null && (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX)) {
			throw new IllegalArgumentException(PORT_RANGE_ERROR_MESSAGE);
		}

		return port;
	}

	//-------------------------------------------------------------------------------------------------
	private String validateAllowNullSystemParamString(final String param) {
		logger.debug("validateAllowNullSystemParamString started...");

		if (Utilities.isEmpty(param)) {
			return null;
		}

		return param.trim().toLowerCase();
	}

	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfServiceDefinitionTable(final String serviceDefinition) {
		logger.debug("checkConstraintsOfServiceDefinitionTable started...");

		try {
			final Optional<ServiceDefinition> find = serviceDefinitionRepository.findByServiceDefinition(serviceDefinition.toLowerCase().trim());
			if (find.isPresent()) {
				throw new InvalidParameterException(serviceDefinition + " definition already exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryRequest(final ServiceRegistryRequestDTO request) {
		logger.debug("checkServiceRegistryRequest started...");
		Assert.isTrue(!Utilities.isEmpty(request.getServiceDefinition()), "Service definition is not specified.");
		
		if (useStrictServiceDefinitionVerifier) {
			Assert.isTrue(cnVerifier.isValid(request.getServiceDefinition()), "Service definition" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		Assert.notNull(request.getProviderSystem(), "Provider system is not specified.");
		Assert.isTrue(!Utilities.isEmpty(request.getProviderSystem().getSystemName()), "Provider system name is not specified.");
		Assert.isTrue(cnVerifier.isValid(request.getProviderSystem().getSystemName()), "Provider system name" + INVALID_FORMAT_ERROR_MESSAGE);		
		Assert.isTrue(!Utilities.isEmpty(request.getProviderSystem().getAddress()), "Provider address is not specified.");//Cannot verify here the address due to pre processing needed
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
		Assert.isTrue(validatedType == ServiceSecurityType.NOT_SECURE || (validatedType != ServiceSecurityType.NOT_SECURE && providerSystemAuthenticationInfo != null),
				"Security type is in conflict with the availability of the authentication info.");

		if (!sslProperties.isSslEnabled() && type != ServiceSecurityType.NOT_SECURE ) {
			throw new InvalidParameterException("ServiceRegistry insecure mode can not handle secure services") ;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceSecurityType validateSRSecurityValue(final String secure) {
		logger.debug("validateSRSecurityValue started...");

		ServiceSecurityType validatedType = null;
		if (secure != null) {
			for (final ServiceSecurityType type : ServiceSecurityType.values()) {
				if (type.name().equalsIgnoreCase(secure)) {
					validatedType = type;
					break;
				}
			}

			if (validatedType == null) {
				throw new InvalidParameterException("Security type is not valid.");
			}
		} else {
			validatedType = ServiceSecurityType.NOT_SECURE;
		}

		return validatedType;
	}

	//-------------------------------------------------------------------------------------------------
	private void checkConstraintOfServiceRegistryTable(final ServiceDefinition serviceDefinition, final System provider, final String serviceUri) {
		logger.debug("checkConstraintOfServiceRegistryTable started...");

		try {
			final Optional<ServiceRegistry> find = serviceRegistryRepository.findByServiceDefinitionAndSystemAndServiceUri(serviceDefinition, provider, serviceUri);
			if (find.isPresent()) {
				throw new InvalidParameterException("Service Registry entry with provider: (" + provider.getSystemName() + ", " + provider.getAddress() + ":" + provider.getPort() +
													"), service definition: " + serviceDefinition.getServiceDefinition() + " and service URI: " + serviceUri + " already exists.");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private System findOrCreateSystem(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {
		final Optional<System> optProvider = systemRepository.findBySystemNameAndAddressAndPort(systemName.toLowerCase().trim(), address.toLowerCase().trim(), port);
		System provider;
		if (optProvider.isPresent()) {
			provider = optProvider.get();
			boolean needSave = false;
			if (!Objects.equals(authenticationInfo, provider.getAuthenticationInfo())) { // authentication info has changed
				provider.setAuthenticationInfo(authenticationInfo);
				needSave = true;
			}
			
			final String metadataStr = Utilities.map2Text(metadata);
			if (!Objects.equals(metadataStr, provider.getMetadata())) {
				provider.setMetadata(metadataStr);
				needSave = true;
			}
			
			if (needSave) {
				provider = systemRepository.saveAndFlush(provider);
			}
		} else {
			provider = createSystem(systemName, address, port, authenticationInfo, metadata);
		}
		
		return provider;
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition findOrCreateServiceDefinition(final String serviceDef) {
		final Optional<ServiceDefinition> optServiceDefinition = serviceDefinitionRepository.findByServiceDefinition(serviceDef.toLowerCase().trim());

		return optServiceDefinition.isPresent() ? optServiceDefinition.get() : createServiceDefinition(serviceDef);
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceInterface> findOrCreateServiceInterfaces(final List<String> interfaces) {
		logger.debug("findOrCreateServiceInterfaces started...");

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

	//-------------------------------------------------------------------------------------------------	
	private boolean checkServiceRegistryIfUniqueValidationNeeded(final ServiceRegistry srEntry, final ServiceDefinition serviceDefinition, final System provider, final String serviceUri) {
		logger.debug("checkServiceRegistryIfUniqueValidationNeeded started...");

		return srEntry.getSystem().getId() != provider.getId() || srEntry.getServiceDefinition().getId() != serviceDefinition.getId() || !srEntry.getServiceUri().equals(serviceUri);
	}

	//-------------------------------------------------------------------------------------------------	
	private ServiceRegistry setModifiedValuesOfServiceRegistryEntryFields(final ServiceRegistry srEntry, final ServiceDefinition serviceDefinition, final System provider,
																		  final String serviceUri, final ZonedDateTime endOfValidity, final ServiceSecurityType securityType,
																		  final String metadataStr, final int version, final List<String> interfaces) {
		logger.debug("setModifiedValuesOfServiceRegistryEntryFields started...");

		try {
			final ServiceSecurityType secure = securityType == null ? ServiceSecurityType.NOT_SECURE : securityType;

			final Set<ServiceRegistryInterfaceConnection> connectionList = srEntry.getInterfaceConnections();
			serviceRegistryInterfaceConnectionRepository.deleteInBatch(connectionList);
			serviceRegistryRepository.refresh(srEntry);

			final List<ServiceInterface> serviceInterfaces = findOrCreateServiceInterfaces(interfaces);
			for (final ServiceInterface serviceInterface : serviceInterfaces) {
				final ServiceRegistryInterfaceConnection connection = new ServiceRegistryInterfaceConnection(srEntry, serviceInterface);
				srEntry.getInterfaceConnections().add(connection);
			}
			serviceRegistryInterfaceConnectionRepository.saveAll(srEntry.getInterfaceConnections());

			srEntry.setServiceDefinition(serviceDefinition);
			srEntry.setSystem(provider);
			srEntry.setServiceUri(serviceUri);
			srEntry.setEndOfValidity(endOfValidity);
			srEntry.setSecure(secure);
			srEntry.setMetadata(metadataStr);
			srEntry.setVersion(version);

			return serviceRegistryRepository.saveAndFlush(srEntry);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfServiceInterfaceTable(final String serviceInterface) {
		logger.debug("checkConstraintsOfServiceInterfaceTable started...");

		try {
			final Optional<ServiceInterface> find = serviceInterfaceRepository.findByInterfaceName(serviceInterface.toUpperCase().trim());
			if (find.isPresent()) {
				throw new InvalidParameterException(serviceInterface + " interface already exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}
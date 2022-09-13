/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.systemregistry.database.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreEventHandlerConstants;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.SystemRegistry;
import eu.arrowhead.common.database.repository.DeviceRepository;
import eu.arrowhead.common.database.repository.SystemRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.drivers.CertificateAuthorityDriver;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.drivers.EventDriver;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemQueryResultDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithNameResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.SpecialNetworkAddressTypeDetector;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@Service
public class SystemRegistryDBService {

    //=================================================================================================
    // members

    private static final String COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE = "Could not delete System, with given parameters";
    private static final String COULD_NOT_DELETE_DEVICE_ERROR_MESSAGE = "Could not delete Device, with given parameters";
    private static final String PORT_RANGE_ERROR_MESSAGE = "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".";
	private static final String INVALID_FORMAT_ERROR_MESSAGE = " has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING;


    private final Logger logger = LogManager.getLogger(SystemRegistryDBService.class);

    private final SystemRegistryRepository systemRegistryRepository;
    private final SystemRepository systemRepository;
    private final DeviceRepository deviceRepository;
    private final SecurityUtilities securityUtilities;
    private final CertificateAuthorityDriver caDriver;
    private final CommonNamePartVerifier cnVerifier;
    private final NetworkAddressPreProcessor networkAddressPreProcessor;
    private final SpecialNetworkAddressTypeDetector networkAddressTypeDetector;
    private final NetworkAddressVerifier networkAddressVerifier;
    private final DriverUtilities driverUtilities;
    private final EventDriver eventDriver;

    @Value(CoreCommonConstants.$SYSTEMREGISTRY_PING_TIMEOUT_WD)
    private int pingTimeout;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Autowired
    public SystemRegistryDBService(final SystemRegistryRepository systemRegistryRepository,
    							   final SystemRepository systemRepository,
    							   final DeviceRepository deviceRepository,
    							   final SecurityUtilities securityUtilities,
    							   final CertificateAuthorityDriver caDriver,
    							   final DriverUtilities driverUtilities,
    							   final CommonNamePartVerifier cnVerifier,
    							   final NetworkAddressPreProcessor networkAddressPreProcessor,
    							   final SpecialNetworkAddressTypeDetector networkAddressTypeDetector,
    							   final NetworkAddressVerifier networkAddressVerifier,
    							   final EventDriver eventDriver) {
    	this.systemRegistryRepository = systemRegistryRepository;
    	this.systemRepository = systemRepository;
    	this.deviceRepository = deviceRepository;
    	this.securityUtilities = securityUtilities;
    	this.caDriver = caDriver;
        this.driverUtilities = driverUtilities;
        this.eventDriver = eventDriver;
    	this.cnVerifier = cnVerifier;
    	this.networkAddressPreProcessor = networkAddressPreProcessor;
    	this.networkAddressTypeDetector = networkAddressTypeDetector;
    	this.networkAddressVerifier = networkAddressVerifier;
    }

    //-------------------------------------------------------------------------------------------------
    public SystemResponseDTO getSystemById(final long systemId) {
        logger.debug("getSystemById started...");

        try {
            final Optional<System> systemOption = systemRepository.findById(systemId);
            if (systemOption.isEmpty()) {
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
    public SystemListResponseDTO getSystemEntries(final CoreUtilities.ValidatedPageParams pageParams, final String sortField) {
        logger.debug("getSystemEntries started...");

        final int validatedPage = pageParams.getValidatedPage();
        final int validatedSize = pageParams.getValidatedSize();
        final Sort.Direction validatedDirection = pageParams.getValidatedDirection();
        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            return DTOConverter.convertSystemEntryListToSystemListResponseDTO(
                    systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO createSystemDto(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {
        logger.debug("createSystemResponse started...");

        return DTOConverter.convertSystemToSystemResponseDTO(createSystem(systemName, address, port, authenticationInfo, metadata));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO updateSystemDto(final long systemId, final String systemName, final String address, final int port,
                                             final String authenticationInfo, final Map<String,String> metadata) { 
        logger.debug("updateSystemResponse started...");

        return DTOConverter.convertSystemToSystemResponseDTO(updateSystem(systemId, systemName, address, port, authenticationInfo, metadata));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public System updateSystem(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) { 
        logger.debug("updateSystem started...");

        final long validatedSystemId = validateId(systemId);
        final int validatedPort = validateSystemPort(port);
        final String validatedSystemName = validateParamString(systemName);
        if (!cnVerifier.isValid(validatedSystemName)) {
        	throw new InvalidParameterException("System name" + INVALID_FORMAT_ERROR_MESSAGE);
        }
        final String validatedAddress = networkAddressPreProcessor.normalize(address);
        networkAddressVerifier.verify(validatedAddress);
        final AddressType addressType = networkAddressTypeDetector.detectAddressType(validatedAddress);

        try {
            final Optional<System> systemOptional = systemRepository.findById(validatedSystemId);
            if (systemOptional.isEmpty()) {
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
            system.setAuthenticationInfo(authenticationInfo);
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

            final Optional<SystemRegistry> optional = systemRegistryRepository.findById(id);

            systemRepository.deleteById(id);
            systemRepository.flush();

            optional.ifPresent(this::publishUnregister);

        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO mergeSystemResponse(final long systemId, final String systemName, final String address, final Integer port,
                                                 final String authenticationInfo, final Map<String,String> metadata) {
        logger.debug("mergeSystemResponse started...");

        return DTOConverter.convertSystemToSystemResponseDTO(mergeSystem(systemId, systemName, address, port, authenticationInfo, metadata));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public System mergeSystem(final long systemId, final String systemName, final String address, final Integer port, final String authenticationInfo, final Map<String,String> metadata) { 
        logger.debug("mergeSystem started...");

        final long validatedSystemId = validateId(systemId);
        final Integer validatedPort = validateAllowNullSystemPort(port);
        final String validatedSystemName = validateAllowNullParamString(systemName);
        if (validatedSystemName != null && !cnVerifier.isValid(validatedSystemName)) {
            throw new InvalidParameterException("System name" + INVALID_FORMAT_ERROR_MESSAGE);
        }
        final String validatedAddress = networkAddressPreProcessor.normalize(address);
        if (!Utilities.isEmpty(validatedAddress)) {
        	networkAddressVerifier.verify(validatedAddress);
		}
        final AddressType addressType = networkAddressTypeDetector.detectAddressType(validatedAddress);

        try {
            final Optional<System> systemOptional = systemRepository.findById(validatedSystemId);
            if (systemOptional.isEmpty()) {
                throw new InvalidParameterException("No system with id : " + validatedSystemId);
            }

            final System system = systemOptional.get();

            if (checkSystemIfUniqueValidationNeeded(system, validatedSystemName, validatedAddress, validatedPort)) {
                checkConstraintsOfSystemTable(validatedSystemName != null ? validatedSystemName : system.getSystemName(),
                                              !Utilities.isEmpty(validatedAddress) ? validatedAddress : system.getAddress(),
                                              validatedPort != null ? validatedPort : system.getPort());
            }

            if (Utilities.notEmpty(validatedSystemName)) {
                system.setSystemName(validatedSystemName);
            }

            if (Utilities.notEmpty(validatedAddress)) {
                system.setAddress(validatedAddress);
                system.setAddressType(addressType);
            }

            if (validatedPort != null) {
                system.setPort(validatedPort);
            }

            if (Utilities.notEmpty(authenticationInfo)) {
                system.setAuthenticationInfo(authenticationInfo);
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
    public DeviceResponseDTO getDeviceById(final long deviceId) {
        logger.debug("getDeviceById started...");

        try {
            final Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
            if (deviceOptional.isEmpty()) {
                throw new InvalidParameterException("Device with id " + deviceId + " not found.");
            }

            return DTOConverter.convertDeviceToDeviceResponseDTO(deviceOptional.get());
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    public DeviceListResponseDTO getDeviceEntries(final CoreUtilities.ValidatedPageParams pageParams, final String sortField) {
        logger.debug("getDeviceList started...");

        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            final Page<Device> devices = deviceRepository.findAll(PageRequest.of(pageParams.getValidatedPage(), pageParams.getValidatedSize(),
                                                                                 pageParams.getValidatedDirection(), validatedSortField));
            return DTOConverter.convertDeviceEntryListToDeviceListResponseDTO(devices);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceResponseDTO createDeviceDto(final String name, final String address, final String macAddress, final String authenticationInfo) {
        return DTOConverter.convertDeviceToDeviceResponseDTO(createDevice(name, address, macAddress, authenticationInfo));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceResponseDTO updateDeviceByIdResponse(final long id, final String name, final String address, final String macAddress,
                                                      final String authenticationInfo) {
        logger.debug("updateDeviceByIdResponse started...");

        try {

            final long validatedId = validateId(id);
            final Device newDevice = validateNonNullDeviceParameters(name, address, macAddress, authenticationInfo);

            final Optional<Device> optionalDevice = deviceRepository.findById(validatedId);
            final Device device = optionalDevice.orElseThrow(() -> new InvalidParameterException("No device with id : " + id));

            device.setDeviceName(newDevice.getDeviceName());
            device.setAddress(newDevice.getAddress());
            device.setMacAddress(newDevice.getMacAddress());
            device.setAuthenticationInfo(newDevice.getAuthenticationInfo());

            return DTOConverter.convertDeviceToDeviceResponseDTO(deviceRepository.saveAndFlush(device));
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeDeviceById(final long id) {
        logger.debug("removeDeviceById started...");

        try {
            if (!deviceRepository.existsById(id)) {
                throw new InvalidParameterException(COULD_NOT_DELETE_DEVICE_ERROR_MESSAGE);
            }

            deviceRepository.deleteById(id);
            deviceRepository.flush();
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryListResponseDTO getSystemRegistryEntries(final CoreUtilities.ValidatedPageParams params, final String sortField) {
        logger.debug("getSystemRegistryEntries started...");
        final String validatedSortField = sortField == null ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!SystemRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            final PageRequest pageRequest = PageRequest.of(params.getValidatedPage(),
                                                           params.getValidatedSize(),
                                                           params.getValidatedDirection(),
                                                           validatedSortField);
            final Page<SystemRegistry> systemRegistryPage = systemRegistryRepository.findAll(pageRequest);
            return DTOConverter.convertSystemRegistryListToSystemRegistryListResponseDTO(systemRegistryPage);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryResponseDTO getSystemRegistryById(final long id) {
        logger.debug("getSystemRegistryById started...");

        return DTOConverter.convertSystemRegistryToSystemRegistryResponseDTO(getSystemRegistryEntryById(id));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeSystemRegistryEntryById(final long id) {
        logger.debug("removeSystemRegistryEntryById started...");

        try {
            if (!systemRegistryRepository.existsById(id)) {
                throw new InvalidParameterException("System Registry entry with id '" + id + "' does not exist");
            }

            systemRegistryRepository.deleteById(id);
            systemRegistryRepository.flush();
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryResponseDTO registerSystemRegistry(final SystemRegistryRequestDTO request) {
        logger.debug("registerSystemRegistry started...");
        checkSystemRegistryRequest(request);

        try {
            final ZonedDateTime endOfValidity = getZonedDateTime(request);
            final String metadataStr = Utilities.map2Text(request.getMetadata());
            final int version = (request.getVersion() != null) ? request.getVersion() : 1;
            final System systemDb = findOrCreateSystem(request.getSystem());
            final Device deviceDb = findOrCreateDevice(request.getProvider());

            final SystemRegistry srEntry = createSystemRegistry(systemDb, deviceDb, endOfValidity, metadataStr, version);

            publishRegister(request);

            return DTOConverter.convertSystemRegistryToSystemRegistryResponseDTO(srEntry);
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
    public SystemRegistryResponseDTO updateSystemRegistryById(final long id, final SystemRegistryRequestDTO request) {
        logger.debug("updateSystemRegistryById started...");

        Assert.isTrue(0 < id, "id is not greater than zero");
        checkSystemRegistryRequest(request);

        try {
            final SystemRegistry srEntry;
            final SystemRegistry updateSrEntry;

            final Optional<SystemRegistry> srEntryOptional = systemRegistryRepository.findById(id);
            srEntry = srEntryOptional.orElseThrow(() -> new InvalidParameterException("System Registry entry with id '" + id + "' does not exist"));

            final System systemDb = findOrCreateSystem(request.getSystem());
            final Device deviceDb = findOrCreateDevice(request.getProvider());
            final ZonedDateTime endOfValidity = getZonedDateTime(request);
            final String metadataStr = Utilities.map2Text(request.getMetadata());
            final int version = (request.getVersion() != null) ? request.getVersion() : 1;

            updateSrEntry = updateSystemRegistry(srEntry, systemDb, deviceDb, endOfValidity, metadataStr, version);

            return DTOConverter.convertSystemRegistryToSystemRegistryResponseDTO(updateSrEntry);
        } catch (final DateTimeParseException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new InvalidParameterException(
                    "End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.", ex);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryResponseDTO mergeSystemRegistryById(final long id, final SystemRegistryRequestDTO request) {

        logger.debug("mergeSystemRegistryById started...");
        Assert.notNull(request, "request is null.");
        Assert.isTrue(0 < id, "id is not greater than zero");

        try {
            final SystemRegistry srEntry;
            final SystemRegistry updateSrEntry;

            final Optional<SystemRegistry> srEntryOptional = systemRegistryRepository.findById(id);
            srEntry = srEntryOptional.orElseThrow(() -> new InvalidParameterException("System Registry entry with id '" + id + "' does not exist"));

            final System systemDb = mergeSystem(request.getSystem(), srEntry.getSystem());
            final Device deviceDb = mergeDevice(request.getProvider(), srEntry.getDevice());

            final ZonedDateTime endOfValidity = Utilities.notEmpty(request.getEndOfValidity()) ?
                    Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim()) :
                    srEntry.getEndOfValidity();

            final String validatedMetadataStr = Objects.nonNull(request.getMetadata()) ?
                    Utilities.map2Text(request.getMetadata()) :
                    srEntry.getMetadata();

            final int validatedVersion = (request.getVersion() != null) ? request.getVersion() : srEntry.getVersion();

            updateSrEntry = updateSystemRegistry(srEntry, systemDb, deviceDb, endOfValidity, validatedMetadataStr, validatedVersion);

            return DTOConverter.convertSystemRegistryToSystemRegistryResponseDTO(updateSrEntry);
        } catch (final InvalidParameterException | IllegalArgumentException ex) {
            throw ex;
        } catch (final DateTimeParseException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new InvalidParameterException(
                    "End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.", ex);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryListResponseDTO getSystemRegistryEntriesBySystemName(final String systemName, final CoreUtilities.ValidatedPageParams pageParameters,
                                                                              final String sortField) {
        logger.debug("getSystemRegistryEntriesBySystemName is started...");
        Assert.notNull(systemName, "System name is null.");

        if (!System.SORTABLE_FIELDS_BY.contains(sortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + sortField + "' is not available");
        }

        final List<System> systemList = systemRepository.findBySystemName(systemName.toLowerCase().trim());
        final PageRequest pageRequest = PageRequest.of(pageParameters.getValidatedPage(), pageParameters.getValidatedSize(),
                                                       pageParameters.getValidatedDirection(), sortField);

        final Page<SystemRegistry> systemRegistries = systemRegistryRepository.findAllBySystemIsIn(systemList, pageRequest);
        return DTOConverter.convertSystemRegistryListToSystemRegistryListResponseDTO(systemRegistries);
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeSystemRegistryByNameAndAddressAndPort(final String systemName, final String address, final int port) {
        logger.debug("removeSystemRegistryByNameAndAddressAndPort is started...");
        final System system = getSystemByNameAndAddressAndPort(systemName, address, port);

        final List<SystemRegistry> entries = systemRegistryRepository.findBySystem(system);
        if (entries.isEmpty()) {
            throw new InvalidParameterException("System Registry entry for System with name '" + systemName +
                                                        "', address '" + address +
                                                        "' and port '" + port + "' does not exist");
        }

        systemRegistryRepository.deleteInBatch(entries);
        systemRegistryRepository.flush();

        entries.forEach(this::publishUnregister);
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemQueryResultDTO queryRegistry(final SystemQueryFormDTO form) { 
        logger.debug("queryRegistry is started...");
        Assert.notNull(form, "Form is null.");

        try {
            final List<System> systems;
            final List<Device> devices;
            final List<SystemRegistry> registryList;
            final int unfilteredHits;

            if (Utilities.notEmpty(form.getSystemNameRequirements())) {
                final String systemName = Utilities.lowerCaseTrim(form.getSystemNameRequirements());
                systems = systemRepository.findBySystemName(systemName);

                if (systems.isEmpty()) {
                    // no system found
                    logger.debug("System not found: {}", systemName);
                    return DTOConverter.convertSystemRegistryListToSystemQueryResultDTO(Collections.emptyList());
                }
            } else {
                throw new InvalidParameterException("System Name must not be null");
            }

            registryList = new ArrayList<>(systemRegistryRepository.findAllBySystemIsIn(systems));
            unfilteredHits = registryList.size();

            if (Utilities.notEmpty(form.getDeviceNameRequirements())) {
                final String deviceName = Utilities.lowerCaseTrim(form.getDeviceNameRequirements());
                devices = deviceRepository.findByDeviceName(deviceName);

                if (devices.isEmpty()) {
                    // no device found
                    logger.debug("Device not found: {}", deviceName);
                    return DTOConverter.convertSystemRegistryListToSystemQueryResultDTO(Collections.emptyList());
                }

                registryList.removeIf(e -> !devices.contains(e.getDevice()));
            }

            // filter on address type
            if (Objects.nonNull(form.getAddressTypeRequirements()) && !form.getAddressTypeRequirements().isEmpty()) {
            	final List<AddressType> normalizedAddressTypeList = form.getAddressTypeRequirements().parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
            	registryList.removeIf(e -> e.getSystem().getAddressType() == null || !normalizedAddressTypeList.contains(e.getSystem().getAddressType()));
            }
            
            // filter on version
            if (Objects.nonNull(form.getVersionRequirement())) {
                registryList.removeIf(e -> Objects.equals(form.getVersionRequirement(), e.getVersion()));
            } else if (Objects.nonNull(form.getMinVersionRequirement()) || Objects.nonNull(form.getMaxVersionRequirement())) {
                final int minVersion = form.getMinVersionRequirement() == null ? 1 : form.getMinVersionRequirement();
                final int maxVersion = form.getMaxVersionRequirement() == null ? Integer.MAX_VALUE : form.getMaxVersionRequirement();

                registryList.removeIf(e -> (e.getVersion() < minVersion) || (e.getVersion() > maxVersion));
            }

            // filter on metadata
            if (form.getMetadataRequirements() != null && !form.getMetadataRequirements().isEmpty()) {
                final Map<String, String> requiredMetadata = normalizeMetadata(form.getMetadataRequirements());
                registryList.removeIf(e ->
                                      {
                                          final Map<String, String> metadata = Utilities.text2Map(e.getMetadata());
                                          if (Objects.isNull(metadata)) {
                                              // we have requirements but no metadata -> remove
                                              return true;
                                          }

                                          return !metadata.entrySet().containsAll(requiredMetadata.entrySet());
                                      });
            }

            // filter on ping
            if (form.getPingProviders()) {
                filterOnPing(registryList, pingTimeout);
            }

            logger.debug("Potential system providers after filtering: {}", registryList.size());
            return DTOConverter.convertListOfSystemRegistryEntriesToSystemQueryResultDTO(registryList, unfilteredHits);
        } catch (final IllegalStateException e) {
            throw new InvalidParameterException("Invalid keys in the metadata requirements (whitespace only differences)");
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO getSystemDtoByNameAndAddressAndPort(final String systemName, final String address, final int port) {
        final System system = getSystemByNameAndAddressAndPort(systemName, address, port);
        return DTOConverter.convertSystemToSystemResponseDTO(system);
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
	public SystemRegistryOnboardingWithNameResponseDTO onboardAndRegisterSystemRegistry(final SystemRegistryOnboardingWithNameRequestDTO request,
                                                                                        final String host, final String address) {
        logger.debug("onboardAndRegisterSystemRegistry started...");

        final CertificateCreationRequestDTO creationRequestDTO = request.getCertificateCreationRequest();
        final KeyPair keyPair = securityUtilities.extractOrGenerateKeyPair(creationRequestDTO);
        final String certificateSigningRequest;

        try {
            certificateSigningRequest = securityUtilities
                    .createCertificateSigningRequest(creationRequestDTO.getCommonName(), keyPair, CertificateType.AH_SYSTEM, host, address);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new ArrowheadException("Unable to create certificate signing request: " + e.getMessage());
        }

        final var signingResponse = signCertificate(certificateSigningRequest);
        signingResponse.setKeyPairDTO(securityUtilities.encodeKeyPair(keyPair));

        final var retValue = new SystemRegistryOnboardingWithNameResponseDTO();
        retValue.setCertificateResponse(signingResponse);
        retValue.load(registerSystemRegistry(request));
        return retValue;
    }

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingWithCsrResponseDTO onboardAndRegisterSystemRegistry(final SystemRegistryOnboardingWithCsrRequestDTO request) {

        logger.debug("onboardAndRegisterDeviceRegistry started...");
        final var signingResponse = signCertificate(request.getCertificateSigningRequest());
        securityUtilities.extractAndSetPublicKey(signingResponse);

        final var retValue = new SystemRegistryOnboardingWithCsrResponseDTO();
        retValue.setCertificateResponse(signingResponse);
        retValue.load(registerSystemRegistry(request));
        return retValue;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private void publishRegister(final SystemRegistryRequestDTO requestDTO) {
        try {
            eventDriver.publish(
                    new EventPublishRequestDTO(CoreEventHandlerConstants.REGISTER_SYSTEM_EVENT,
                                               driverUtilities.getCoreSystemRequestDTO(),
                                               null,
                                               eventDriver.convert(requestDTO),
                                               Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now())
                    )
            );
        } catch (final Exception e) {
            logger.warn("Unable to publish register event: {}", e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void publishUnregister(final SystemRegistry systemRegistry) {
        try {
            final var device = systemRegistry.getDevice();
            final var system = systemRegistry.getSystem();

            final var deviceRequestDTO = new DeviceRequestDTO(device.getDeviceName(),
                                                              device.getAddress(),
                                                              device.getMacAddress(),
                                                              device.getAuthenticationInfo());

            final var systemRequestDTO = new SystemRequestDTO(system.getSystemName(),
                                                              system.getAddress(),
                                                              system.getPort(),
                                                              system.getAuthenticationInfo(),
                                                              Utilities.text2Map(system.getMetadata()));

            final var requestDTO = new SystemRegistryRequestDTO(systemRequestDTO,
                                                                deviceRequestDTO,
                                                                Utilities.convertZonedDateTimeToUTCString(systemRegistry.getEndOfValidity()),
                                                                Utilities.text2Map(systemRegistry.getMetadata()),
                                                                systemRegistry.getVersion());
            eventDriver.publish(
                    new EventPublishRequestDTO(CoreEventHandlerConstants.UNREGISTER_SYSTEM_EVENT,
                                               driverUtilities.getCoreSystemRequestDTO(),
                                               null,
                                               eventDriver.convert(requestDTO),
                                               Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now())
                    )
            );
        } catch (final Exception e) {
            logger.warn("Unable to publish unregister event: {}", e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    private System getSystemByNameAndAddressAndPort(final String systemName, final String address, final int port) {
        final String dbSystemName = Utilities.lowerCaseTrim(systemName);
        final String dbAddress = networkAddressPreProcessor.normalize(address);
        final Optional<System> optionalSystem = systemRepository.findBySystemNameAndAddressAndPort(dbSystemName, dbAddress, port);
        return optionalSystem.orElseThrow(() -> new InvalidParameterException(
                "System entry with name '" + dbSystemName + "', address '" + dbAddress + "' and port '" + port + "' does not exist"));
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("squid:S1126")
    private boolean checkSystemIfUniqueValidationNeeded(final System system, final String validatedSystemName, final String validatedAddress,
                                                        final Integer validatedPort) {
        logger.debug("checkSystemIfUniqueValidationNeeded started...");

        final String actualSystemName = system.getSystemName();
        final String actualAddress = system.getAddress();
        final int actualPort = system.getPort();

        if (validatedSystemName != null && !actualSystemName.equalsIgnoreCase(validatedSystemName)) {
            return true;
        } else if (validatedAddress != null && !actualAddress.equalsIgnoreCase(validatedAddress)) {
            return true;
        } else {
            return validatedPort != null && actualPort != validatedPort;
        }
    }

    //-------------------------------------------------------------------------------------------------
    // This method may CHANGE the content of providedServices
    private void filterOnPing(final List<SystemRegistry> systems, final int timeout) {
        if (systems == null || systems.isEmpty() || timeout <= 0) {
            return;
        }

        systems.removeIf(sr -> !pingService(sr.getSystem().getAddress(), sr.getSystem().getPort(), timeout));
    }

    //-------------------------------------------------------------------------------------------------
    private boolean pingService(final String address, final int port, final int timeout) {
        final InetSocketAddress host = new InetSocketAddress(address, port);
        try (final Socket socket = new Socket()) {
            socket.connect(host, timeout);
            return true;
        } catch (final IOException ex) {
            return false;
        }
    }

    //-------------------------------------------------------------------------------------------------
    private Map<String, String> normalizeMetadata(final Map<String, String> metadata) throws IllegalStateException {
        logger.debug("normalizeMetadata started...");
        if (metadata == null) {
            return Map.of();
        }

        final Map<String, String> map = new HashMap<>();

        metadata.forEach((k, v) -> {
                             if (Objects.nonNull(v)) {
                                 map.put(k.trim(), v.trim());
                             }
                         });

        return map;
    }

    //-------------------------------------------------------------------------------------------------
    private void checkConstraintsOfSystemTable(final String validatedSystemName, final String validatedAddress, final int validatedPort) {
        logger.debug("checkConstraintsOfSystemTable started...");

        try {
            final Optional<System> find = systemRepository
                    .findBySystemNameAndAddressAndPort(validatedSystemName.toLowerCase().trim(), validatedAddress.toLowerCase().trim(), validatedPort);
            if (find.isPresent()) {
                throw new InvalidParameterException(
                        "System with name: " + validatedSystemName + ", address: " + validatedAddress + ", port: " + validatedPort + " already exists.");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkConstraintsOfDeviceTable(final String validatedDeviceName, final String validatedMacAddress) {
        logger.debug("checkConstraintsOfDeviceTable started...");

        try {
            final Optional<Device> find = deviceRepository.findByDeviceNameAndMacAddress(validatedDeviceName, validatedMacAddress);
            if (find.isPresent()) {
                throw new InvalidParameterException(
                        "Device with name: " + validatedDeviceName + ", MAC address: " + validatedMacAddress + " already exists.");
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

        final String normalizedAddress = networkAddressPreProcessor.normalize(address);

        validateNonNullParameters(systemName, normalizedAddress);

        final AddressType addressType = networkAddressTypeDetector.detectAddressType(normalizedAddress);

        if (!cnVerifier.isValid(systemName)) {
        	throw new InvalidParameterException("System name" + INVALID_FORMAT_ERROR_MESSAGE);
        }

        if (port <= CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
        }

        final String validatedSystemName = Utilities.lowerCaseTrim(systemName);
        final String validatedAddress = normalizedAddress;

        checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);

        return new System(validatedSystemName, validatedAddress, addressType, port, authenticationInfo, Utilities.map2Text(metadata));
    }

    //-------------------------------------------------------------------------------------------------
    private Device validateNonNullDeviceParameters(final String deviceName, final String address, final String macAddress, final String authenticationInfo) {
        logger.debug("validateNonNullDeviceParameters started...");

        final String normalizedAddress = networkAddressPreProcessor.normalize(address);
        validateNonNullParameters(deviceName, normalizedAddress);

        if (Utilities.isEmpty(macAddress)) {
            throw new InvalidParameterException("MAC address is null or empty");
        }

        final String validatedDeviceName = Utilities.lowerCaseTrim(deviceName);
        final String validatedAddress = normalizedAddress;
        final String validatedMacAddress = Utilities.lowerCaseTrim(macAddress);

        if (!Utilities.isValidMacAddress(validatedMacAddress)) {
            throw new BadPayloadException("Unrecognized format of MAC Address", HttpStatus.SC_BAD_REQUEST);
        }

        checkConstraintsOfDeviceTable(validatedDeviceName, validatedMacAddress);

        return new Device(validatedDeviceName, validatedAddress, validatedMacAddress, authenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private void validateNonNullParameters(final String name, final String address) {
        logger.debug("validateNonNullParameters started...");

        if (Utilities.isEmpty(name)) {
            throw new InvalidParameterException("Name is null or empty");
        }

        networkAddressVerifier.verify(address);

        if (name.contains(".")) {
            throw new InvalidParameterException("Name can't contain dot (.)");
        }
    }

    //-------------------------------------------------------------------------------------------------
    private String validateParamString(final String param) {
        logger.debug("validateSystemParamString started...");

        if (Utilities.isEmpty(param)) {
            throw new InvalidParameterException("parameter null or empty");
        }

        return Utilities.lowerCaseTrim(param);
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
    private long validateId(final long id) {
        logger.debug("validateId started...");

        if (id < 1) {
            throw new IllegalArgumentException("Id must be greater than zero");
        }

        return id;
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
    private String validateAllowNullParamString(final String param) {
        logger.debug("validateAllowNullParamString started...");

        if (Utilities.isEmpty(param)) {
            return null;
        }

        return Utilities.lowerCaseTrim(param);
    }


    //-------------------------------------------------------------------------------------------------
    private SystemRegistry getSystemRegistryEntryById(final long id) {
        logger.debug("getSystemRegistryEntryById started...");
        try {
            final Optional<SystemRegistry> systemRegistry = systemRegistryRepository.findById(id);
            return systemRegistry.orElseThrow(() -> new InvalidParameterException("System Registry with id of '" + id + "' does not exist"));
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private System findOrCreateSystem(final SystemRequestDTO requestSystemDto) {
        return findOrCreateSystem(requestSystemDto.getSystemName(),
                                  requestSystemDto.getAddress(),
                                  requestSystemDto.getPort(),
                                  requestSystemDto.getAuthenticationInfo(),
                                  requestSystemDto.getMetadata());
    }

    //-------------------------------------------------------------------------------------------------
    private System findOrCreateSystem(final String name, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {

        final String validatedName = Utilities.lowerCaseTrim(name);
        final String validatedAddress = networkAddressPreProcessor.normalize(address);
        networkAddressVerifier.verify(validatedAddress);

        final Optional<System> optSystem = systemRepository.findBySystemNameAndAddressAndPort(validatedName, validatedAddress, port);
        System provider;

        if (optSystem.isPresent()) {
            provider = optSystem.get();
            final String metadataStr = Utilities.map2Text(metadata);
            if (!Objects.equals(authenticationInfo, provider.getAuthenticationInfo()) ||
                !Objects.equals(validatedAddress, provider.getAddress()) ||
                !Objects.equals(metadataStr, provider.getMetadata())) { // authentication info or system has changed
                provider.setAuthenticationInfo(authenticationInfo);
                provider.setAddress(validatedAddress);
                provider.setMetadata(metadataStr);
                provider = systemRepository.saveAndFlush(provider);
            }
        } else {
            provider = createSystem(validatedName, validatedAddress, port, authenticationInfo, metadata);
        }
        return provider;
    }

    //-------------------------------------------------------------------------------------------------
    private Device findOrCreateDevice(final DeviceRequestDTO requestDeviceDto) {
        return findOrCreateDevice(requestDeviceDto.getDeviceName(),
                                  requestDeviceDto.getAddress(),
                                  requestDeviceDto.getMacAddress(),
                                  requestDeviceDto.getAuthenticationInfo());
    }

    //-------------------------------------------------------------------------------------------------
    private Device findOrCreateDevice(final String deviceName, final String address, final String macAddress, final String authenticationInfo) {

        final String validateName = Utilities.lowerCaseTrim(deviceName);
        final String validateAddress = networkAddressPreProcessor.normalize(address);
        networkAddressVerifier.verify(validateAddress);
        final String validatedMacAddress = Utilities.lowerCaseTrim(macAddress);

        if (!Utilities.isValidMacAddress(validatedMacAddress)) {
            throw new BadPayloadException("Unrecognized format of MAC Address", HttpStatus.SC_BAD_REQUEST);
        }

        final Optional<Device> optProvider = deviceRepository.findByDeviceNameAndMacAddress(validateName, validatedMacAddress);
        Device provider;

        if (optProvider.isPresent()) {
            provider = optProvider.get();
            if (!Objects.equals(authenticationInfo, provider.getAuthenticationInfo()) ||
                    !Objects.equals(validateAddress, provider.getAddress())) { // authentication info or provider has changed
                provider.setAuthenticationInfo(authenticationInfo);
                provider.setAddress(validateAddress);
                provider = deviceRepository.saveAndFlush(provider);
            }
        } else {
            provider = createDevice(validateName, validateAddress, validatedMacAddress, authenticationInfo);
        }
        return provider;
    }

    //-------------------------------------------------------------------------------------------------
    private Device createDevice(final String name, final String address, final String macAddress, final String authenticationInfo) {
    	final String normalizedAddress = networkAddressPreProcessor.normalize(address);
    	networkAddressVerifier.verify(normalizedAddress);
        final Device device = new Device(name, normalizedAddress, macAddress, authenticationInfo);
        return deviceRepository.saveAndFlush(device);
    }

    //-------------------------------------------------------------------------------------------------
    private void checkSystemRegistryRequest(final SystemRegistryRequestDTO request) {
        logger.debug("checkSystemRegistryRequest started...");
        Assert.notNull(request, "Request is null.");

        Assert.notNull(request.getSystem(), "System is not specified.");
        Assert.isTrue(Utilities.notEmpty(request.getSystem().getSystemName()), "System name is not specified.");
        Assert.isTrue(cnVerifier.isValid(request.getSystem().getSystemName()), "System name" + INVALID_FORMAT_ERROR_MESSAGE);
        try {
			networkAddressVerifier.verify(request.getSystem().getAddress());
		} catch (final InvalidParameterException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
        Assert.notNull(request.getSystem().getPort(), "System port is not specified.");

        Assert.notNull(request.getProvider(), "Provider Device is not specified.");
        Assert.isTrue(Utilities.notEmpty(request.getProvider().getDeviceName()), "Provider Device name is not specified.");
        try {
			networkAddressVerifier.verify(request.getProvider().getAddress());
		} catch (final InvalidParameterException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
        Assert.isTrue(Utilities.notEmpty(request.getProvider().getMacAddress()), "Provider Device MAC is not specified.");
    }

    //-------------------------------------------------------------------------------------------------
    private void checkConstraintsOfSystemRegistryTable(final System systemDb, final Device deviceDb) {
        logger.debug("checkConstraintOfSystemRegistryTable started...");

        try {
            final Optional<SystemRegistry> find = systemRegistryRepository.findBySystemAndDevice(systemDb, deviceDb);
            if (find.isPresent()) {
                throw new InvalidParameterException("System Registry entry with provider: (" + deviceDb.getDeviceName() + ", " + deviceDb.getMacAddress() +
                                                            ") and system : " + systemDb.getSystemName() + " already exists.");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private Device mergeDevice(final DeviceRequestDTO request, final Device device) {
        final String name = Utilities.firstNotNullIfExists(request.getDeviceName(), device.getDeviceName());
        final String address = Utilities.firstNotNullIfExists(networkAddressPreProcessor.normalize(request.getAddress()), device.getAddress());
        networkAddressVerifier.verify(address);
        final String macAddress = Utilities.firstNotNullIfExists(request.getDeviceName(), device.getDeviceName());
        final String authenticationInfo = Utilities.firstNotNullIfExists(request.getAuthenticationInfo(), device.getAuthenticationInfo());

        return findOrCreateDevice(name, address, macAddress, authenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private System mergeSystem(final SystemRequestDTO request, final System system) {
        final String name = Utilities.firstNotNullIfExists(request.getSystemName(), system.getSystemName());
        Assert.isTrue(cnVerifier.isValid(name), "System name" + INVALID_FORMAT_ERROR_MESSAGE);
        final String address = Utilities.firstNotNullIfExists(networkAddressPreProcessor.normalize(request.getAddress()), system.getAddress());
        networkAddressVerifier.verify(address);
        final int port = request.getPort() > 0 ? request.getPort() : system.getPort();
        final String authenticationInfo = Utilities.firstNotNullIfExists(request.getAuthenticationInfo(), system.getAuthenticationInfo());
        final Map<String,String> metadata = request.getMetadata() != null ? request.getMetadata() : Utilities.text2Map(system.getMetadata());

        return findOrCreateSystem(name, address, port, authenticationInfo, metadata);
    }

    //-------------------------------------------------------------------------------------------------
    private SystemRegistry createSystemRegistry(final System systemDb, final Device deviceDb, final ZonedDateTime endOfValidity, final String metadataStr,
                                                final int version) {
        logger.debug("createSystemRegistry started...");

        checkConstraintsOfSystemRegistryTable(systemDb, deviceDb);

        try {
            return systemRegistryRepository.saveAndFlush(new SystemRegistry(systemDb, deviceDb, endOfValidity, metadataStr, version));
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private boolean checkSystemRegistryIfUniqueValidationNeeded(final SystemRegistry srEntry, final System system, final Device device) {
        logger.debug("checkSystemRegistryIfUniqueValidationNeeded started...");

        return srEntry.getSystem().getId() != system.getId() || srEntry.getDevice().getId() != device.getId();

    }

    //-------------------------------------------------------------------------------------------------
    private SystemRegistry updateSystemRegistry(final SystemRegistry srEntry, final System system, final Device device,
                                                final ZonedDateTime endOfValidity, final String metadataStr, final int version) {
        logger.debug("updateSystemRegistry started...");
        Assert.notNull(srEntry, "SystemRegistry Entry is not specified.");
        Assert.notNull(system, "System is not specified.");
        Assert.notNull(device, "Device is not specified.");

        if (checkSystemRegistryIfUniqueValidationNeeded(srEntry, system, device)) {
            checkConstraintsOfSystemRegistryTable(system, device);
        }

        return setModifiedValuesOfSystemRegistryEntryFields(srEntry, system, device, endOfValidity, metadataStr, version);
    }

    //-------------------------------------------------------------------------------------------------
    private ZonedDateTime getZonedDateTime(final SystemRegistryRequestDTO request) {
        return Utilities.isEmpty(request.getEndOfValidity()) ? null : Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
    }

    //-------------------------------------------------------------------------------------------------
    private CertificateCreationResponseDTO signCertificate(final String signingRequest) {
        logger.debug("Contact CertificateAuthority ...");
        final var csrDTO = new CertificateSigningRequestDTO(signingRequest);
        final var signingResponse = caDriver.signCertificate(csrDTO);

        logger.debug("Processing response from Certificate Authority ...");
        final CertificateCreationResponseDTO certificateResponseDTO = new CertificateCreationResponseDTO();
        certificateResponseDTO.setCertificate(signingResponse.getCertificateChain().get(0));
        certificateResponseDTO.setCertificateFormat(CoreCommonConstants.CERTIFICATE_FORMAT);
        certificateResponseDTO.setCertificateType(CertificateType.AH_DEVICE);
        return certificateResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    private SystemRegistry setModifiedValuesOfSystemRegistryEntryFields(final SystemRegistry srEntry, final System system, final Device device,
                                                                        final ZonedDateTime endOfValidity, final String metadataStr, final int version) {

        logger.debug("setModifiedValuesOfSystemRegistryEntryFields started...");

        try {
            srEntry.setSystem(system);
            srEntry.setDevice(device);
            srEntry.setEndOfValidity(endOfValidity);
            srEntry.setMetadata(metadataStr);
            srEntry.setVersion(version);

            return systemRegistryRepository.saveAndFlush(srEntry);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
}
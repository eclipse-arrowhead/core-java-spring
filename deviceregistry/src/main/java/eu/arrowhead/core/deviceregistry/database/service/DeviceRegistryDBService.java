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

package eu.arrowhead.core.deviceregistry.database.service;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreEventHandlerConstants;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.DeviceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.DeviceRegistryRepository;
import eu.arrowhead.common.database.repository.DeviceRepository;
import eu.arrowhead.common.drivers.CertificateAuthorityDriver;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.drivers.EventDriver;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.internal.DeviceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.DeviceQueryFormDTO;
import eu.arrowhead.common.dto.shared.DeviceQueryResultDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithNameResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeviceRegistryDBService {

    //=================================================================================================
    // members

    private static final String COULD_NOT_DELETE_DEVICE_ERROR_MESSAGE = "Could not delete Device, with given parameters";

    private final Logger logger = LogManager.getLogger(DeviceRegistryDBService.class);

    private final DeviceRegistryRepository deviceRegistryRepository;
    private final DeviceRepository deviceRepository;
    private final SecurityUtilities securityUtilities;
    private final CertificateAuthorityDriver caDriver;
    private final EventDriver eventHandlerDriver;
    private final DriverUtilities driverUtilities;

    @Autowired
    public DeviceRegistryDBService(final DeviceRegistryRepository deviceRegistryRepository,
                                   final DeviceRepository deviceRepository,
                                   final SecurityUtilities securityUtilities,
                                   final CertificateAuthorityDriver caDriver,
                                   final EventDriver eventHandlerDriver,
                                   final DriverUtilities driverUtilities) {
        this.deviceRegistryRepository = deviceRegistryRepository;
        this.deviceRepository = deviceRepository;
        this.securityUtilities = securityUtilities;
        this.caDriver = caDriver;
        this.eventHandlerDriver = eventHandlerDriver;
        this.driverUtilities = driverUtilities;
    }


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public DeviceResponseDTO getDeviceById(long deviceId) {
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
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceRegistryOnboardingWithNameResponseDTO onboardAndRegisterDeviceRegistry(final DeviceRegistryOnboardingWithNameRequestDTO request,
                                                                                        final String host, final String address) {
        logger.debug("onboardAndRegisterDeviceRegistry started...");
        Assert.notNull(request, "DeviceRegistryOnboardingWithNameRequestDTO must not be null");

        final CertificateCreationRequestDTO creationRequestDTO = request.getCertificateCreationRequest();
        final KeyPair keyPair = securityUtilities.extractOrGenerateKeyPair(creationRequestDTO);
        final String certificateSigningRequest;

        try {
            certificateSigningRequest = securityUtilities
                    .createCertificateSigningRequest(creationRequestDTO.getCommonName(), keyPair, CertificateType.AH_DEVICE, host, address);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new ArrowheadException("Unable to create certificate signing request: " + e.getMessage());
        }

        final CertificateCreationResponseDTO signingResponse = signCertificate(certificateSigningRequest);
        signingResponse.setKeyPairDTO(securityUtilities.encodeKeyPair(keyPair));

        final DeviceRegistryOnboardingWithNameResponseDTO retValue = new DeviceRegistryOnboardingWithNameResponseDTO();
        retValue.setCertificateResponse(signingResponse);
        retValue.load(registerDeviceRegistry(request));
        return retValue;
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceRegistryOnboardingWithCsrResponseDTO onboardAndRegisterDeviceRegistry(final DeviceRegistryOnboardingWithCsrRequestDTO request) {

        logger.debug("onboardAndRegisterDeviceRegistry started...");
        Assert.notNull(request, "DeviceRegistryOnboardingWithCsrRequestDTO must not be null");

        final CertificateCreationResponseDTO signingResponse = signCertificate(request.getCertificateSigningRequest());
        securityUtilities.extractAndSetPublicKey(signingResponse);

        final DeviceRegistryOnboardingWithCsrResponseDTO retValue = new DeviceRegistryOnboardingWithCsrResponseDTO();
        retValue.setCertificateResponse(signingResponse);
        retValue.load(registerDeviceRegistry(request));
        return retValue;
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceListResponseDTO getDeviceEntries(final CoreUtilities.ValidatedPageParams pageParams, final String sortField) {
        logger.debug("getDeviceList started...");
        Assert.notNull(pageParams, "ValidatedPageParams must not be null");

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
    public DeviceResponseDTO mergeDevice(final long deviceId, final DeviceRequestDTO request) {
        logger.debug("mergeDevice started...");
        Assert.notNull(request, "DeviceRequestDTO must not be null");

        try {
            final Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
            final Device device = deviceOptional.orElseThrow(() -> new InvalidParameterException("Device with id " + deviceId + " not found."));

            return DTOConverter.convertDeviceToDeviceResponseDTO(mergeDevice(request, device));
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceRegistryListResponseDTO getDeviceRegistryEntries(final CoreUtilities.ValidatedPageParams params, final String sortField) {
        logger.debug("getDeviceRegistryEntries started...");
        Assert.notNull(params, "ValidatedPageParams must not be null");

        final String validatedSortField = sortField == null ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!DeviceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            final PageRequest pageRequest = PageRequest.of(params.getValidatedPage(),
                                                           params.getValidatedSize(),
                                                           params.getValidatedDirection(),
                                                           validatedSortField);
            final Page<DeviceRegistry> deviceRegistryPage = deviceRegistryRepository.findAll(pageRequest);
            return DTOConverter.convertDeviceRegistryListToDeviceRegistryListResponseDTO(deviceRegistryPage);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceRegistryListResponseDTO getDeviceRegistryEntriesByDeviceName(final String deviceName,
                                                                              final CoreUtilities.ValidatedPageParams params,
                                                                              final String sortField) {
        logger.debug("getDeviceRegistryEntriesByDeviceName started...");
        Assert.notNull(params, "ValidatedPageParams must not be null");

        final String validatedSortField = sortField == null ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!DeviceRegistry.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            final List<Device> devices = deviceRepository.findByDeviceName(Utilities.lowerCaseTrim(deviceName));

            final PageRequest pageRequest = PageRequest.of(params.getValidatedPage(),
                                                           params.getValidatedSize(),
                                                           params.getValidatedDirection(),
                                                           validatedSortField);
            final Page<DeviceRegistry> deviceRegistryPage = deviceRegistryRepository.findAllByDeviceIsIn(devices, pageRequest);
            return DTOConverter.convertDeviceRegistryListToDeviceRegistryListResponseDTO(deviceRegistryPage);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceRegistryResponseDTO getDeviceRegistryById(final long id) {
        logger.debug("getDeviceRegistryById started...");

        return DTOConverter.convertDeviceRegistryToDeviceRegistryResponseDTO(getDeviceRegistryEntryById(id));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeDeviceRegistryEntryById(final long id) {
        logger.debug("removeDeviceRegistryEntryById started...");

        try {
            if (!deviceRegistryRepository.existsById(id)) {
                throw new InvalidParameterException("Device Registry entry with id '" + id + "' does not exist");
            }

            final Optional<DeviceRegistry> optionalDeviceRegistry = deviceRegistryRepository.findById(id);
            deviceRegistryRepository.deleteById(id);
            deviceRegistryRepository.flush();

            optionalDeviceRegistry.ifPresent(this::publishUnregister);

        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceRegistryResponseDTO registerDeviceRegistry(final DeviceRegistryRequestDTO request) {
        logger.debug("registerDeviceRegistry started...");
        checkDeviceRegistryRequest(request);

        try {
            final Device deviceDb = findOrCreateDevice(request.getDevice());

            final ZonedDateTime endOfValidity = getZonedDateTime(request);
            final String metadataStr = Utilities.map2Text(request.getMetadata());
            final int version = (request.getVersion() != null) ? request.getVersion() : 1;
            final DeviceRegistry drEntry = createDeviceRegistry(deviceDb, endOfValidity, metadataStr, version);

            publishRegister(request);

            return DTOConverter.convertDeviceRegistryToDeviceRegistryResponseDTO(drEntry);
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
    public DeviceRegistryResponseDTO updateDeviceRegistryById(final long id, final DeviceRegistryRequestDTO request) {
        logger.debug("updateDeviceRegistryById started...");

        Assert.isTrue(0 < id, "id is not greater than zero");
        checkDeviceRegistryRequest(request);

        try {
            final DeviceRegistry drEntry;
            final DeviceRegistry updateDrEntry;

            final Optional<DeviceRegistry> drEntryOptional = deviceRegistryRepository.findById(id);
            drEntry = drEntryOptional.orElseThrow(() -> new InvalidParameterException("Device Registry entry with id '" + id + "' does not exist"));

            final Device deviceDb = findOrCreateDevice(request.getDevice());

            final ZonedDateTime endOfValidity = getZonedDateTime(request);
            final String metadataStr = Utilities.map2Text(request.getMetadata());
            final int version = (request.getVersion() != null) ? request.getVersion() : 1;
            updateDrEntry = updateDeviceRegistry(drEntry, deviceDb, endOfValidity, metadataStr, version);

            return DTOConverter.convertDeviceRegistryToDeviceRegistryResponseDTO(updateDrEntry);
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
    public DeviceRegistryResponseDTO mergeDeviceRegistryById(final long id, final DeviceRegistryRequestDTO request) {
        logger.debug("mergeDeviceRegistryById started...");
        Assert.notNull(request, "request is null.");
        Assert.isTrue(0 < id, "id is not greater than zero");

        try {
            final DeviceRegistry drEntry;
            final DeviceRegistry updateDrEntry;

            final Optional<DeviceRegistry> drEntryOptional = deviceRegistryRepository.findById(id);
            drEntry = drEntryOptional.orElseThrow(() -> new InvalidParameterException("System Registry entry with id '" + id + "' does not exist"));

            final Device deviceDb = mergeDevice(request.getDevice(), drEntry.getDevice());

            final ZonedDateTime endOfValidity = Utilities.notEmpty(request.getEndOfValidity()) ?
                    Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim()) :
                    drEntry.getEndOfValidity();

            final String validatedMetadataStr = Objects.nonNull(request.getMetadata()) ?
                    Utilities.map2Text(request.getMetadata()) :
                    drEntry.getMetadata();

            final int validatedVersion = (request.getVersion() != null) ? request.getVersion() : drEntry.getVersion();

            updateDrEntry = updateDeviceRegistry(drEntry, deviceDb, endOfValidity, validatedMetadataStr, validatedVersion);

            return DTOConverter.convertDeviceRegistryToDeviceRegistryResponseDTO(updateDrEntry);
        } catch (final InvalidParameterException ex) {
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
    public DeviceResponseDTO getDeviceDtoByNameAndMacAddress(final String deviceName, final String macAddress) {
        final Device device = getDeviceByNameAndMacAddress(deviceName, macAddress);
        return DTOConverter.convertDeviceToDeviceResponseDTO(device);
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeDeviceRegistryByNameAndMacAddress(final String deviceName, final String macAddress) {
        logger.debug("removeDeviceRegistryByNameAndMacAddress is started...");
        final Device device = getDeviceByNameAndMacAddress(deviceName, macAddress);

        final Optional<DeviceRegistry> optionalDeviceRegistry = deviceRegistryRepository.findByDevice(device);
        final DeviceRegistry deviceRegistry = optionalDeviceRegistry.orElseThrow(
                () -> new InvalidParameterException(
                        "Device Registry entry for System with name '" + deviceName +
                                "' and MAC address '" + macAddress + "' does not exist"));

        deviceRegistryRepository.delete(deviceRegistry);
        deviceRegistryRepository.flush();

        publishUnregister(deviceRegistry);
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceQueryResultDTO queryRegistry(final DeviceQueryFormDTO form) {
        logger.debug("queryRegistry is started...");
        Assert.notNull(form, "Form is null.");

        try {
            final List<Device> devices;
            final List<DeviceRegistry> registryList;
            final int unfilteredHits;

            if (Utilities.isEmpty(form.getDeviceNameRequirements())) {
                throw new InvalidParameterException("Device Name must not be null");
            }

            final String deviceName = Utilities.lowerCaseTrim(form.getDeviceNameRequirements());

            if (Utilities.notEmpty(form.getMacAddressRequirement())) {
                // exact match or no match
                final String macAddress = Utilities.lowerCaseTrim(form.getMacAddressRequirement());
                final Optional<Device> optionalDevice = deviceRepository.findByDeviceNameAndMacAddress(deviceName, macAddress);

                if (optionalDevice.isPresent()) {
                    devices = new ArrayList<>();
                    devices.add(optionalDevice.get());
                } else {
                    return DTOConverter.convertDeviceRegistryListToDeviceQueryResultDTO(List.of());
                }
            } else {
                devices = deviceRepository.findByDeviceName(deviceName);

                if (devices.isEmpty()) {
                    logger.debug("Device not found: {}", deviceName);
                    return DTOConverter.convertDeviceRegistryListToDeviceQueryResultDTO(List.of());
                }
            }

            registryList = deviceRegistryRepository.findAllByDeviceIsIn(devices);
            unfilteredHits = registryList.size();

            if (Utilities.notEmpty(form.getAddressRequirement())) {
                final String address = Utilities.lowerCaseTrim(form.getAddressRequirement());
                devices.removeIf(e -> !e.getAddress().startsWith(address));
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

            logger.debug("Potential devices after filtering: {}", registryList.size());
            return DTOConverter.convertListOfDeviceRegistryEntriesToDeviceQueryResultDTO(registryList, unfilteredHits);
        } catch (final IllegalStateException e) {
            throw new InvalidParameterException("Invalid keys in the metadata requirements (whitespace only differences)");
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //=================================================================================================
    // assistant methods
    //-------------------------------------------------------------------------------------------------
    private Device getDeviceByNameAndMacAddress(final String deviceName, final String macAddress) {
        final Optional<Device> optionalDevice = deviceRepository.findByDeviceNameAndMacAddress(deviceName, macAddress);
        return optionalDevice.orElseThrow(() -> new InvalidParameterException(
                "Device entry with name '" + deviceName + "' and MAC address '" + macAddress + "' does not exist"));
    }

    //-------------------------------------------------------------------------------------------------
    private Map<String, String> normalizeMetadata(final Map<String, String> metadata) throws IllegalStateException {
        logger.debug("normalizeMetadata started...");
        if (metadata == null) {
            return Map.of();
        }

        final Map<String, String> map = new HashMap<>();

        metadata.forEach((k, v) ->
                         {
                             if (Objects.nonNull(v)) {
                                 map.put(k.trim(), v.trim());
                             }
                         });

        return map;
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
    private Device validateNonNullDeviceParameters(final String deviceName, final String address, final String macAddress, final String authenticationInfo) {
        logger.debug("validateNonNullDeviceParameters started...");

        if (Utilities.isEmpty(deviceName)) {
            throw new InvalidParameterException("Name is null or empty");
        }

        if (Utilities.isEmpty(macAddress)) {
            throw new InvalidParameterException("MAC address is null or empty");
        } else {
            if (!Utilities.isValidMacAddress(macAddress)) {
                throw new InvalidParameterException("Unrecognized format of MAC Address");
            }
        }

        if (deviceName.contains(".")) {
            throw new InvalidParameterException("Name can't contain dot (.)");
        }

        final String validatedDeviceName = Utilities.lowerCaseTrim(deviceName);
        final String validatedAddress = Utilities.lowerCaseTrim(address);
        final String validatedMacAddress = Utilities.lowerCaseTrim(macAddress);

        checkConstraintsOfDeviceTable(validatedDeviceName, validatedMacAddress);

        return new Device(validatedDeviceName, validatedAddress, validatedMacAddress, authenticationInfo);
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
    private DeviceRegistry getDeviceRegistryEntryById(final long id) {
        logger.debug("getDeviceRegistryEntryById started...");
        try {
            final Optional<DeviceRegistry> deviceRegistry = deviceRegistryRepository.findById(id);
            return deviceRegistry.orElseThrow(() -> new InvalidParameterException("Device Registry with id of '" + id + "' does not exist"));
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private Device findOrCreateDevice(DeviceRequestDTO requestDeviceDto) {
        return findOrCreateDevice(requestDeviceDto.getDeviceName(),
                                  requestDeviceDto.getAddress(),
                                  requestDeviceDto.getMacAddress(),
                                  requestDeviceDto.getAuthenticationInfo());
    }

    //-------------------------------------------------------------------------------------------------
    private Device findOrCreateDevice(String deviceName, String address, String macAddress, String authenticationInfo) {

        final String validateName = Utilities.lowerCaseTrim(deviceName);
        final String validateAddress = Utilities.lowerCaseTrim(address);
        final String validatedMacAddress = Utilities.lowerCaseTrim(macAddress);

        final Optional<Device> optDevice = deviceRepository.findByDeviceNameAndMacAddress(validateName, validatedMacAddress);
        Device device;

        if (optDevice.isPresent()) {
            device = optDevice.get();
            if (!Objects.equals(authenticationInfo, device.getAuthenticationInfo()) ||
                    !Objects.equals(validateAddress, device.getAddress())) { // authentication info or device has changed
                device.setAuthenticationInfo(authenticationInfo);
                device.setAddress(validateAddress);
                device = deviceRepository.saveAndFlush(device);
            }
        } else {
            device = createDevice(validateName, validateAddress, validatedMacAddress, authenticationInfo);
        }
        return device;
    }

    //-------------------------------------------------------------------------------------------------
    private CertificateCreationResponseDTO signCertificate(final String signingRequest) {
        logger.debug("Contact CertificateAuthority ...");
        final CertificateSigningRequestDTO csrDTO = new CertificateSigningRequestDTO(signingRequest);
        final CertificateSigningResponseDTO signingResponse = caDriver.signCertificate(csrDTO);

        logger.debug("Processing response from Certificate Authority ...");
        final CertificateCreationResponseDTO certificateResponseDTO = new CertificateCreationResponseDTO();
        certificateResponseDTO.setCertificate(signingResponse.getCertificateChain().get(0));
        certificateResponseDTO.setCertificateFormat(CoreCommonConstants.CERTIFICATE_FORMAT);
        certificateResponseDTO.setCertificateType(CertificateType.AH_DEVICE);
        return certificateResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    private Device createDevice(final String name, final String address, final String macAddress, final String authenticationInfo) {
        final Device device = validateNonNullDeviceParameters(name, address, macAddress, authenticationInfo);
        return deviceRepository.saveAndFlush(device);
    }

    //-------------------------------------------------------------------------------------------------
    private void checkDeviceRegistryRequest(final DeviceRegistryRequestDTO request) {
        logger.debug("checkDeviceRegistryRequest started...");
        Assert.notNull(request, "Request is null.");

        Assert.notNull(request.getDevice(), "Device is not specified.");
        Assert.isTrue(Utilities.notEmpty(request.getDevice().getDeviceName()), "Device name is not specified.");
        Assert.isTrue(Utilities.notEmpty(request.getDevice().getMacAddress()), "Device MAC address is not specified.");
    }

    //-------------------------------------------------------------------------------------------------
    private void checkConstraintsOfDeviceRegistryTable(final Device deviceDb) {
        logger.debug("checkConstraintOfDeviceRegistryTable started...");

        try {
            final Optional<DeviceRegistry> find = deviceRegistryRepository.findByDevice(deviceDb);
            if (find.isPresent()) {
                throw new InvalidParameterException("Device Registry entry with provider: (" +
                                                            deviceDb.getDeviceName() + ", " +
                                                            deviceDb.getMacAddress() +
                                                            ") already exists.");
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
        final String address = Utilities.firstNotNullIfExists(request.getAddress(), device.getAddress());
        final String macAddress = Utilities.firstNotNullIfExists(request.getDeviceName(), device.getDeviceName());
        final String authenticationInfo = Utilities.firstNotNullIfExists(request.getAuthenticationInfo(), device.getAuthenticationInfo());
        return findOrCreateDevice(name, address, macAddress, authenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private DeviceRegistry createDeviceRegistry(final Device deviceDb, final ZonedDateTime endOfValidity, final String metadataStr, final int version) {
        logger.debug("createDeviceRegistry started...");

        checkConstraintsOfDeviceRegistryTable(deviceDb);

        try {
            return deviceRegistryRepository.saveAndFlush(new DeviceRegistry(deviceDb, endOfValidity, metadataStr, version));
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private boolean checkDeviceRegistryIfUniqueValidationNeeded(final DeviceRegistry drEntry, final Device device) {
        logger.debug("checkDeviceRegistryIfUniqueValidationNeeded started...");

        return drEntry.getDevice().getId() != device.getId();

    }

    //-------------------------------------------------------------------------------------------------
    private DeviceRegistry updateDeviceRegistry(final DeviceRegistry drEntry, final Device device,
                                                final ZonedDateTime endOfValidity, final String metadataStr, final int version) {
        logger.debug("updateDeviceRegistry started...");
        Assert.notNull(drEntry, "DeviceRegistry Entry is not specified.");
        Assert.notNull(device, "Device is not specified.");

        if (checkDeviceRegistryIfUniqueValidationNeeded(drEntry, device)) {
            checkConstraintsOfDeviceRegistryTable(device);
        }

        return setModifiedValuesOfDeviceRegistryEntryFields(drEntry, device, endOfValidity, metadataStr, version);
    }

    //-------------------------------------------------------------------------------------------------
    private ZonedDateTime getZonedDateTime(final DeviceRegistryRequestDTO request) {
        return Utilities.isEmpty(request.getEndOfValidity()) ? null : Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
    }

    //-------------------------------------------------------------------------------------------------
    private void publishRegister(final DeviceRegistryRequestDTO requestDTO) {
        try {
            eventHandlerDriver.publish(
                    new EventPublishRequestDTO(CoreEventHandlerConstants.REGISTER_DEVICE_EVENT,
                                               driverUtilities.getCoreSystemRequestDTO(),
                                               null,
                                               eventHandlerDriver.convert(requestDTO),
                                               Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now())
                    )
            );
        } catch (final Exception e) {
            logger.warn("Unable to publish register event: {}", e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void publishUnregister(final DeviceRegistry deviceRegistry) {
        try {
            final var device = deviceRegistry.getDevice();

            final var deviceRequestDTO = new DeviceRequestDTO(device.getDeviceName(),
                                                              device.getAddress(),
                                                              device.getMacAddress(),
                                                              device.getAuthenticationInfo());

            final var requestDTO = new DeviceRegistryRequestDTO(deviceRequestDTO,
                                                             Utilities.convertZonedDateTimeToUTCString(deviceRegistry.getEndOfValidity()),
                                                             Utilities.text2Map(deviceRegistry.getMetadata()),
                                                             deviceRegistry.getVersion());
            eventHandlerDriver.publish(
                    new EventPublishRequestDTO(CoreEventHandlerConstants.UNREGISTER_DEVICE_EVENT,
                                               driverUtilities.getCoreSystemRequestDTO(),
                                               null,
                                               eventHandlerDriver.convert(requestDTO),
                                               Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now())
                    )
            );
        } catch (final Exception e) {
            logger.warn("Unable to publish unregister event: {}", e.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    private DeviceRegistry setModifiedValuesOfDeviceRegistryEntryFields(final DeviceRegistry drEntry, final Device device,
                                                                        final ZonedDateTime endOfValidity, final String metadataStr, final int version) {

        logger.debug("setModifiedValuesOfDeviceRegistryEntryFields started...");

        try {
            drEntry.setDevice(device);
            drEntry.setEndOfValidity(endOfValidity);
            drEntry.setMetadata(metadataStr);
            drEntry.setVersion(version);

            return deviceRegistryRepository.saveAndFlush(drEntry);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
}

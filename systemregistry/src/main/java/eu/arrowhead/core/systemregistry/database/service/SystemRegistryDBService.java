package eu.arrowhead.core.systemregistry.database.service;

import eu.arrowhead.common.*;
import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.SystemRegistry;
import eu.arrowhead.common.database.repository.DeviceRepository;
import eu.arrowhead.common.database.repository.SystemRegistryRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class SystemRegistryDBService {

    //=================================================================================================
    // members

    private static final String COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE = "Could not delete System, with given parameters";
    private static final String COULD_NOT_DELETE_DEVICE_ERROR_MESSAGE = "Could not delete Device, with given parameters";
    private static final String PORT_RANGE_ERROR_MESSAGE = "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".";

    private static final int MAX_BATCH_SIZE = 200;

    private final Logger logger = LogManager.getLogger(SystemRegistryDBService.class);


    @Autowired
    private SystemRegistryRepository systemRegistryRepository;

    @Autowired
    private SystemRepository systemRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SSLProperties sslProperties;

    //=================================================================================================
    // methods

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
    public System createSystem(final String systemName, final String address, final int port, final String authenticationInfo) {
        logger.debug("createSystem started...");

        final System system = validateNonNullSystemParameters(systemName, address, port, authenticationInfo);

        try {
            return systemRepository.saveAndFlush(system);
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO createSystemResponse(final String systemName, final String address, final int port, final String authenticationInfo) {
        logger.debug("createSystemResponse started...");

        return DTOConverter.convertSystemToSystemResponseDTO(createSystem(systemName, address, port, authenticationInfo));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO updateSystemResponse(final long systemId, final String systemName, final String address, final int port,
                                                  final String authenticationInfo) {
        logger.debug("updateSystemResponse started...");

        return DTOConverter.convertSystemToSystemResponseDTO(updateSystem(systemId, systemName, address, port, authenticationInfo));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public System updateSystem(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo) {
        logger.debug("updateSystem started...");

        final long validatedSystemId = validateId(systemId);
        final int validatedPort = validateSystemPort(port);
        final String validatedSystemName = validateParamString(systemName);
        if (validatedSystemName.contains(".")) {
            throw new InvalidParameterException("System name can't contain dot (.)");
        }
        final String validatedAddress = validateParamString(address);
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
            system.setPort(validatedPort);
            system.setAuthenticationInfo(validatedAuthenticationInfo);

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
    public SystemResponseDTO mergeSystemResponse(final long systemId, final String systemName, final String address, final Integer port,
                                                 final String authenticationInfo) {
        logger.debug("mergeSystemResponse started...");

        return DTOConverter.convertSystemToSystemResponseDTO(mergeSystem(systemId, systemName, address, port, authenticationInfo));
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public System mergeSystem(final long systemId, final String systemName, final String address, final Integer port, final String authenticationInfo) {
        logger.debug("mergeSystem started...");

        final long validatedSystemId = validateId(systemId);
        final Integer validatedPort = validateAllowNullSystemPort(port);
        final String validatedSystemName = validateAllowNullParamString(systemName);
        if (validatedSystemName != null && validatedSystemName.contains(".")) {
            throw new InvalidParameterException("System name can't contain dot (.)");
        }
        final String validatedAddress = validateAllowNullParamString(address);
        final String validatedAuthenticationInfo = authenticationInfo;

        try {
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

            return systemRepository.saveAndFlush(system);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }


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
    public DeviceResponseDTO createDevice(final String name, final String address, final String macAddress, final String authenticationInfo) {
        final Device device = new Device(name, address, macAddress, authenticationInfo);
        return DTOConverter.convertDeviceToDeviceResponseDTO(deviceRepository.save(device));
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public DeviceResponseDTO updateDeviceByIdResponse(final long id, final String name, final String address, final String macAddress, final String authenticationInfo) {

        logger.debug("updateDeviceByIdResponse started...");

        try {

            final long validatedId = validateId(id);
            final Device newDevice = validateNonNullDeviceParameters(name, address, macAddress, authenticationInfo);

            final Optional<Device> optionalDevice = deviceRepository.findById(id);
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
            return DTOConverter.convertSystemRegistryPageToSystemRegistryListResponseDTO(systemRegistryPage);
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
                throw new InvalidParameterException("System Registry entry with id '" + id + "' does not exists");
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
        return null;
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryResponseDTO updateSystemRegistryById(final long id, final SystemRegistryRequestDTO request) {
        return null;
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryResponseDTO mergeSystemRegistryById(final long id, final SystemRegistryRequestDTO request) {
        return null;
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemRegistryListResponseDTO getSystemRegistryEntriesBySystemName(final String systemName, final CoreUtilities.ValidatedPageParams pageParameters, final String sortField) {
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeSystemRegistryByNameAndAddressAndPort(final String systemName, final String address, final int port) {
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemQueryResultDTO queryRegistry(final SystemQueryFormDTO form) {
    }


    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public SystemResponseDTO getSystemByNameAndAddressAndPort(final String systemName, final String address, final int port) {
    }

    //=================================================================================================
    // assistant methods

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
        } else return validatedPort != null && actualPort != validatedPort;
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
    private System validateNonNullSystemParameters(final String systemName, final String address, final int port, final String authenticationInfo) {
        logger.debug("validateNonNullSystemParameters started...");

        validateNonNullParameters(systemName, address, authenticationInfo);

        if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
        }

        final String validatedSystemName = systemName.trim().toLowerCase();
        final String validatedAddress = address.trim().toLowerCase();
        final String validatedAuthenticationInfo = authenticationInfo;

        checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);

        return new System(validatedSystemName, validatedAddress, port, validatedAuthenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private Device validateNonNullDeviceParameters(final String deviceName, final String address, final String macAddress, final String authenticationInfo) {
        logger.debug("validateNonNullDeviceParameters started...");

        validateNonNullParameters(deviceName, address, authenticationInfo);

        if (Utilities.isEmpty(macAddress)) {
            throw new InvalidParameterException("MAC address is null or empty");
        }

        final String validatedDeviceName = deviceName.trim().toLowerCase();
        final String validatedAddress = address.trim().toLowerCase();
        final String validatedMacAddress = macAddress.trim().toUpperCase();
        final String validatedAuthenticationInfo = authenticationInfo;

        checkConstraintsOfDeviceTable(validatedDeviceName, validatedMacAddress);

        return new Device(validatedDeviceName, validatedAddress, validatedMacAddress, validatedAuthenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private void validateNonNullParameters(final String name, final String address, final String authenticationInfo) {
        logger.debug("validateNonNullParameters started...");

        if (Utilities.isEmpty(name)) {
            throw new InvalidParameterException("Name is null or empty");
        }

        if (Utilities.isEmpty(address)) {
            throw new InvalidParameterException("Address is null or empty");
        }

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

        return param.trim().toLowerCase();
    }


    //-------------------------------------------------------------------------------------------------
    private SystemRegistry getSystemRegistryEntryById(final long id) {
        logger.debug("getSystemRegistryEntryById started...");
        try {
            final Optional<SystemRegistry> systemRegistry = systemRegistryRepository.findById(id);
            return systemRegistry.orElseThrow(() -> new InvalidParameterException("System Registry with id of '" + id + "' not exists"));
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private System findOrCreateSystem(final String systemName, final String address, final int port, final String authenticationInfo) {
        final Optional<System> optProvider = systemRepository
                .findBySystemNameAndAddressAndPort(systemName.toLowerCase().trim(), address.toLowerCase().trim(), port);
        System provider;
        if (optProvider.isPresent()) {
            provider = optProvider.get();
            if (!Objects.equals(authenticationInfo, provider.getAuthenticationInfo())) { // authentication info has changed
                provider.setAuthenticationInfo(authenticationInfo);
                provider = systemRepository.saveAndFlush(provider);
            }
        } else {
            provider = createSystem(systemName, address, port, authenticationInfo);
        }
        return provider;
    }
}
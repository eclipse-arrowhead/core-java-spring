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

package eu.arrowhead.core.systemregistry;

import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

public class Validation {

    private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
    private static final String SYSTEM_NAME_NULL_ERROR_MESSAGE = " System name must have value ";
    private static final String SYSTEM_ADDRESS_NULL_ERROR_MESSAGE = " System address must have value ";
    private static final String SYSTEM_PORT_NULL_ERROR_MESSAGE = " System port must have value ";
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";


    private static final String DEVICE_NAME_NULL_ERROR_MESSAGE = " Device name must have value ";
    private static final String DEVICE_ADDRESS_NULL_ERROR_MESSAGE = " Device address must have value ";
    private static final String DEVICE_MAC_ADDRESS_NULL_ERROR_MESSAGE = " Device MAC address must have value ";

    private final Logger logger = LogManager.getLogger();
    private final CommonNamePartVerifier cnVerifier = new CommonNamePartVerifier(); 

    public Validation() { super(); }

    //-------------------------------------------------------------------------------------------------
    void checkSystemNameRequirements(final String systemNameRequirements, final String origin) {

        if (Utilities.isEmpty(systemNameRequirements)) {
            throw new BadPayloadException("System definition requirement is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

    }

    //-------------------------------------------------------------------------------------------------
    void checkId(final long systemId, final String origin) {
        if (systemId < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkSystemMergeRequest(final SystemRequestDTO request, final long systemId, final String origin) {
        logger.debug("checkSystemPatchRequest started...");

        if (systemId <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        boolean needChange = false;
        if (!Utilities.isEmpty(request.getAddress())) {
            needChange = true;
        }

        if (!Utilities.isEmpty(request.getSystemName())) {
            needChange = true;
            
            if (!cnVerifier.isValid(request.getSystemName())) {
            	throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
            }
            
            for (final CoreSystem coreSystem : CoreSystem.values()) {
                if (coreSystem.name().equalsIgnoreCase(request.getSystemName().trim())) {
                    throw new BadPayloadException("System name '" + request.getSystemName() + "' is a reserved arrowhead core system name.",
                                                  HttpStatus.SC_BAD_REQUEST, origin);
                }
            }
        }

        if (request.getPort() != null) {
            final int validatedPort = request.getPort();
            if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
                throw new BadPayloadException(
                        "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
                        HttpStatus.SC_BAD_REQUEST,
                        origin);
            }

            needChange = true;
        }

        if (request.getAuthenticationInfo() != null) {
            needChange = true;
        }
        
        if (request.getMetadata() != null) {
        	needChange = true;
        }

        if (!needChange) {
            throw new BadPayloadException("Patch request is empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkOnboardingRequest(final SystemRegistryOnboardingWithNameRequestDTO request, final String origin) {
        logger.debug("checkOnboardingRequest started...");
        if (request == null) {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getCertificateCreationRequest() == null) {
            throw new BadPayloadException("Certificate creation request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkSystemRegistryRequest(request, origin, true);
    }

    //-------------------------------------------------------------------------------------------------
    void checkOnboardingRequest(final SystemRegistryOnboardingWithCsrRequestDTO request, final String origin) {
        logger.debug("checkOnboardingRequest started...");
        if (request == null) {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getCertificateSigningRequest() == null) {
            throw new BadPayloadException("Certificate signing request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkSystemRegistryRequest(request, origin, true);
    }

    //-------------------------------------------------------------------------------------------------
    void checkSystemPutRequest(final SystemRequestDTO request, final long systemId, final String origin) {
        logger.debug("checkSystemPutRequest started...");

        if (systemId <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkSystemRequest(request, origin, true);
    }

    //-------------------------------------------------------------------------------------------------
    void checkSystemRequest(final SystemRequestDTO request, final String origin, final boolean checkReservedCoreSystemNames) {
        logger.debug("checkSystemRequest started...");

        if (request == null) {
            throw new BadPayloadException("System is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getSystemName())) {
            throw new BadPayloadException(SYSTEM_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        
        if (!cnVerifier.isValid(request.getSystemName())) {
        	throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (checkReservedCoreSystemNames) {
            for (final CoreSystem coreSystem : CoreSystem.values()) {
                if (coreSystem.name().equalsIgnoreCase(request.getSystemName().trim())) {
                    throw new BadPayloadException("System name '" + request.getSystemName() + "' is a reserved arrowhead core system name.",
                                                  HttpStatus.SC_BAD_REQUEST, origin);
                }
            }
        }

        if (Utilities.isEmpty(request.getAddress())) {
            throw new BadPayloadException(SYSTEM_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getPort() == null) {
            throw new BadPayloadException(SYSTEM_PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        final int validatedPort = request.getPort();
        if (validatedPort <= CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException(
                    "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
                    HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkDeviceRequest(final DeviceRequestDTO request, final String origin) {
        logger.debug("checkDeviceRequest started...");

        if (request == null) {
            throw new BadPayloadException("Device is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getDeviceName())) {
            throw new BadPayloadException(DEVICE_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getAddress())) {
            throw new BadPayloadException(DEVICE_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Utilities.isEmpty(request.getMacAddress())) {
            throw new BadPayloadException(DEVICE_MAC_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (!Utilities.isValidMacAddress(request.getMacAddress())) {
            throw new BadPayloadException("Unrecognized format of MAC Address", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkSystemRegistryRequest(final SystemRegistryRequestDTO request, final String origin, final boolean checkReservedCoreSystemNames) {
        logger.debug("checkSystemRegistryRequest started...");

        checkSystemRequest(request.getSystem(), origin, checkReservedCoreSystemNames);
        checkDeviceRequest(request.getProvider(), origin);

        if (!Utilities.isEmpty(request.getEndOfValidity())) {
            try {
                Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
            } catch (final DateTimeParseException ex) {
                throw new BadPayloadException(
                        "End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.",
                        HttpStatus.SC_BAD_REQUEST, origin);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkSystemName(final String systemName, final String origin) {
        if (Utilities.isEmpty(systemName)) {
            throw new BadPayloadException("Name of the system is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }
        
        if (!cnVerifier.isValid(systemName)) {
        	throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

    }


    //-------------------------------------------------------------------------------------------------
    void checkUnregisterSystemParameters(final String systemName, final String address, final int port, final String origin) {
        // parameters can't be null, but can be empty
        logger.debug("checkUnregisterSystemParameters started...");

        checkSystemName(systemName, origin);

        if (Utilities.isEmpty(address)) {
            throw new BadPayloadException("Address of the system is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException(
                    "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
                    HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkSystemRegistryUpdateRequest(final long id, final SystemRegistryRequestDTO request, final String origin) {
        logger.debug("checkSystemRegistryUpdateRequest started...");

        if (id <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkSystemRegistryRequest(request, origin, true);

    }

    //-------------------------------------------------------------------------------------------------
    void validateDevice(final DeviceRequestDTO deviceRequestDto, final String origin) {
        if (Utilities.isEmpty(deviceRequestDto.getDeviceName())) {
            throw new BadPayloadException("Device name is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(deviceRequestDto.getAddress())) {
            throw new BadPayloadException("Device address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(deviceRequestDto.getMacAddress())) {
            throw new BadPayloadException("Device MAC address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("squid:S3776")
    void checkSystemRegistryMergeRequest(final long id, final SystemRegistryRequestDTO request, final String origin) {
        logger.debug("checkSystemRegistryMergeRequest started...");

        if (id <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        boolean needChange = false;

        if (Objects.nonNull(request.getSystem())) {
            final SystemRequestDTO system = request.getSystem();

            if (!Utilities.isEmpty(system.getSystemName())) {
                needChange = true;
                
                if (!cnVerifier.isValid(request.getSystem().getSystemName())) {
                	throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
                }
            } else if (!Utilities.isEmpty(system.getAddress())) {
                needChange = true;
            } else if (Objects.nonNull(system.getPort())) {
                final int validatedPort = system.getPort();
                if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
                    throw new BadPayloadException(
                            "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
                            HttpStatus.SC_BAD_REQUEST,
                            origin);
                }

                needChange = true;
            } else if (Objects.nonNull(system.getAuthenticationInfo())) {
                needChange = true;
            } else if (Objects.nonNull(system.getMetadata())) {
            	needChange = true;
            }
        }

        if (request.getEndOfValidity() != null) {
            needChange = true;
        }

        if (request.getMetadata() != null) {
            needChange = true;
        }

        if (request.getVersion() != null && request.getVersion() > 0) {
            needChange = true;
        }

        if (!needChange) {
            throw new BadPayloadException("Patch request is empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}
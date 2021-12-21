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

package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeParseException;
import java.util.Objects;

public class Validation {
	
	//=================================================================================================
	// members

    private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0.";
    private static final String DEVICE_NAME_NULL_ERROR_MESSAGE = "Device name must have value";
    private static final String DEVICE_ADDRESS_NULL_ERROR_MESSAGE = "Device address must have value";
    private static final String DEVICE_MAC_ADDRESS_NULL_ERROR_MESSAGE = "Device MAC address must have value";

    private final Logger logger = LogManager.getLogger();
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	Validation() { super(); }

    //-------------------------------------------------------------------------------------------------
    void checkId(final Long id, final String origin) {
        logger.debug("checkId started...");

        if (id == null) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICEREGISTRY_URI +
                    CoreCommonConstants.OP_DEVICEREGISTRY_QUERY_BY_DEVICE_ID_URI);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkUnregisterDeviceParameters(final String deviceName, final String macAddress) {
        // parameters can't be null, but can be empty
        logger.debug("checkUnregisterDeviceParameters started...");

        final String origin = CommonConstants.DEVICEREGISTRY_URI + CommonConstants.OP_DEVICEREGISTRY_UNREGISTER_URI;

        if (Utilities.isEmpty(deviceName)) {
            throw new BadPayloadException("Name of the device is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(macAddress)) {
            throw new BadPayloadException("MAC Address of the device is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (!Utilities.isValidMacAddress(macAddress)) {
            throw new BadPayloadException("Unrecognized format of MAC Address", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkDeviceMergeRequest(final DeviceRequestDTO request, final Long deviceId, final String origin) {
        logger.debug("checkDeviceMergeRequest started...");

        checkId(deviceId, origin);

        boolean needChange = false;
        if (Utilities.notEmpty(request.getAddress())) {
            needChange = true;
        }

        if (Utilities.notEmpty(request.getDeviceName())) {
            needChange = true;
        }

        if (Utilities.notEmpty(request.getMacAddress())) {
            needChange = true;
        }

        if (Utilities.notEmpty(request.getAuthenticationInfo())) {
            needChange = true;
        }

        if (!needChange) {
            throw new BadPayloadException("Patch request is empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    void checkDevicePutRequest(final DeviceRequestDTO request, final Long deviceId, final String origin) {
        logger.debug("checkDevicePutRequest started...");
        checkId(deviceId, origin);
        checkDeviceRequest(request, origin);
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
    }

    //-------------------------------------------------------------------------------------------------
    void checkOnboardingRequest(final DeviceRegistryOnboardingWithNameRequestDTO request, final String origin) {
        logger.debug("checkOnboardingRequest started...");
        if (request == null) {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getCertificateCreationRequest() == null) {
            throw new BadPayloadException("Certificate creation request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkDeviceRegistryRequest(request, origin);
    }

    //-------------------------------------------------------------------------------------------------
    void checkOnboardingRequest(final DeviceRegistryOnboardingWithCsrRequestDTO request, final String origin) {
        logger.debug("checkOnboardingRequest started...");
        if (request == null) {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getCertificateSigningRequest() == null) {
            throw new BadPayloadException("Certificate signing request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkDeviceRegistryRequest(request, origin);
    }

    //-------------------------------------------------------------------------------------------------
    void checkDeviceRegistryRequest(final DeviceRegistryRequestDTO request, final String origin) {
        logger.debug("checkDeviceRegistryRequest started...");

        checkDeviceRequest(request.getDevice(), origin);

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
    void checkDeviceRegistryUpdateRequest(final Long id, final DeviceRegistryRequestDTO request, final String origin) {
        logger.debug("checkDeviceRegistryUpdateRequest started...");

        checkId(id, origin);
        checkDeviceRegistryRequest(request, origin);
    }

    //-------------------------------------------------------------------------------------------------
    void checkDeviceRegistryMergeRequest(final Long id, final DeviceRegistryRequestDTO request, final String origin) {
        logger.debug("checkDeviceRegistryMergeRequest started...");

        checkId(id, origin);

        boolean needChange = false;

        if (Objects.nonNull(request.getDevice())) {
            final DeviceRequestDTO device = request.getDevice();

            if (Utilities.notEmpty(device.getDeviceName())) {
                needChange = true;
            } else if (Utilities.notEmpty(device.getAddress())) {
                needChange = true;
            } else if (Utilities.notEmpty(device.getMacAddress())) {
                needChange = true;
            } else if (Utilities.notEmpty(device.getAuthenticationInfo())) {
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
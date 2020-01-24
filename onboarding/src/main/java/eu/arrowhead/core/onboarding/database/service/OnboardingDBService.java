package eu.arrowhead.core.onboarding.database.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OnboardingDBService
{
    private static final String COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE = "Could not delete System, with given parameters";
    private static final String PORT_RANGE_ERROR_MESSAGE = "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".";
    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(OnboardingDBService.class);
    private final SystemRepository systemRepository;
    private final SSLProperties sslProperties;

    @Autowired
    public OnboardingDBService(final SystemRepository systemRepository, final SSLProperties sslProperties)
    {
        this.systemRepository = systemRepository;
        this.sslProperties = sslProperties;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithNameResponseDTO onboarding(final OnboardingWithNameRequestDTO onboardingRequest)
    {
        logger.debug("onboarding started...");
        // TODO contact certificate authority
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithCsrResponseDTO onboarding(final OnboardingWithCsrRequestDTO onboardingRequest)
    {
        logger.debug("onboarding started...");
        // TODO contact certificate authority
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public List<System> getSystemByName(final String systemName)
    {
        logger.debug("getSystemByName started...");

        final String name = validateSystemParamString(systemName);
        try
        {
            return systemRepository.findBySystemName(name);
        }
        catch (final Exception ex)
        {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeSystemById(final long id)
    {
        logger.debug("removeSystemById started...");

        try
        {
            if (!systemRepository.existsById(id))
            {
                throw new InvalidParameterException(COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE);
            }

            systemRepository.deleteById(id);
            systemRepository.flush();
        }
        catch (final InvalidParameterException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public System createSystem(final String systemName, final String address, final int port, final String authenticationInfo)
    {
        logger.debug("createSystem started...");

        final System system = validateNonNullSystemParameters(systemName, address, port, authenticationInfo);

        try
        {
            return systemRepository.saveAndFlush(system);
        }
        catch (final Exception ex)
        {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private String validateSystemParamString(final String param)
    {
        logger.debug("validateSystemParamString started...");

        if (Utilities.isEmpty(param))
        {
            throw new InvalidParameterException("parameter null or empty");
        }

        return param.trim().toLowerCase();
    }

    //-------------------------------------------------------------------------------------------------
    private System validateNonNullSystemParameters(final String systemName, final String address, final int port, final String authenticationInfo)
    {
        logger.debug("validateNonNullSystemParameters started...");

        if (Utilities.isEmpty(systemName))
        {
            throw new InvalidParameterException("System name is null or empty");
        }

        if (Utilities.isEmpty(address))
        {
            throw new InvalidParameterException("System address is null or empty");
        }

        if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX)
        {
            throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
        }

        final String validatedSystemName = systemName.trim().toLowerCase();
        if (validatedSystemName.contains("."))
        {
            throw new InvalidParameterException("System name can't contain dot (.)");
        }
        final String validatedAddress = address.trim().toLowerCase();

        checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);

        return new System(validatedSystemName, validatedAddress, port, authenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private void checkConstraintsOfSystemTable(final String validatedSystemName, final String validatedAddress, final int validatedPort)
    {
        logger.debug("checkConstraintsOfSystemTable started...");

        try
        {
            final Optional<System> find = systemRepository
                    .findBySystemNameAndAddressAndPort(validatedSystemName.toLowerCase().trim(), validatedAddress.toLowerCase().trim(), validatedPort);
            if (find.isPresent())
            {
                throw new InvalidParameterException(
                        "System with name: " + validatedSystemName + ", address: " + validatedAddress + ", port: " + validatedPort + " already exists.");
            }
        }
        catch (final InvalidParameterException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

}

package eu.arrowhead.core.onboarding.database.service;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OnboardingDBService {
    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(OnboardingDBService.class);
    private final SSLProperties sslProperties;

    @Autowired
    public OnboardingDBService(final SSLProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithNameResponseDTO onboarding(final OnboardingWithNameRequestDTO onboardingRequest) {
        logger.debug("onboarding started...");
        // TODO contact certificate authority
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithCsrResponseDTO onboarding(final OnboardingWithCsrRequestDTO onboardingRequest) {
        logger.debug("onboarding started...");
        // TODO contact certificate authority
        return null;
    }

    //=================================================================================================
    // assistant methods

}

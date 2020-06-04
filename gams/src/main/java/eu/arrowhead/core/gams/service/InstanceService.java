package eu.arrowhead.core.gams.service;

import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.drivers.CertificateAuthorityDriver;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.drivers.OrchestrationDriver;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import eu.arrowhead.core.gams.rest.dto.InstanceDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class InstanceService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(InstanceService.class);
    private final OrchestrationDriver orchestrationDriver;
    private final CertificateAuthorityDriver caDriver;
    private final SecurityUtilities securityUtilities;

    @Autowired
    public InstanceService(final OrchestrationDriver orchestrationDriver,
                           final CertificateAuthorityDriver caDriver,
                           final SecurityUtilities securityUtilities) {
        this.orchestrationDriver = orchestrationDriver;
        this.caDriver = caDriver;
        this.securityUtilities = securityUtilities;
    }


    //-------------------------------------------------------------------------------------------------
    public InstanceDto create(final CreateInstanceRequest instanceRequest)
            throws DriverUtilities.DriverException {
        logger.debug("create started...");
        Assert.notNull(instanceRequest, "CreateInstanceRequest must not be null");
        Assert.hasText(instanceRequest.getName(), "CreateInstanceRequest must not be empty");

        return null;
    }
}

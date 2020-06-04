package eu.arrowhead.core.gams.service;

import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.drivers.CertificateAuthorityDriver;
import eu.arrowhead.common.drivers.OrchestrationDriver;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.InstanceDto;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(SensorService.class);
    private final OrchestrationDriver orchestrationDriver;
    private final CertificateAuthorityDriver caDriver;
    private final SecurityUtilities securityUtilities;

    @Autowired
    public SensorService(final OrchestrationDriver orchestrationDriver,
                         final CertificateAuthorityDriver caDriver,
                         final SecurityUtilities securityUtilities) {
        this.orchestrationDriver = orchestrationDriver;
        this.caDriver = caDriver;
        this.securityUtilities = securityUtilities;
    }

    public InstanceDto create(final CreateSensorRequest createSensorRequest) {
        return null;
    }

    public InstanceDto publish(final PublishSensorDataRequest publishSensorDataRequest) {
        return null;
    }

    //=================================================================================================
    // methods
}

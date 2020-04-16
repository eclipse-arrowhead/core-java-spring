package eu.arrowhead.common.drivers;

import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

@Service
public class OrchestrationDriver extends AbstractDriver {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(OrchestrationDriver.class);

    @Autowired
    public OrchestrationDriver(final DriverUtilities driverUtilities, final HttpService httpService) {
        super(driverUtilities, httpService);
    }

    public UriComponents findCoreSystemService(final CoreSystemService service) throws DriverUtilities.DriverException {
        logger.traceEntry("findCoreSystemService: {}", service);
        return logger.traceExit(driverUtilities.findUriByOrchestrator(service));
    }

    public OrchestrationResponseDTO orchestrate(final OrchestrationFormRequestDTO request) throws DriverUtilities.DriverException {
        logger.traceEntry("orchestrate: {}", request);
        final UriComponents uri = driverUtilities.getOrchestrationQueryUri();
        final ResponseEntity<OrchestrationResponseDTO> httpResponse = httpService
                .sendRequest(uri, HttpMethod.POST, OrchestrationResponseDTO.class, request);
        final OrchestrationResponseDTO result = httpResponse.getBody();
        return logger.traceExit(result);
    }
}

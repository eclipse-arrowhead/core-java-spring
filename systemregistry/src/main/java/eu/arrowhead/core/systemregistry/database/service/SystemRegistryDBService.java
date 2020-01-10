package eu.arrowhead.core.systemregistry.database.service;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.database.repository.*;
import eu.arrowhead.common.intf.ServiceInterfaceNameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemRegistryDBService
{

    //=================================================================================================
    // members

    private final Logger logger = LogManager.getLogger(SystemRegistryDBService.class);


    @Autowired
    private SystemRegistryRepository serviceRegistryRepository;

    @Autowired
    private SystemRepository systemRepository;

    @Autowired
    private SSLProperties sslProperties;

}

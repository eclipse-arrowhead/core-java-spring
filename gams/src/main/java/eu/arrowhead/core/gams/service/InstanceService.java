package eu.arrowhead.core.gams.service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.gams.database.entities.GamsInstance;
import eu.arrowhead.core.gams.database.repositories.GamsInstanceRepository;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;

@Service
public class InstanceService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(InstanceService.class);
    private final GamsInstanceRepository instanceRepository;

    @Autowired
    public InstanceService(final GamsInstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    //-------------------------------------------------------------------------------------------------
    public GamsInstance create(final CreateInstanceRequest instanceRequest)
            throws DriverUtilities.DriverException {
        logger.debug("create({})", instanceRequest);
        Assert.notNull(instanceRequest, "CreateInstanceRequest must not be null");
        Assert.hasText(instanceRequest.getName(), "CreateInstanceRequest must not be empty");

        GamsInstance instance = new GamsInstance(instanceRequest.getName(), UUID.randomUUID());
        return instanceRepository.saveAndFlush(instance);
    }

    public GamsInstance findByUid(final String uid) {
        logger.debug("findByUid({})", uid);
        Assert.notNull(uid, "Instance uid must not be null");

        final Optional<GamsInstance> instanceByUid = instanceRepository.findByUid(UUID.fromString(uid));
        return instanceByUid.orElseThrow(() -> new DataNotFoundException("Unable to find gams instance with given uid"));
    }
}

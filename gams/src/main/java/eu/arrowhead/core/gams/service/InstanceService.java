package eu.arrowhead.core.gams.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.repository.GamsInstanceRepository;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
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
    private final GamsInstanceRepository instanceRepository;
    private final SensorService sensorService;
    private final DataValidation validation;

    @Autowired
    public InstanceService(final GamsInstanceRepository instanceRepository, final SensorService sensorService) {
        this.instanceRepository = instanceRepository;
        this.sensorService = sensorService;
        validation = new DataValidation();
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional
    public GamsInstance create(final CreateInstanceRequest instanceRequest)
            throws DriverUtilities.DriverException {
        logger.debug("create({})", instanceRequest);
        validation.verify(instanceRequest);

        GamsInstance instance = new GamsInstance(instanceRequest.getName(), UUID.randomUUID(), instanceRequest.getDelayInSeconds());
        instance = instanceRepository.saveAndFlush(instance);

        sensorService.createEventSensor(instance);

        return instance;
    }

    // management
    public List<GamsInstance> getAll() {
        return instanceRepository.findAll();
    }

    public GamsInstance findByUid(final String uid) {
        logger.debug("findByUid({})", uid);
        Assert.notNull(uid, "Instance uid must not be null");

        try {
            final Optional<GamsInstance> instanceByUid = instanceRepository.findByUid(UUID.fromString(uid));
            return instanceByUid.orElseThrow(() -> createException(uid));
        } catch (IllegalArgumentException e) {
            throw createException(uid);
        }
    }

    public void delete(final GamsInstance gamsInstance) {
        logger.debug("delete({})", gamsInstance);
        Assert.notNull(gamsInstance, "Instance must not be null");

        instanceRepository.delete(gamsInstance);
    }

    private DataNotFoundException createException(final String uid) {
        return new DataNotFoundException("Unable to find gams instance with uid " + uid);
    }
}

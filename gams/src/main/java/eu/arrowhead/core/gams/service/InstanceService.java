package eu.arrowhead.core.gams.service;

import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.repository.GamsInstanceRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class InstanceService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(InstanceService.class);
    private final GamsInstanceRepository instanceRepository;
    private final DataValidation validation;

    @Autowired
    public InstanceService(final GamsInstanceRepository instanceRepository, final SensorService sensorService) {
        this.instanceRepository = instanceRepository;
        validation = new DataValidation();
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional
    public GamsInstance create(final CreateInstanceRequest instanceRequest) {
        logger.debug("create({})", instanceRequest);
        validation.verify(instanceRequest);

        try {
            GamsInstance instance = new GamsInstance(instanceRequest.getName(), UUID.randomUUID(), instanceRequest.getDelayInSeconds());
            instance = instanceRepository.saveAndFlush(instance);

            return instance;
        } catch (final Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    // management
    public Page<GamsInstance> getAll(final Pageable pageable) {
        try {
            return instanceRepository.findAll(pageable);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    public GamsInstance findByUid(final String uid) {
        logger.debug("findByUid({})", uid);
        Assert.notNull(uid, "Instance uid must not be null");

        try {
            final Optional<GamsInstance> instanceByUid = instanceRepository.findByUid(UUID.fromString(uid));
            return instanceByUid.orElseThrow(() -> createNotFoundException(uid));
        } catch (IllegalArgumentException e) {
            throw createNotFoundException(uid);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    public GamsInstance findByName(final String name) {
        logger.debug("findByName({})", name);
        Assert.notNull(name, "Name must not be null");

        try {
            final Optional<GamsInstance> instanceByName = instanceRepository.findByName(name);
            return instanceByName.orElseThrow(() -> createNotFoundException(name));
        } catch (IllegalArgumentException e) {
            throw createNotFoundException(name);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    @Transactional
    public void delete(final GamsInstance gamsInstance) {
        logger.debug("delete({})", gamsInstance);
        Assert.notNull(gamsInstance, "Instance must not be null");

        try {
            instanceRepository.delete(gamsInstance);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    private DataNotFoundException createNotFoundException(final String uid) {
        return new DataNotFoundException("Unable to find gams instance with uid " + uid);
    }
}

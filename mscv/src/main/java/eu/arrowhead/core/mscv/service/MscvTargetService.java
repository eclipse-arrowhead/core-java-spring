package eu.arrowhead.core.mscv.service;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.Optional;

import static eu.arrowhead.core.mscv.MscvUtilities.ADDRESS_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.EXAMPLE_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.ID_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.NAME_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.OS_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.PAGE_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.PORT_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.TARGET_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Service
public class MscvTargetService {

    private final Logger logger = LogManager.getLogger();
    private final SshTargetRepository targetRepo;

    @Autowired
    public MscvTargetService(final SshTargetRepository targetRepo) {
        super();
        this.targetRepo = targetRepo;
    }

    public void checkSupported(final Class<?> cls) {
        logger.debug("checkSupported({}) started", cls);
        Assert.notNull(cls, "Argument must not be null");
        if (isSupported(cls)) {
            throw new IllegalArgumentException(cls + " is not supported");
        }
    }

    public boolean isSupported(final Class<?> cls) {
        logger.debug("isSupported({}) started", cls);
        Assert.notNull(cls, "Argument must not be null");
        return cls.isAssignableFrom(SshTargetDto.class) || cls.isAssignableFrom(SshTarget.class);
    }

    public SshTarget findOrCreate(final Target target) {
        logger.debug("findOrCreateTarget({}) started", target);
        Assert.notNull(target, "Argument must not be null");
        checkSupported(target.getClass());
        final var sshTarget = (SshTarget) target;
        return findOrCreate(sshTarget.getName(), sshTarget.getOs(), sshTarget.getAddress(), sshTarget.getPort());
    }

    @Transactional
    public SshTarget findOrCreate(final String name, final OS os, final String address, final Integer port) {
        logger.debug("findOrCreateTarget({},{},{},{}) started", name, os, address, port);
        Assert.hasText(name, NAME_NOT_NULL);
        Assert.notNull(os, OS_NOT_NULL);
        Assert.hasText(address, ADDRESS_NOT_NULL);
        Assert.notNull(port, PORT_NOT_NULL);

        final SshTarget returnValue;
        final Optional<SshTarget> existingTarget = find(address, port);

        if (existingTarget.isEmpty()) {
            final var newTarget = new SshTarget(name, os, address, port);
            returnValue = targetRepo.saveAndFlush(newTarget);
        } else {
            returnValue = existingTarget.get();
        }
        return returnValue;
    }

    public Optional<SshTarget> find(final String address, final Integer port) {
        logger.debug("findTarget({},{}) started", address, port);
        Assert.hasText(address, ADDRESS_NOT_NULL);
        Assert.notNull(port, PORT_NOT_NULL);
        return targetRepo.findByAddressAndPort(address, port);
    }

    public Page<SshTarget> getPageByExample(final Example<SshTarget> example, final Pageable pageable) {
        logger.debug("getTargets({},{}) started", example, pageable);
        Assert.notNull(example, EXAMPLE_NOT_NULL);
        Assert.notNull(pageable, PAGE_NOT_NULL);

        return targetRepo.findAll(example, pageable);
    }

    @Transactional
    public SshTarget replace(final SshTarget oldTarget, final SshTargetDto newValues) {
        logger.debug("replaceTarget({},{}) started", oldTarget, newValues);
        Assert.notNull(oldTarget, "old " + TARGET_NOT_NULL);
        Assert.notNull(newValues, "new " + TARGET_NOT_NULL);

        oldTarget.setName(newValues.getName());
        oldTarget.setOs(newValues.getOs());
        oldTarget.setAddress(newValues.getAddress());
        oldTarget.setPort(newValues.getPort());
        return targetRepo.saveAndFlush(oldTarget);
    }

    @Transactional
    public void delete(final String address, final Integer port) {
        logger.debug("delete({},{}) started", address, port);
        final Optional<SshTarget> target = find(address, port);
        target.ifPresent(targetRepo::delete);
        targetRepo.flush();
    }

    protected SshTarget getTargetById(final Long id) {
        logger.debug("getTargetById({}) started", id);
        Assert.notNull(id, ID_NOT_NULL);
        final Optional<SshTarget> optional = targetRepo.findById(id);
        return optional.orElseThrow(notFoundException("SSH target"));
    }
}

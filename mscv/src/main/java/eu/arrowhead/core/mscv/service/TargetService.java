package eu.arrowhead.core.mscv.service;

import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.handlers.ExecutionHandler;
import eu.arrowhead.core.mscv.handlers.ExecutionHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;
import static eu.arrowhead.core.mscv.Validation.ADDRESS_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.EXAMPLE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.ID_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.NAME_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.OS_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PAGE_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.PORT_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.TARGET_NULL_ERROR_MESSAGE;
import static eu.arrowhead.core.mscv.Validation.USERNAME_NULL_ERROR_MESSAGE;

@Service
public class TargetService {

    private final Logger logger = LogManager.getLogger();
    private final SshTargetRepository targetRepo;
    private final ExecutionHandlerFactory executionHandlerFactory;
    private final MscvDefaults defaults;
    private final Validation validation;

    @Autowired
    public TargetService(final SshTargetRepository targetRepo,
                         final ExecutionHandlerFactory executionHandlerFactory,
                         final MscvDefaults defaults) {
        super();
        this.targetRepo = targetRepo;
        this.executionHandlerFactory = executionHandlerFactory;
        this.defaults = defaults;

        validation = new Validation();
    }

    public void checkSupported(final Class<?> cls) {
        logger.debug("checkSupported({}) started", cls);
        Assert.notNull(cls, "Argument must not be null");
        if (!isSupported(cls)) {
            throw new IllegalArgumentException(cls + " is not supported");
        }
    }

    public boolean isSupported(final Class<?> cls) {
        logger.debug("isSupported({}) started", cls);
        Assert.notNull(cls, "Argument must not be null");
        return SshTargetDto.class.isAssignableFrom(cls) || SshTarget.class.isAssignableFrom(cls);
    }

    @Transactional
    public SshTarget findOrCreate(final Target target) {
        logger.debug("findOrCreate({}) started", target);
        Assert.notNull(target, "Argument must not be null");
        checkSupported(target.getClass());
        return findOrCreate((SshTarget) target);
    }

    @Transactional
    public SshTarget findOrCreate(final SshTarget sshTarget) {
        logger.debug("findOrCreate({}) started", sshTarget);
        Assert.notNull(sshTarget, "Argument must not be null");
        return findOrCreate(sshTarget.getName(), sshTarget.getOs(), sshTarget.getAddress(), sshTarget.getPort());
    }

    @Transactional
    public SshTarget create(final SshTarget sshTarget) {
        logger.debug("create({}) started", sshTarget);
        Assert.notNull(sshTarget, "Argument must not be null");
        return targetRepo.saveAndFlush(sshTarget);
    }

    @Transactional
    public SshTarget findOrCreate(final String name, final OS os, final String address, final Integer port) {
        logger.debug("findOrCreate({},{},{},{}) started", name, os, address, port);
        Assert.hasText(name, NAME_NULL_ERROR_MESSAGE);
        Assert.notNull(os, OS_NULL_ERROR_MESSAGE);
        validation.verifyAddress(address, "MSCV");
        validation.verifyPort(port, "MSCV");

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

    @Transactional(readOnly = true)
    public Optional<SshTarget> find(final String address, final Integer port) {
        logger.debug("find({},{}) started", address, port);
        Assert.hasText(address, ADDRESS_NULL_ERROR_MESSAGE);
        Assert.notNull(port, PORT_NULL_ERROR_MESSAGE);
        return targetRepo.findByAddressAndPort(address, port);
    }

    @Transactional(readOnly = true)
    public Optional<Target> find(final String name, final OS os) {
        logger.debug("find({},{}) started", name, os);
        Assert.hasText(name, NAME_NULL_ERROR_MESSAGE);
        Assert.notNull(os, OS_NULL_ERROR_MESSAGE);
        return targetRepo.findByNameAndOs(name, os);
    }

    @Transactional(readOnly = true)
    public Page<SshTarget> pageByExample(final Example<SshTarget> example, final Pageable pageable) {
        logger.debug("pageByExample({},{}) started", example, pageable);
        Assert.notNull(example, EXAMPLE_NULL_ERROR_MESSAGE);
        Assert.notNull(pageable, PAGE_NULL_ERROR_MESSAGE);
        return targetRepo.findAll(example, pageable);
    }

    @Transactional
    public SshTarget replace(final SshTarget oldTarget, final SshTarget newValues) {
        logger.debug("replace({},{}) started", oldTarget, newValues);
        Assert.notNull(oldTarget, "old " + TARGET_NULL_ERROR_MESSAGE);
        Assert.notNull(newValues, "new " + TARGET_NULL_ERROR_MESSAGE);

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

    public void login(final Target target, final String username, final String password) throws MscvException, ArrowheadException {
        logger.debug("login({},{},{}) started", target, username, "(password)");
        Assert.notNull(target, TARGET_NULL_ERROR_MESSAGE);
        Assert.notNull(username, USERNAME_NULL_ERROR_MESSAGE);
        // password may or may not be empty/null
        checkSupported(target.getClass());
        final Optional<ExecutionHandler> optionalHandler = executionHandlerFactory.find(target);
        final ExecutionHandler handler = optionalHandler.orElseThrow(() -> new InvalidParameterException("The type of target is not supported"));

        try {
            handler.login(target, username, password);
        } catch (final Exception e) {
            throw new MscvException(e.getMessage(), e);
        }
    }

    public boolean verifyLogin(final Target target) {
        logger.debug("verifyLogin({}) started", target);
        Assert.notNull(target, TARGET_NULL_ERROR_MESSAGE);

        checkSupported(target.getClass());
        final Optional<ExecutionHandler> optionalHandler = executionHandlerFactory.find(target);
        final ExecutionHandler handler = optionalHandler.orElseThrow(() -> new InvalidParameterException("The type of target is not supported"));
        return handler.verifyPasswordlessLogin(target);
    }

    public boolean exists(final SshTarget sshTarget) {
        return targetRepo.exists(Example.of(sshTarget, ExampleMatcher.matchingAll()));
    }

    @Transactional(readOnly = true)
    protected SshTarget getTargetById(final Long id) {
        logger.debug("getTargetById({}) started", id);
        Assert.notNull(id, ID_NULL_ERROR_MESSAGE);
        final Optional<SshTarget> optional = targetRepo.findById(id);
        return optional.orElseThrow(notFoundException("SSH target"));
    }
}

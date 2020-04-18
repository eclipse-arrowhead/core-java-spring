package eu.arrowhead.core.mscv.service;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import eu.arrowhead.common.database.view.mscv.SshTargetView;
import eu.arrowhead.common.database.view.mscv.TargetView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.Optional;

import static eu.arrowhead.core.mscv.MscvUtilities.ADDRESS_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.ID_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.NAME_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.OS_NOT_NULL;
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

    @Transactional(rollbackOn = Exception.class)
    public SshTargetView getTargetViewById(final Long id) {
        logger.debug("getTargetViewById({}) started", id);
        Assert.notNull(id, ID_NOT_NULL);
        final Optional<SshTargetView> optional = targetRepo.findViewById(id);
        return optional.orElseThrow(notFoundException("SSH target"));
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
        return cls.isAssignableFrom(SshTargetView.class) || cls.isAssignableFrom(SshTarget.class);
    }

    @Transactional
    protected Target findOrCreateTarget(final TargetView targetView) {
        logger.debug("findOrCreateTarget({}) started", targetView);
        Assert.notNull(targetView, TARGET_NOT_NULL);
        Assert.hasText(targetView.getName(), NAME_NOT_NULL);
        Assert.notNull(targetView.getOs(), OS_NOT_NULL);
        checkSupported(targetView.getClass());

        final Target returnValue;

        // only ssh target is supported
        final var sshView = (SshTargetView) targetView;
        Assert.hasText(sshView.getAddress(), ADDRESS_NOT_NULL);
        Assert.notNull(sshView.getPort(), PORT_NOT_NULL);

        final var existingTarget = targetRepo.findByAddressAndPort(sshView.getAddress(), sshView.getPort());

        if (existingTarget.isEmpty()) {
            final var newTarget = new SshTarget(sshView.getName(), sshView.getOs(), sshView.getAddress(), sshView.getPort());
            returnValue = targetRepo.saveAndFlush(newTarget);
        } else {
            returnValue = existingTarget.get();
        }
        return returnValue;
    }

    @Transactional
    protected SshTarget getTargetById(final Long id) {
        logger.debug("getTargetById({}) started", id);
        Assert.notNull(id, ID_NOT_NULL);
        final Optional<SshTarget> optional = targetRepo.findById(id);
        return optional.orElseThrow(notFoundException("SSH target"));
    }
}

package eu.arrowhead.core.mscv.database.service;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import eu.arrowhead.common.database.view.mscv.SshTargetView;
import eu.arrowhead.common.database.view.mscv.TargetView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.Optional;

import static eu.arrowhead.core.mscv.MscvUtilities.ID_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.TARGET_NOT_NULL;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Service
public class MscvTargetService {

    private final SshTargetRepository targetRepo;

    @Autowired
    public MscvTargetService(final SshTargetRepository targetRepo) {
        super();
        this.targetRepo = targetRepo;
    }

    @Transactional(rollbackOn = Exception.class)
    public SshTargetView getTargetViewById(final Long id) {
        Assert.notNull(id, ID_NOT_NULL);
        final Optional<SshTargetView> optional = targetRepo.findViewById(id);
        return optional.orElseThrow(notFoundException("SSH target"));
    }

    @Transactional
    protected Target findOrCreateTarget(final TargetView targetView) {
        Assert.notNull(targetView, TARGET_NOT_NULL);
        checkSupported(targetView.getClass());

        final Target returnValue;

        // only ssh target is supported
        final var sshView = (SshTargetView) targetView;
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
        Assert.notNull(id, ID_NOT_NULL);
        final Optional<SshTarget> optional = targetRepo.findById(id);
        return optional.orElseThrow(notFoundException("SSH target"));
    }

    public void checkSupported(final Class<?> cls) {
        Assert.notNull(cls, "Argument must not be null");
        if (isSupported(cls)) {
            throw new IllegalArgumentException(cls.getSimpleName() + " is not supported");
        }
    }

    public boolean isSupported(final Class<?> cls) {
        Assert.notNull(cls, "Argument must not be null");
        return SshTargetView.class.isAssignableFrom(cls) || SshTarget.class.isAssignableFrom(cls);
    }


}

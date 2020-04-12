package eu.arrowhead.core.msvc.database.service;

import eu.arrowhead.core.msvc.database.entities.SshTarget;
import eu.arrowhead.core.msvc.database.entities.Target;
import eu.arrowhead.core.msvc.database.repositories.SshTargetRepository;
import eu.arrowhead.core.msvc.database.view.SshTargetView;
import eu.arrowhead.core.msvc.database.view.TargetView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;

@Service
public class MsvcTargetService {

    private final SshTargetRepository targetRepo;

    @Autowired
    public MsvcTargetService(final SshTargetRepository targetRepo) {
        super();
        this.targetRepo = targetRepo;
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

    @Transactional
    public Target findOrCreateTarget(final TargetView targetView) {
        Assert.notNull(targetView, "Argument must not be null");
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
}

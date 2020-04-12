package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.SshTarget;
import eu.arrowhead.core.msvc.database.entities.Target;
import eu.arrowhead.core.msvc.database.view.SshTargetView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SshTargetRepository extends RefreshableRepository<SshTarget, Long> {

    SshTargetView findViewByName(final String name);

    SshTargetView findViewByAddressAndPort(final String address, final Integer port);

    <S extends SshTarget> Optional<S> findByAddressAndPort(final String address, final Integer port);
}

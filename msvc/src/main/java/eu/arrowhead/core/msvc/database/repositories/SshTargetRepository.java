package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.core.msvc.database.entities.SshTarget;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SshTargetRepository extends TargetRepository<SshTarget> {

    Optional<SshTarget> findByAddressAndPort(final String address, final Integer port);

}

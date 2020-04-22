package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SshTargetRepository extends RefreshableRepository<SshTarget, Long> {

    List<SshTarget> findViewByName(final String name);

    <S extends SshTarget> Optional<S> findByAddressAndPort(final String address, final Integer port);

}

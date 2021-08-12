package eu.arrowhead.common.database.repository.mscv;

import java.util.List;
import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.dto.shared.mscv.OS;
import org.springframework.stereotype.Repository;

@Repository
public interface SshTargetRepository extends RefreshableRepository<SshTarget, Long> {

    List<SshTarget> findByName(final String name);

    <S extends SshTarget> Optional<S> findByAddressAndPort(final String address, final Integer port);

    <S extends Target> Optional<S> findByNameAndOs(final String name, final OS os);
}

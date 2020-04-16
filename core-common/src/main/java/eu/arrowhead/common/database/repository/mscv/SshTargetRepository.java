package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.view.mscv.SshTargetView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SshTargetRepository extends RefreshableRepository<SshTarget, Long> {

    List<SshTargetView> findViewByName(final String name);

    SshTargetView findViewByAddressAndPort(final String address, final Integer port);

    <S extends SshTarget> Optional<S> findByAddressAndPort(final String address, final Integer port);

    Optional<SshTargetView> findViewById(Long id);
}

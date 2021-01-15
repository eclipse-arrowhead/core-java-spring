package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoreographerExecutorRepository extends RefreshableRepository<ChoreographerExecutor, Long> {

    //=================================================================================================
    // methods

    public Optional<ChoreographerExecutor> findByAddressAndPortAndBaseUri(final String address, final int port, final String baseUri);
}

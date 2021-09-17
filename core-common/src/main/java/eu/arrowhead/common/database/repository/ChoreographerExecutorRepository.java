package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;

@Repository
public interface ChoreographerExecutorRepository extends RefreshableRepository<ChoreographerExecutor,Long> {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public Optional<ChoreographerExecutor> findByAddressAndPortAndBaseUri(final String address, final int port, final String baseUri);
    public Optional<ChoreographerExecutor> findByName(final String name);
}

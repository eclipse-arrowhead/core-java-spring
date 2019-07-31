package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Relay;

@Repository
public interface RelayRepository extends RefreshableRepository<Relay,Long> {
	
	public boolean existsByAddressAndPort(final String address, final int port);
	public Optional<Relay> findByAddressAndPort(final String address, final int port);

}
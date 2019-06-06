package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.System;

@Repository
public interface SystemRepository extends JpaRepository<System, Long> {	
	Optional<System> findBySystemNameAndAddressAndPort(final String systemName, final String address, final int port);
}

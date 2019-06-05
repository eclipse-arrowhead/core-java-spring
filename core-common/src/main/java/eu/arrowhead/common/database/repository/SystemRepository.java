package eu.arrowhead.common.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.System;

@Repository
public interface SystemRepository extends JpaRepository<System, Long> {	
	System findBySystemNameAndAddressAndPort(String systemName, String address, int port);
}

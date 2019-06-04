package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ServiceDefinition;

@Repository
public interface ServiceDefinitionRepository extends JpaRepository<ServiceDefinition, Long> {

	Optional<ServiceDefinition> findByServiceDefinition(String serviceDefinition);	
	
}

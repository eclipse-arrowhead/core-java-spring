package eu.arrowhead.common.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ServiceRegistry;

@Repository
public interface ServiceRegistryRepository extends JpaRepository<ServiceRegistry, Long> {

}

package eu.arrowhead.common.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Relay;

@Repository
public interface RelayRepository extends JpaRepository<Relay, Long> {

}

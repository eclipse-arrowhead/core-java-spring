package eu.arrowhead.common.database.repository;

import java.util.Optional;
import java.util.Set;

import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Knowledge;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeRepository extends RefreshableRepository<Knowledge, Long> {
    Optional<Knowledge> findByInstanceAndKey(final GamsInstance instance, final String key);

    Iterable<Knowledge> findByInstanceAndKeyIn(final GamsInstance instance, final String... key);

    Iterable<Knowledge> findByInstanceAndKeyIn(final GamsInstance instance, final Set<String> key);
}

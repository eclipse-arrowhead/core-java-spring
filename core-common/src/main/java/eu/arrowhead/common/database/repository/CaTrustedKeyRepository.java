package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.CaTrustedKey;

@Repository
public interface CaTrustedKeyRepository extends RefreshableRepository<CaTrustedKey, Long> {

    Optional<CaTrustedKey> findByHash(final String publicKeyHash);
}

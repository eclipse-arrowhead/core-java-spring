package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.InterCloudAuthorization;

@Repository
public interface InterCloudAuthorizationRepository extends RefreshableRepository<InterCloudAuthorization,Long> {

}

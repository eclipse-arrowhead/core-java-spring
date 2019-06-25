package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.IntraCloudAuthorization;

@Repository
public interface IntraCloudAuthorizationRepository extends RefreshableRepository<IntraCloudAuthorization,Long> {

}
package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;

@Repository
public interface CloudRepository extends RefreshableRepository<Cloud,Long> {

}
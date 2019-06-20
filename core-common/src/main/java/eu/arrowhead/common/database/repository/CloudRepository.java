package eu.arrowhead.common.database.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;

@Repository
public interface CloudRepository extends JpaRepository<Cloud,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<Cloud> findByOwnCloudAndSecure(final boolean ownCloud, final boolean secure);
}
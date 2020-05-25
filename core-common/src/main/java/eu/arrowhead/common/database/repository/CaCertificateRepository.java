package eu.arrowhead.common.database.repository;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.CaCertificate;

@Repository
public interface CaCertificateRepository extends RefreshableRepository<CaCertificate,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    public Optional<CaCertificate> findBySerial(final BigInteger serial);

	public Optional<CaCertificate> findByCommonNameAndSerial(final String commonName, final BigInteger serial);

	@Modifying
	@Query("update CaCertificate c set c.revokedAt = ?2 where c.id = ?1 and c.createdBy = ?3")
	boolean setRevokedById(long id, ZonedDateTime revokedAt, String createdBy);
}

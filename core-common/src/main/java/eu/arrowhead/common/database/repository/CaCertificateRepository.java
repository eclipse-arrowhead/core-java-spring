/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.repository;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.database.entity.CaCertificate;

@Repository
public interface CaCertificateRepository extends RefreshableRepository<CaCertificate,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    public Optional<CaCertificate> findBySerial(final BigInteger serial);

	public Optional<CaCertificate> findByCommonNameAndSerial(final String commonName, final BigInteger serial);

	@Modifying
	@Transactional
	@Query("update CaCertificate c set c.revokedAt = ?2 where c.id = ?1 and c.createdBy = ?3")
	int setRevokedById(long id, ZonedDateTime revokedAt, String createdBy);
}

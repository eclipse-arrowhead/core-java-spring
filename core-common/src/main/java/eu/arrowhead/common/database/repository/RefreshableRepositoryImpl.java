/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.repository;

import java.io.Serializable;
import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("squid:S00119")
public class RefreshableRepositoryImpl<T,ID extends Serializable> extends SimpleJpaRepository<T,ID> implements RefreshableRepository<T,ID> {
	
	//=================================================================================================
	// members

	private final EntityManager entityManager;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RefreshableRepositoryImpl(final JpaEntityInformation entityInformation, final EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityManager = entityManager;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	@Transactional
	public void refresh(final T t) {
		entityManager.refresh(t);
	}
}
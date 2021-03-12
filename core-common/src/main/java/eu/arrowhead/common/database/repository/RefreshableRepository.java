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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@SuppressWarnings("squid:S00119")
@NoRepositoryBean
public interface RefreshableRepository<T,ID extends Serializable> extends JpaRepository<T,ID> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void refresh(final T t);
}
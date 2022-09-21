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

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;

@Repository
public interface ServiceRegistryRepository extends RefreshableRepository<ServiceRegistry,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ServiceRegistry> findByServiceDefinitionAndSystemAndServiceUri(final ServiceDefinition serviceDefinition, final System system, final String serviceUri);
	public List<ServiceRegistry> findByServiceDefinition(final ServiceDefinition serviceDefinition);
	public Page<ServiceRegistry> findAllByServiceDefinition(final ServiceDefinition serviceDefinition, final Pageable page);
	public List<ServiceRegistry> findBySystem(final System system);
}
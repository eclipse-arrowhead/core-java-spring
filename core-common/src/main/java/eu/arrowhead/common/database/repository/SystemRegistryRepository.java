/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.SystemRegistry;

@Repository
public interface SystemRegistryRepository extends RefreshableRepository<SystemRegistry, Long> {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    List<SystemRegistry> findBySystem(final System system);

    List<SystemRegistry> findByDevice(final Device device);

    Optional<SystemRegistry> findBySystemAndDevice(final System systemDb, final Device deviceDb);

    List<SystemRegistry> findAllBySystemIsIn(final List<System> systems);

    Page<SystemRegistry> findAllBySystemIsIn(final List<System> systemList, final Pageable pageable);
}
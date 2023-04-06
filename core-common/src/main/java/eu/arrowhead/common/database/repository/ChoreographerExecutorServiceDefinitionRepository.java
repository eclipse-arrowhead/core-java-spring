/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;

import java.util.List;
import java.util.Optional;

public interface ChoreographerExecutorServiceDefinitionRepository extends RefreshableRepository<ChoreographerExecutorServiceDefinition,Long> {

    //=================================================================================================
    // methods

    public Optional<ChoreographerExecutorServiceDefinition> findByExecutorAndServiceDefinition(final ChoreographerExecutor executor, final String serviceDefinition);
    public List<ChoreographerExecutorServiceDefinition> findAllByExecutor(final ChoreographerExecutor executor);
    public List<ChoreographerExecutorServiceDefinition> findAllByServiceDefinition(final String serviceDefinition);
}

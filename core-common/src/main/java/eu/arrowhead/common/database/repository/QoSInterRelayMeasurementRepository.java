/********************************************************************************
 * Copyright (c) 2020 AITIA
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

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Repository
public interface QoSInterRelayMeasurementRepository extends RefreshableRepository<QoSInterRelayMeasurement,Long> {

	//-------------------------------------------------------------------------------------------------
	public Optional<QoSInterRelayMeasurement> findByCloudAndRelayAndMeasurementType(final Cloud cloud, final Relay relay, final QoSMeasurementType type);
	public List<QoSInterRelayMeasurement> findByCloudAndMeasurementType(final Cloud cloud, final QoSMeasurementType type);
}

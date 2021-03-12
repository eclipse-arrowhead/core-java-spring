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
import eu.arrowhead.common.database.entity.QoSInterDirectMeasurement;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Repository
public interface QoSInterDirectMeasurementRepository extends RefreshableRepository<QoSInterDirectMeasurement,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Optional<QoSInterDirectMeasurement> findByAddressAndMeasurementType(final String address, final QoSMeasurementType type);
	public List<QoSInterDirectMeasurement> findByCloudAndMeasurementType(final Cloud cloud, final QoSMeasurementType type);
	public Optional<QoSInterDirectMeasurement> findByCloudAndAddressAndMeasurementType(final Cloud cloud,  final String address, final QoSMeasurementType type);
}

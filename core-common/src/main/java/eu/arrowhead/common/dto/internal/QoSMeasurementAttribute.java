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

package eu.arrowhead.common.dto.internal;

public enum QoSMeasurementAttribute {
	MIN_RESPONSE_TIME, MAX_RESPONSE_TIME, MEAN_RESPONSE_TIME_WITH_TIMEOUT, MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT, JITTER_WITH_TIMEOUT, JITTER_WITHOUT_TIMEOUT, LOST_PER_MEASUREMENT_PERCENT;
}

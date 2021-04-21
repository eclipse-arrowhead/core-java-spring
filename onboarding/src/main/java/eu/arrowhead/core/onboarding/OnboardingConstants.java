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

package eu.arrowhead.core.onboarding;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;

public class OnboardingConstants {

    public static final String AUTHORIZATION_INTRA_CLOUD_MGMT_URI = CoreCommonConstants.MGMT_URI + "/intracloud";
    public static final String SERVICEREGISTRY_QUERY_BY_DTO_PATH = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI;
    public static final String SERVICE_REGISTRY_QUERY_BY_DTO_KEY = SERVICEREGISTRY_QUERY_BY_DTO_PATH + CoreCommonConstants.URI_SUFFIX;

    //-------------------------------------------------------------------------------------------------
    private OnboardingConstants() { super(); }
}

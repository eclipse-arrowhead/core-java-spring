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

package eu.arrowhead.core.deviceregistry.security;

import java.util.Map;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true)
public class DeviceRegistryAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members

	private final SecurityUtilities securityUtilities;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Autowired
	public DeviceRegistryAccessControlFilter(final SecurityUtilities securityUtilities) {
		this.securityUtilities = securityUtilities;
	}
	
    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON,
                                         final Map<String, String[]> queryParams) {

        if (requestTarget.endsWith(CommonConstants.ECHO_URI)
                || requestTarget.contains(CommonConstants.ONBOARDING_URI)
                || requestTarget.contains(CommonConstants.OP_DEVICEREGISTRY_UNREGISTER_URI)) {
            securityUtilities.authenticateCertificate(clientCN, requestTarget, CertificateType.AH_ONBOARDING);
            return;
        }

        super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
        final String cloudCN = getServerCloudCN();

        if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
            // Only the local System Operator can use these methods
            checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
        }
    }
}
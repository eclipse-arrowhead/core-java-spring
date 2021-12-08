/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.CommonConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CAProperties {

    @Value(CommonConstants.$CA_CERT_VALIDITY_NEG_OFFSET_MINUTES)
    private long certValidityNegativeOffsetMinutes;

    @Value(CommonConstants.$CA_CERT_VALIDITY_POS_OFFSET_MINUTES)
    private long certValidityPositiveOffsetMinutes;

    @Value(CommonConstants.$CA_CERT_KEYSTORE_TYPE)
    private String cloudKeyStoreType;

    @Value(CommonConstants.$CA_CERT_KEYSTORE_PATH)
    private Resource cloudKeyStorePath;

    @Value(CommonConstants.$CA_CERT_KEYSTORE_PASSWORD)
    private String cloudKeyStorePassword;

    @Value(CommonConstants.$CA_CERT_KEY_PASSWORD)
    private String cloudKeyPassword;

    public long getCertValidityNegativeOffsetMinutes() {
        return certValidityNegativeOffsetMinutes;
    }

    public long getCertValidityPositiveOffsetMinutes() {
        return certValidityPositiveOffsetMinutes;
    }

    public String getCloudKeyStoreType() {
        return cloudKeyStoreType;
    }

    public Resource getCloudKeyStorePath() {
        return cloudKeyStorePath;
    }

    public String getCloudKeyStorePassword() {
        return cloudKeyStorePassword;
    }

    public String getCloudKeyPassword() {
        return cloudKeyPassword;
    }
}

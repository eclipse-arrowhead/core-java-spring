package eu.arrowhead.core.certificate_authority;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;

@Component
public class CAProperties {

    @Value(CommonConstants.$CA_CERT_VALIDITY_NEG_OFFSET_MINUTES)
    private long certValidityNegativeOffsetMinutes;

    @Value(CommonConstants.$CA_CERT_VALIDITY_POS_OFFSET_MINUTES)
    private long certValidityPositiveOffsetMinutes;

    public long getCertValidityNegativeOffsetMinutes() {
        return certValidityNegativeOffsetMinutes;
    }

    public long getCertValidityPositiveOffsetMinutes() {
        return certValidityPositiveOffsetMinutes;
    }    
}

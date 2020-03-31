package eu.arrowhead.core.certificate_authority;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;

@Component
public class CAProperties {

    @Value(CommonConstants.$CA_CERT_VALIDITY_NEG_OFFSET_MILLIS)
    private long certValidityNegativeOffsetMillis;

    @Value(CommonConstants.$CA_CERT_VALIDITY_POS_OFFSET_MILLIS)
    private long certValidityPositiveOffsetMillis;

    public long getCertValidityNegativeOffsetMillis() {
        return certValidityNegativeOffsetMillis;
    }

    public long getCertValidityPositiveOffsetMillis() {
        return certValidityPositiveOffsetMillis;
    }    
}

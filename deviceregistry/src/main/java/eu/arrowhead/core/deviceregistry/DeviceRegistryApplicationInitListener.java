package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.ApplicationInitListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class DeviceRegistryApplicationInitListener extends ApplicationInitListener {

    //=================================================================================================
    // members

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {
        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}" + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }
    }
}
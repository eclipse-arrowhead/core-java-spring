package eu.arrowhead.core.gams;

import java.util.Base64;

import eu.arrowhead.common.ApplicationInitListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class GamsApplicationInitListener extends ApplicationInitListener {


    private final Logger logger = LogManager.getLogger(GamsApplicationInitListener.class);

    public GamsApplicationInitListener() {}

    //=================================================================================================
    // members

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {

        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }
    }
}
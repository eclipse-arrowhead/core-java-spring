package eu.arrowhead.core.mscv;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class MscvApplicationInitListener extends ApplicationInitListener {

    private final Logger logger = LogManager.getLogger(MscvApplicationInitListener.class);

    public MscvApplicationInitListener() {}

    //=================================================================================================
    // members

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {

        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }
    }

    @Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
        return List.of(CoreSystemService.EVENT_SUBSCRIBE_SERVICE);
    }
}
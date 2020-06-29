package eu.arrowhead.core.datamanager;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystemService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class DataManagerApplicationInitListener extends ApplicationInitListener {

    //=================================================================================================
    // members

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {
        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }
    }

//    @Override
//    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
//        return List.of();
//    }
}

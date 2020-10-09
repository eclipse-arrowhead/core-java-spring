package eu.arrowhead.core.translator.services.translator;

import eu.arrowhead.core.translator.services.translator.common.TranslatorDef.EndPoint;
import eu.arrowhead.core.translator.services.translator.spokes.BaseSpokeConsumer;
import eu.arrowhead.core.translator.services.translator.spokes.CoapConsumerSpoke;
import eu.arrowhead.core.translator.services.translator.spokes.CoapProducerSpoke;
import eu.arrowhead.core.translator.services.translator.spokes.HttpConsumerSpoke;
import eu.arrowhead.core.translator.services.translator.spokes.HttpProducerSpoke;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.arrowhead.core.translator.services.translator.spokes.BaseSpokeProducer;

public class TranslatorHub {
    
    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(TranslatorHub.class);
    private final int id;
    private final EndPoint pSpokeConsumer;
    private final EndPoint cSpokeProvider;
    private final BaseSpokeProducer pSpoke;
    private final BaseSpokeConsumer cSpoke;
    private final String hubIp = "0.0.0.0";
    private final int hubPort;
    public boolean noactivity = false;
    
    public TranslatorHub(int id, EndPoint pSpokeConsumer, EndPoint cSpokeProvider) throws Exception {
        logger.debug("NEW HUB");
        this.id = id;
        this.pSpokeConsumer = pSpokeConsumer;
        this.cSpokeProvider = cSpokeProvider;
                
        switch(pSpokeConsumer.getProtocol()) {
            case coap:
                pSpoke = new CoapProducerSpoke(hubIp);
                hubPort = new URI(pSpoke.getAddress()).getPort();
                break;
            case http:
                pSpoke = new HttpProducerSpoke(hubIp, "/*");
                hubPort = new URI(pSpoke.getAddress()).getPort();
                break;
            default:
                throw new Exception("Unknown protocol "+ pSpokeConsumer.getProtocol());
        }
        
        switch(cSpokeProvider.getProtocol()) {
            case coap:
                cSpoke = new CoapConsumerSpoke(cSpokeProvider.getHostIpAddress());
                break;
            case http:
                cSpoke = new HttpConsumerSpoke(cSpokeProvider.getHostIpAddress());
                break;
            default:
                throw new Exception("Unknown protocol "+ pSpokeConsumer.getProtocol());
        }
                
        // link the spoke connections 
        pSpoke.setNextSpoke(cSpoke);
        cSpoke.setNextSpoke(pSpoke);
        
        // Activity Monitor
        ScheduledExecutorService sesPrintReport = Executors.newSingleThreadScheduledExecutor();
        sesPrintReport.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                activityMonitor();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public int getTranslatorId() {
        return id;
    }
    
    //-------------------------------------------------------------------------------------------------
    public int getHubPort() {
        return hubPort;
    }
    
    
    //=================================================================================================
    // assistant methods
    //-------------------------------------------------------------------------------------------------
    private void activityMonitor() {
        if ((cSpoke.getLastActivity() > 0) || (pSpoke.getLastActivity() > 0)) {
            logger.debug("activityMonitor [{}] - Active", id);
            cSpoke.clearActivity();
            pSpoke.clearActivity();
        } else {
            logger.debug("activityMonitor [{}] - No Active", id);
        }
    }
}

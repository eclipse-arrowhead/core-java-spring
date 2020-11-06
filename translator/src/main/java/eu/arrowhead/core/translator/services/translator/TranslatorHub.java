package eu.arrowhead.core.translator.services.translator;

import eu.arrowhead.core.translator.services.translator.common.TranslatorDef.EndPoint;
import eu.arrowhead.core.translator.services.translator.spokes.BaseSpokeConsumer;
import eu.arrowhead.core.translator.services.translator.spokes.CoapConsumerSpoke;
import eu.arrowhead.core.translator.services.translator.spokes.CoapProducerSpoke;
import eu.arrowhead.core.translator.services.translator.spokes.HttpConsumerSpoke;
import eu.arrowhead.core.translator.services.translator.spokes.HttpProducerSpoke;

import eu.arrowhead.common.exception.ArrowheadException;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.arrowhead.core.translator.services.translator.spokes.BaseSpokeProducer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.apache.http.HttpStatus;

public class TranslatorHub {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(TranslatorHub.class);
    private final int id;
    private final BaseSpokeProducer pSpoke;
    private final BaseSpokeConsumer cSpoke;
    private final String hubIp = "0.0.0.0";
    private final int hubPort;

    public TranslatorHub(int id, EndPoint pSpokeConsumer, EndPoint cSpokeProvider) throws ArrowheadException {
        this.id = id;
        checkEndpoint(pSpokeConsumer);
        checkEndpoint(cSpokeProvider);

        try {

            switch (pSpokeConsumer.getProtocol()) {
                case coap:
                    pSpoke = new CoapProducerSpoke(hubIp);
                    hubPort = new URI(pSpoke.getAddress()).getPort();
                    break;
                case http:
                    pSpoke = new HttpProducerSpoke(hubIp, "/*");
                    hubPort = new URI(pSpoke.getAddress()).getPort();
                    break;
                default:
                    throw new ArrowheadException("Unknown protocol " + pSpokeConsumer.getProtocol(), HttpStatus.SC_BAD_REQUEST);
            }

            switch (cSpokeProvider.getProtocol()) {
                case coap:
                    cSpoke = new CoapConsumerSpoke(cSpokeProvider.getHostIpAddress());
                    break;
                case http:
                    cSpoke = new HttpConsumerSpoke(cSpokeProvider.getHostIpAddress());
                    break;
                default:
                    throw new ArrowheadException("Unknown protocol " + cSpokeProvider.getProtocol(), HttpStatus.SC_BAD_REQUEST);
            }
        } catch (URISyntaxException ex) {
            throw new ArrowheadException("Wrong URI Syntax", HttpStatus.SC_BAD_REQUEST, ex.getLocalizedMessage());
        } catch (IOException | InterruptedException ex) {
            throw new ArrowheadException(ex.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
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
            logger.info(String.format("activityMonitor [%d] - Active", id));
            cSpoke.clearActivity();
            pSpoke.clearActivity();
        } else {
            logger.info(String.format("activityMonitor [%d] - No Active", id));
        }
    }

    private void checkEndpoint(EndPoint endpoint) {
        if (endpoint == null) {
            throw new ArrowheadException("Null endpoint", HttpStatus.SC_BAD_REQUEST);
        }
        if (endpoint.getHostIpAddress() == null) {
            throw new ArrowheadException("No Host IpAddress", HttpStatus.SC_BAD_REQUEST);
        }
        if (endpoint.getName() == null) {
            throw new ArrowheadException("No name", HttpStatus.SC_BAD_REQUEST);
        }
        if (endpoint.getPort() <= 0 || endpoint.getPort() > 65535) {
            throw new ArrowheadException("No valid Port", HttpStatus.SC_BAD_REQUEST);
        }
        if (endpoint.getProtocol() == null) {
            throw new ArrowheadException("No Protocol", HttpStatus.SC_BAD_REQUEST);
        }
    }

}

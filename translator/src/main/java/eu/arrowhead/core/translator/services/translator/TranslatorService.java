package eu.arrowhead.core.translator.services.translator;

import eu.arrowhead.core.translator.services.translator.common.TranslatorSetup;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.core.translator.services.translator.common.TranslatorHubAccess;
import eu.arrowhead.core.translator.services.translator.common.TranslatorDef.EndPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class TranslatorService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(TranslatorService.class);
    private final Map<Integer, TranslatorHub> hubs = new HashMap<>();

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public void start() {
        logger.debug("Starting TranslatorService");
    }

    //-------------------------------------------------------------------------------------------------
    public TranslatorHubAccess createTranslationHub(TranslatorSetup setup, String outgoingIp) throws Exception {
        logger.debug("create Translation Hub");
        logger.debug("ip: " + outgoingIp);
        logger.debug("setup: " + new ObjectMapper().writeValueAsString(setup));

        EndPoint consumerEP = new EndPoint(setup.getConsumerName(), setup.getConsumerAddress());
        EndPoint producerEP = new EndPoint(setup.getProducerName(), setup.getProducerAddress());

        if (consumerEP.isLocal() || producerEP.isLocal()) {
            logger.warn("Not valid ip address, we need absolute address, not relative (as 127.0.0.1 or localhost)");
            throw new Exception("Not valid ip address, we need absolute address, not relative (as 127.0.0.1 or localhost)");
        } else {

            int translatorId = (producerEP.getName() + consumerEP.getName()).hashCode();
            if (!hubs.isEmpty() && hubs.containsKey(translatorId)) {
                logger.debug("Hub already exists!");
                TranslatorHub existingHub = hubs.get(translatorId);
                return new TranslatorHubAccess(existingHub.getTranslatorId(), outgoingIp, existingHub.getHubPort());
            } else {
                logger.debug("Creating a new Hub: ClientSpoke type:" + consumerEP.getProtocol() + " ServerSpoke type: " + producerEP.getProtocol());
                TranslatorHub hub = new TranslatorHub(translatorId, consumerEP, producerEP);
                hubs.put(translatorId, hub);
                return new TranslatorHubAccess(hub.getTranslatorId(), outgoingIp, hub.getHubPort());
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<TranslatorHubAccess> getAllHubs(String localOutgoingIp) {
        ArrayList<TranslatorHubAccess> response = new ArrayList<>();
        hubs.entrySet().forEach((entry) -> {
            response.add(new TranslatorHubAccess(entry.getValue().getTranslatorId(), localOutgoingIp, entry.getValue().getHubPort()));
        });
        return response;
    }

    //-------------------------------------------------------------------------------------------------
    public TranslatorHubAccess getHub(int translatorId, String localOutgoingIp) throws Exception {
        TranslatorHub hub = hubs.get(translatorId);
        if (hub == null) {
            throw new Exception("Not Found");
        }
        return new TranslatorHubAccess(translatorId, localOutgoingIp, hub.getHubPort());
    }

}

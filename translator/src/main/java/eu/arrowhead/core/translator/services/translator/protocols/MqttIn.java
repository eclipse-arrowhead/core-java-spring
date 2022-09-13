package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import eu.arrowhead.core.translator.services.translator.common.ContentTranslator;

public class MqttIn extends ProtocolIn {
    private MqttClient client;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private String topicId;
    private final String broker = "tcp://127.0.0.1:1883";

    public MqttIn(URI uri) throws Exception {
        super(uri);
        topicId = "" + uri.getPort();
        client = new MqttClient(broker, topicId, persistence);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            protocolOut.observe(new InterProtocolRequest("", "", MimeTypes.Type.TEXT_PLAIN, null));
        }).start();

    }

    private void publish(String topic, String msg) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish(topic, new MqttMessage(msg.getBytes()));
        } catch (MqttException ex) {
            // Ignore
        }
    }

    @Override
    synchronized void notifyObservers(InterProtocolResponse response) {

        // Translation
        response.setContent(
                ContentTranslator.translate(getContentType(), protocolOut.getContentType(), response.getContent()));

        publish(topicId + "/out", response.getContentAsString());
    }

}

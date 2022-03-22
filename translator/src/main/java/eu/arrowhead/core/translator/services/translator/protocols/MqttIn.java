package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttIn extends ProtocolIn {
    private MqttClient client;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private String topicId;
    private final String broker = "tcp://127.0.0.1:1883";

    public MqttIn(URI uri) throws Exception {
        super(uri);
        topicId = "" + uri.getPort();
        System.out.println("MQTTIN: " + topicId);
        client = new MqttClient(broker, topicId, persistence);

        /*
         * client.setCallback(new MqttCallback() {
         * 
         * @Override
         * public void connectionLost(Throwable thrwbl) {
         * }
         * 
         * @Override
         * public void messageArrived(String topic, MqttMessage message) throws
         * Exception {
         * System.out.println(String.format("TOPIC: %s  MSG:%s", topic, new
         * String(message.getPayload())));
         * }
         * 
         * @Override
         * public void deliveryComplete(IMqttDeliveryToken imdt) {
         * }
         * });
         * 
         * subscribe(topicId);
         */
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
            System.out.println("MqttException: " + ex.getLocalizedMessage());
        }
    }

    @Override
    synchronized void notifyObservers(InterProtocolResponse response) {
        publish(topicId + "/out", response.getContentAsString());
    }

}

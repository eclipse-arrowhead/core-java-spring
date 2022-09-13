package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttOut extends ProtocolOut {
    private MqttClient client;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private String topicId;
    private final String broker = "tcp://127.0.0.1:1883";
    private byte[] payloadBuffer = "Web Socket Connected".getBytes();

    public MqttOut(URI uri) throws Exception {
        super(uri);
        topicId = "" + uri.getPort();
        client = new MqttClient(broker, getRandomString(), persistence);

        client.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable thrwbl) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                protocolIn.notifyObservers(
                        new InterProtocolResponse(Type.TEXT_PLAIN,
                                HttpServletResponse.SC_OK, message.getPayload()));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {
            }
        });

        subscribe(topicId + "/out");

    }

    private void subscribe(String topic) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.subscribe(topic, 0);
        } catch (MqttException ex) {
            // Ignore
        }
    }

    @Override
    public InterProtocolResponse observe(InterProtocolRequest request) {

        return new InterProtocolResponse(
                MimeTypes.Type.TEXT_PLAIN,
                HttpServletResponse.SC_OK,
                payloadBuffer);
    }

    private String getRandomString() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString().toUpperCase();
        return generatedString;
    }
}

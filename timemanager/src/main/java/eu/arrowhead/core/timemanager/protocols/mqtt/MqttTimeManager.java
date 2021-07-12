package eu.arrowhead.core.timemanager.protocols.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.SslUtil;

import org.springframework.stereotype.Component;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

@Component
public class MqttTimeManager implements MqttCallback, Runnable {

    // =================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(MqttTimeManager.class);


    // =================================================================================================
    // methods
    // -------------------------------------------------------------------------------------------------
    @PostConstruct
    public void init() {
    }

    @Override
    public void run() {
    
    }

    @Override
    public void connectionLost(Throwable cause) {
    
    
    }
  
    @Override
    public void messageArrived(String topic, MqttMessage message) {
    
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
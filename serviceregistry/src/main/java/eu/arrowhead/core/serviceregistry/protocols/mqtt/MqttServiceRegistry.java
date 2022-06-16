package eu.arrowhead.core.serviceregistry.protocols.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.SslUtil;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.MqttRequestDTO;
import eu.arrowhead.common.dto.shared.MqttResponseDTO;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
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
public class MqttServiceRegistry implements MqttCallback, Runnable {

  // =================================================================================================
  // members
  private final Logger logger = LogManager.getLogger(MqttServiceRegistry.class);

  @Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
  private boolean gatekeeperIsPresent;

  @Autowired
  private ServiceRegistryDBService serviceRegistryDBService;

  @Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
  private String mqttSystemName;

  @Value(CoreCommonConstants.$MQTT_BROKER_ENABLED)
  private boolean mqttBrokerEnabled;

  @Value(CoreCommonConstants.$MQTT_BROKER_ADDRESS)
  private String mqttBrokerAddress;

  @Value(CoreCommonConstants.$MQTT_BROKER_PORT)
  private int mqttBrokerPort;

  @Value(CoreCommonConstants.$MQTT_BROKER_USERNAME)
  private String mqttBrokerUsername;

  @Value(CoreCommonConstants.$MQTT_BROKER_PASSWORD)
  private String mqttBrokerPassword;

  @Value(CoreCommonConstants.$MQTT_BROKER_CAFILE)
  private String mqttBrokerCAFile;

  @Value(CoreCommonConstants.$MQTT_BROKER_CERTFILE)
  private String mqttBrokerCertFile;

  @Value(CoreCommonConstants.$MQTT_BROKER_KEYFILE)
  private String mqttBrokerKeyFile;

  @Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
  private boolean serverSslEnabled;

  final String DIRECTION_KEY = "direction";
  final String DIRECTION_DEFAULT = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE;
  final String NULL_DEFAULT = null;
  final String PAGE_KEY = "page";
  final int PAGE_DEFAULT = -1;
  final String ID_KEY = "id";
  final int ID_DEFAULT = 0;
  final String SIZE_KEY = "size";
  final int SIZE_DEFAULT = -1;
  final String SORT_KEY = "sortField";
  final String SORT_DEFAULT = CoreCommonConstants.COMMON_FIELD_NAME_ID;
  final String UNREGISTER_SERVICE_DEFINITION_KEY = "service_definition";
  final String UNREGISTER_SERVICE_PROVIDER_ADDRESS_KEY = "address";
  final String UNREGISTER_SERVICE_PROVIDER_PORT_KEY = "port";
  final String UNREGISTER_SERVICE_PROVIDER_SYSTEM_NAME_KEY = "system_name";

  final String ECHO_TOPIC = "ah/serviceregistry/echo";
  final String REGISTER_TOPIC = "ah/serviceregistry/register";
  final String UNREGISTER_TOPIC = "ah/serviceregistry/unregister";
  final String QUERY_TOPIC = "ah/serviceregistry/query";

  Thread t = null;

  // =================================================================================================
  // methods
  // -------------------------------------------------------------------------------------------------
  @PostConstruct
  public void init() {
    
    if (mqttBrokerEnabled) {
      logger.info("Starting MQTT protocol");

      if(Utilities.isEmpty(mqttBrokerUsername) || Utilities.isEmpty(mqttBrokerPassword)) {
        logger.info("Missing MQTT broker username or password! Using anonymoues login.");
      }

      if(Utilities.isEmpty(mqttBrokerCAFile) || Utilities.isEmpty(mqttBrokerCertFile) || Utilities.isEmpty(mqttBrokerKeyFile)) {
        logger.info("Missing MQTT broker certificate/key files! Running without encryption");
      }
      
      t = new Thread(this);
      t.start();
    }
  }

  MqttClient client = null;
  MemoryPersistence persistence = null;

  private void connectBroker() {

    try {
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      connOpts.setUserName(mqttBrokerUsername);
      connOpts.setPassword(mqttBrokerPassword.toCharArray());

      connOpts.setConnectionTimeout(60);
      connOpts.setKeepAliveInterval(60);
      connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

      if (!Utilities.isEmpty(mqttBrokerCAFile) && !Utilities.isEmpty(mqttBrokerCertFile) && !Utilities.isEmpty(mqttBrokerKeyFile)) {
      	SSLSocketFactory socketFactory = null;
        try {
          socketFactory = SslUtil.getSslSocketFactory(mqttBrokerCAFile, mqttBrokerCertFile, mqttBrokerKeyFile, "");
        } catch (Exception e) {
          logger.info("Could not open certificates: " + e.toString());
        }

      	connOpts.setSocketFactory(socketFactory);
      }
      
      client.setCallback(this);
      client.connect(connOpts);

      String topics[] = { ECHO_TOPIC, REGISTER_TOPIC, UNREGISTER_TOPIC, QUERY_TOPIC };
      client.subscribe(topics);
    } catch (MqttException mex) {
      logger.info("Could not connect to MQTT broker!\n\t" + mex.toString());
    }

  }

  @Override
  public void run() {

    while (true) {
      try {
        if (client == null) {
          persistence = new MemoryPersistence();
      	  if (!Utilities.isEmpty(mqttBrokerCAFile) && !Utilities.isEmpty(mqttBrokerCertFile) && !Utilities.isEmpty(mqttBrokerKeyFile)) {
            client = new MqttClient("ssl://" + mqttBrokerAddress + ":" + mqttBrokerPort, mqttSystemName, persistence);
	  } else {
            client = new MqttClient("tcp://" + mqttBrokerAddress + ":" + mqttBrokerPort, mqttSystemName, persistence);
	  }
        }
        if (!client.isConnected()) {
          connectBroker();
        }
        Thread.sleep(1000 * 15);
      } catch (InterruptedException iex) {
        logger.info("Error starting MQTT timeout thread");
      } catch (MqttException mex) {
        logger.info("MQTT error: " + mex.toString());
      }
    }

  }

  @Override
  public void connectionLost(Throwable cause) {
    logger.info("Connection lost to MQTT broker");
    client = null;
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
    MqttRequestDTO request = null;
    MqttResponseDTO response = null;
    ObjectMapper mapper;

    try {
      // request = Utilities.fromJson(message.toString(), MqttRequestDTO.class);
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      request = mapper.readValue(message.toString(), MqttRequestDTO.class);
    } catch (Exception ae) {
      logger.info("Could not convert MQTT message to REST request!");
      return;
    }

    logger.info(request.toString());

    switch (topic) {
      case ECHO_TOPIC:
        logger.info(request.getMethod() + " echo(): " + new String(message.getPayload(), StandardCharsets.UTF_8));
        if (!request.getMethod().toLowerCase().equals("get")) {
          return;
        }
        try {
          response = new MqttResponseDTO("200", "text/plain", "Got it");
          MqttMessage resp = new MqttMessage(Utilities.toJson(response).getBytes());
          resp.setQos(2);
          client.publish(request.getReplyTo(), resp);
          return;
        } catch (MqttException mex) {
          logger.info("echo(): Couldn't reply " + mex.toString());
        }
        break;
      case REGISTER_TOPIC:

        try {
          logger.info("register(): " + new String(message.getPayload(), StandardCharsets.UTF_8));
          if (!request.getMethod().toLowerCase().equals("post")) {
            return;
          }

          ServiceRegistryRequestDTO serviceRegistryRequestDTO = mapper.convertValue(request.getPayload(), ServiceRegistryRequestDTO.class);

          if (Utilities.isEmpty(serviceRegistryRequestDTO.getServiceDefinition())) {
            throw new Exception("Service definition is null or blank");
          }

          if (!Utilities.isEmpty(serviceRegistryRequestDTO.getEndOfValidity())) {
            try {
              Utilities.parseUTCStringToLocalZonedDateTime(serviceRegistryRequestDTO.getEndOfValidity().trim());
            } catch (final DateTimeParseException ex) {
              throw new Exception("End of validity is specified in the wrong format. Please provide UTC time using "
                  + "ISO 8601  pattern.");
            }
          }

          ServiceSecurityType securityType = null;
          if (serviceRegistryRequestDTO.getSecure() != null) {
            for (final ServiceSecurityType type : ServiceSecurityType.values()) {
              if (type.name().equalsIgnoreCase(serviceRegistryRequestDTO.getSecure())) {
                securityType = type;
                break;
              }
            }

            if (securityType == null) {
              throw new Exception("Security type is not valid.");
            }
          } else {
            securityType = ServiceSecurityType.NOT_SECURE;
          }

          if (securityType != ServiceSecurityType.NOT_SECURE && serviceRegistryRequestDTO.getProviderSystem().getAuthenticationInfo() == null) {
            throw new Exception("Security type is in conflict with the availability of the authentication info.");
          }

          //logger.info("SRREQ:: " + serviceRegistryRequestDTO.toString());
          response = new MqttResponseDTO("200", "application/json", null);
          response.setPayload(serviceRegistryDBService.registerServiceResponse(serviceRegistryRequestDTO));
          MqttMessage resp = new MqttMessage(mapper.writeValueAsString(response).getBytes());
          resp.setQos(2);
          client.publish(request.getReplyTo(), resp);
          return;

        } catch (Exception e) {
          logger.info("Could not register: " + e.toString());

        }
        break;
      case UNREGISTER_TOPIC:
        logger.info("unregister(): " + message.toString());
        if (!request.getMethod().equalsIgnoreCase("delete")) {
          return;
        }

        try {
          String serviceDefinition = request.getQueryParameters().get("serviceDefinition");
          String providerName = request.getQueryParameters().get("providerName");
          String providerAddress = request.getQueryParameters().get("providerAddress");
          int providerPort = Integer.parseInt(request.getQueryParameters().get("providerPort"));
          String serviceUri = request.getQueryParameters().get("serviceUri");

          if (Utilities.isEmpty(serviceDefinition)) {
            throw new Exception("Service definition is blank");
          }

          if (Utilities.isEmpty(providerName)) {
            throw new Exception("Name of the provider system is blank");
          }

          if (Utilities.isEmpty(providerAddress)) {
            throw new Exception("Address of the provider system is blank");
          }

          if (providerPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || providerPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new Exception("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
          }
          serviceRegistryDBService.removeServiceRegistry(serviceDefinition, providerName, providerAddress, providerPort, serviceUri);

          return;
        } catch (Exception e) {
          logger.info("illegal request: " + e.toString());
        }

        break;
      case QUERY_TOPIC:
        logger.info("query(): " + message.toString());
        if (!request.getMethod().toLowerCase().equals("post")) {
          return;
        }

        try {
          ServiceQueryFormDTO serviceQueryFormDTO = mapper.convertValue(request.getPayload(),
              ServiceQueryFormDTO.class);

          if (Utilities.isEmpty(serviceQueryFormDTO.getServiceDefinitionRequirement())) {
            throw new Exception("Service definition requirement is null or blank");
          }

          //logger.info("SRQUERY:: " + serviceQueryFormDTO.toString());
          response = new MqttResponseDTO("200", "application/json", null);
          response.setPayload(serviceRegistryDBService.queryRegistry(serviceQueryFormDTO));
          MqttMessage resp = new MqttMessage(mapper.writeValueAsString(response).getBytes());
          resp.setQos(2);
          client.publish(request.getReplyTo(), resp);
          return;
        } catch (Exception e) {

        }

        break;
      default:
        logger.info("Received message to unsupported topic");
    }

    try {
      MqttResponseDTO srResponse = new MqttResponseDTO("400", null, null);
      String respJson = mapper.convertValue(srResponse, String.class);
      MqttMessage resp = new MqttMessage(respJson.getBytes());
      resp.setQos(2);
      client.publish(request.getReplyTo(), resp);
    } catch (Exception me) {
      logger.info("Could not reply: " + me.toString());
    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }

}

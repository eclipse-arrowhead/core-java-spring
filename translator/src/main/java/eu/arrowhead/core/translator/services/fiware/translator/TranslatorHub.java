package eu.arrowhead.core.translator.services.translator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpStatus;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.translator.services.translator.common.TranslatorHubDTO;
import eu.arrowhead.core.translator.services.translator.common.TranslatorSetupDTO;
import eu.arrowhead.core.translator.services.translator.protocols.CoapIn;
import eu.arrowhead.core.translator.services.translator.protocols.CoapOut;
import eu.arrowhead.core.translator.services.translator.protocols.ProtocolIn;
import eu.arrowhead.core.translator.services.translator.protocols.ProtocolOut;

public class TranslatorHub {
  // =================================================================================================
  // members
  private final int id;
  private final TranslatorSetupDTO setup;
  private ProtocolIn protocolIn;
  private ProtocolOut protocolOut;

  public TranslatorHub(int id, TranslatorSetupDTO setup) {
    this.setup = setup;
    this.id = id;
    startHub();
  }

  // =================================================================================================
  // methods
  // -------------------------------------------------------------------------------------------------
  public TranslatorHubDTO getDTO() {
    return new TranslatorHubDTO(id, setup.getIn(), setup.getOut());
  }

  // =================================================================================================
  // assistant methods
  // -------------------------------------------------------------------------------------------------
  private void startHub() {
    try {

      switch (setup.getIn().getProtocol()) {
        case COAP:
          setup.getIn().setPort(getAvailablePort());
          protocolIn = new CoapIn(new URI("coap://0.0.0.0:" + setup.getIn().getPort()));
          break;
        case HTTP:
          break;
        case MQTT:
          break;
        case WS:
          break;
        default:
          throw new ArrowheadException("Unknown Protocol In: " + setup.getIn().getProtocol(),
              HttpStatus.SC_INTERNAL_SERVER_ERROR);
      }

      switch (setup.getOut().getProtocol()) {
        case COAP:
          protocolOut = new CoapOut(new URI("coap://" + setup.getOut().getIp() + ":" + setup.getOut().getPort()));
          break;
        case HTTP:
          break;
        case MQTT:
          break;
        case WS:
          break;
        default:
          throw new ArrowheadException("Unknown Protocol Out: " + setup.getOut().getProtocol(),
              HttpStatus.SC_INTERNAL_SERVER_ERROR);
      }

      protocolIn.setProtocolOut(protocolOut);
      protocolOut.setProtocolIn(protocolIn);

    } catch (Exception ex) {
      throw new ArrowheadException("Problems to start a hub: " + ex.getLocalizedMessage(),
          HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

  }

  // -------------------------------------------------------------------------------------------------
  private int getAvailablePort() {
    try {
      ServerSocket ss = new ServerSocket(0);
      int port = ss.getLocalPort();
      ss.close();
      return port;
    } catch (IOException ex) {
      throw new ArrowheadException("Problems to start a empty port: " + ex.getLocalizedMessage(),
          HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

  }
}

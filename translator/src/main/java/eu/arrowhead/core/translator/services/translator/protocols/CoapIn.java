package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.jetty.http.MimeTypes;

import eu.arrowhead.core.translator.services.translator.common.ContentTranslator;
import eu.arrowhead.core.translator.services.translator.common.Translation;

public class CoapIn extends ProtocolIn {

  private final CoapServer coapServer;
  // private final Resource resource;

  public CoapIn(URI uri) {
    super(uri);
    coapServer = new RootCoapServer(uri.getPort());
    coapServer.start();
  }

  private class RootCoapServer extends CoapServer {

    public RootCoapServer(final int... ports) {
      super(NetworkConfig.getStandard(), ports);
    }

    @Override
    protected Resource createRoot() {
      return new CustomRootResource();
    }
  }

  private class CustomRootResource extends CoapResource {

    private byte[] payload = "Loading...".getBytes();
    private int rc = HttpServletResponse.SC_OK;
    private String ct = MimeTypes.Type.TEXT_PLAIN.asString();

    public CustomRootResource() {
      super("");
      setObservable(true);
      setObserveType(CoAP.Type.CON);
      getAttributes().setObservable();
    }

    @Override
    public Resource getChild(String name) {
      return this;
    }

    public void notifyObservers(InterProtocolResponse response) {

      // Translation
      response.setContent(
          ContentTranslator.translate(getContentType(), protocolOut.getContentType(), response.getContent()));

      payload = response.getContent();
      rc = response.getStatusCode();
      ct = response.getContentType();
      new Thread(() -> {
        changed();
      }).start();
    }

    @Override
    public void handleGET(CoapExchange exchange) {

      try {
        Request request = exchange.advanced().getRequest();
        Response response = exchange.advanced().getResponse();
        URI uri = new URI(request.getURI());

        if (request.isObserve()) {
          if (response == null) {
            new Thread(() -> {
              protocolOut.observe(new InterProtocolRequest(uri.getPath(), uri.getQuery(),
                  Translation.contentFormatFromCoap(request.getOptions().getContentFormat()), null));
            }).start();
          }
          sendResponse(exchange, new InterProtocolResponse(ct, rc, payload));

        } else {
          sendResponse(exchange, protocolOut.get(new InterProtocolRequest(uri.getPath(), uri.getQuery(),
              Translation.contentFormatFromCoap(request.getOptions().getContentFormat()), null)));
        }

      } catch (URISyntaxException ex) {
        exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
      }
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
      try {
        Request request = exchange.advanced().getRequest();
        URI uri = new URI(request.getURI());

        sendResponse(exchange, protocolOut.post(new InterProtocolRequest(uri.getPath(), uri.getQuery(),
            Translation.contentFormatFromCoap(request.getOptions().getContentFormat()), request.getPayload())));
      } catch (URISyntaxException ex) {
        exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
      }
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
      try {
        Request request = exchange.advanced().getRequest();
        URI uri = new URI(request.getURI());

        sendResponse(exchange, protocolOut.put(new InterProtocolRequest(uri.getPath(), uri.getQuery(),
            Translation.contentFormatFromCoap(request.getOptions().getContentFormat()), request.getPayload())));
      } catch (URISyntaxException ex) {
        exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
      }
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
      try {
        Request request = exchange.advanced().getRequest();
        URI uri = new URI(request.getURI());

        sendResponse(exchange, protocolOut.delete(new InterProtocolRequest(uri.getPath(), uri.getQuery(),
            Translation.contentFormatFromCoap(request.getOptions().getContentFormat()), null)));
      } catch (URISyntaxException ex) {
        exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
      }
    }

    // Private
    private void sendResponse(CoapExchange exchange, InterProtocolResponse response) {
      exchange.respond(Translation.statusToCoap(response.getStatusCode()), response.getContent(),
          Translation.contentFormatToCoap(response.getContentType()));
    }

  }

  @Override
  synchronized void notifyObservers(InterProtocolResponse response) {
    CustomRootResource rr = (CustomRootResource) coapServer.getRoot();
    rr.notifyObservers(response);
  }
}

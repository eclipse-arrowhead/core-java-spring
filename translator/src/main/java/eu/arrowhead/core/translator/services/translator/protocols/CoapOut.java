package eu.arrowhead.core.translator.services.translator.protocols;

import java.io.IOException;
import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.jetty.http.MimeTypes;

import eu.arrowhead.core.translator.services.translator.common.Translation;

public class CoapOut extends ProtocolOut {

    public CoapOut(URI uri) throws Exception {
        super(uri);
    }

    @Override
    public InterProtocolResponse get(InterProtocolRequest request) {
        String remoteRequest = uri.toString() + request.getPath()
                + (request.getQueries() == null ? "" : "?" + request.getQueries());
        try {
            CoapClient coapClient = new CoapClient(remoteRequest);

            CoapResponse response = coapClient.get();

            return new InterProtocolResponse(
                    Translation.contentFormatFromCoap(response.getOptions().getContentFormat()),
                    Translation.statusFromCoap(response.getCode()), response.getPayload());

        } catch (ConnectorException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (IOException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse post(InterProtocolRequest request) {
        String remoteRequest = uri.toString() + request.getPath() + "?" + request.getQueries();
        try {
            CoapClient coapClient = new CoapClient(remoteRequest);

            CoapResponse response = coapClient.post(request.getContent(),
                    Translation.contentFormatToCoap(request.getContentType()));

            return new InterProtocolResponse(
                    Translation.contentFormatFromCoap(response.getOptions().getContentFormat()),
                    Translation.statusFromCoap(response.getCode()), response.getPayload());

        } catch (ConnectorException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (IOException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse put(InterProtocolRequest request) {
        String remoteRequest = uri.toString() + request.getPath() + "?" + request.getQueries();
        try {
            CoapClient coapClient = new CoapClient(remoteRequest);

            CoapResponse response = coapClient.put(request.getContent(),
                    Translation.contentFormatToCoap(request.getContentType()));

            return new InterProtocolResponse(
                    Translation.contentFormatFromCoap(response.getOptions().getContentFormat()),
                    Translation.statusFromCoap(response.getCode()), response.getPayload());

        } catch (ConnectorException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (IOException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse delete(InterProtocolRequest request) {
        String remoteRequest = uri.toString() + request.getPath() + "?" + request.getQueries();
        try {
            CoapClient coapClient = new CoapClient(remoteRequest);

            CoapResponse response = coapClient.delete();

            return new InterProtocolResponse(
                    Translation.contentFormatFromCoap(response.getOptions().getContentFormat()),
                    Translation.statusFromCoap(response.getCode()), response.getPayload());

        } catch (ConnectorException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (IOException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse observe(InterProtocolRequest request) {
        String remoteRequest = uri.toString() + request.getPath() + "?" + request.getQueries();
        try {
            CoapClient coapClient = new CoapClient(remoteRequest);

            CoapResponse response = coapClient.get();

            coapClient.observe(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {

                    new Thread(() -> {
                        protocolIn.notifyObservers(new InterProtocolResponse(
                                Translation.contentFormatFromCoap(response.getOptions().getContentFormat()),
                                Translation.statusFromCoap(response.getCode()), response.getPayload()));

                    }).start();
                }

                @Override
                public void onError() {
                    new Thread(() -> {
                        protocolIn.notifyObservers(new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN,
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CoAP observe Error".getBytes()));
                    }).start();
                }
            });

            return new InterProtocolResponse(
                    Translation.contentFormatFromCoap(response.getOptions().getContentFormat()),
                    response.getCode().value, response.getPayload());

        } catch (ConnectorException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (IOException ex) {
            return new InterProtocolResponse(MimeTypes.Type.TEXT_PLAIN, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }
    }

}

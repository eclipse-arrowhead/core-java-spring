package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class WsOut extends ProtocolOut {

    private final WebSocketClient wsClient;
    private final SocketAdapter socket;
    private byte[] payloadBuffer = "Web Socket Connected".getBytes();

    public WsOut(URI uri) throws Exception {
        super(uri);
        socket = new SocketAdapter();
        wsClient = new WebSocketClient();
    }

    @Override
    public InterProtocolResponse get(InterProtocolRequest request) {
        return observe(request);
    }

    @Override
    public InterProtocolResponse post(InterProtocolRequest request) {
        String remoteRequest;
        try {
            remoteRequest = new URI(uri.getScheme(), uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                    request.getPath(), request.getQueries()).toString();
        } catch (URISyntaxException ex) {
            remoteRequest = uri.toString();
        }
        try {
            if (!wsClient.isRunning()) {
                wsClient.start();
            }

            if (socket.isNotConnected()) {
                wsClient.connect(socket, URI.create(remoteRequest));
                Thread.sleep(500); // Give time to connect
            }

            if (socket.isConnected()) {
                socket.getSession().getRemote().sendBytes(ByteBuffer.wrap(request.getContent()));
                return new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_OK,
                        "Websocket Sent!".getBytes());
            } else {
                return new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Impossible to connect to the Websocket Server".getBytes());
            }

        } catch (Exception ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }

    }

    @Override
    public InterProtocolResponse observe(InterProtocolRequest request) {
        String remoteRequest;
        try {
            remoteRequest = new URI(uri.getScheme(), uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                    request.getPath(), request.getQueries()).toString();
        } catch (URISyntaxException ex) {
            remoteRequest = uri.toString();
        }

        try {
            if (!wsClient.isRunning()) {
                wsClient.start();
            }

            if (socket.isNotConnected()) {
                wsClient.connect(socket, URI.create(remoteRequest));
                Thread.sleep(500); // Give time to connect
            }

            if (socket.isConnected()) {
                return new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_OK,
                        payloadBuffer);
            } else {
                return new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Impossible to connect to the Websocket Server".getBytes());
            }

        } catch (Exception ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    private class SocketAdapter extends WebSocketAdapter {

        public SocketAdapter() {
        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            payloadBuffer = payload;
            protocolIn.notifyObservers(new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_OK,
                    payload));
        }

        @Override
        public void onWebSocketText(String message) {
            payloadBuffer = message.getBytes();
            protocolIn.notifyObservers(new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_OK,
                    message.getBytes()));
        }
    }
}

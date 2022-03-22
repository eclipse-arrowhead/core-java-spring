package eu.arrowhead.core.translator.services.translator.protocols;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WsIn extends ProtocolIn {

    private final Server wsServer = new Server();
    private final WsServlet wsServlet = new WsServlet();

    public WsIn(URI uri) throws Exception {
        super(uri);
        HttpConfiguration config = new HttpConfiguration();
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(config);
        ServerConnector httpConnector = new ServerConnector(wsServer, httpConnectionFactory);
        httpConnector.setPort(uri.getPort());
        wsServer.addConnector(httpConnector);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder("ws", wsServlet), "/*");
        wsServer.setHandler(handler);
        wsServer.start();
    }

    @Override
    synchronized void notifyObservers(InterProtocolResponse response) {
        wsServlet.notifyAllSessions(response);
    }

    private class WsServlet extends WebSocketServlet {

        private final ArrayList<Session> sessions;

        WsServlet() {
            sessions = new ArrayList();
        }

        void addSession(Session session) {
            sessions.add(session);
        }

        void removeSession(Session session) {
            sessions.remove(session);
        }

        void notifyAllSessions(InterProtocolResponse response) {
            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.getRemote().sendBytes(ByteBuffer.wrap(response.getContent()));
                    }
                } catch (IOException ex) {
                    System.err.println("notifyAll Exception: " + ex.getLocalizedMessage());
                }
            });
        }

        @Override
        public void configure(WebSocketServletFactory wssf) {
            wssf.setCreator(new WsCreator(this));
        }
    }

    private class WsCreator implements WebSocketCreator {

        private final WsServlet wsServlet;

        WsCreator(WsServlet wsServlet) {
            this.wsServlet = wsServlet;
        }

        @Override
        public Object createWebSocket(ServletUpgradeRequest sur, ServletUpgradeResponse sur1) {
            return new Ws(wsServlet);
        }

        @WebSocket(maxTextMessageSize = 100 * 1024 * 1024, maxBinaryMessageSize = 64 * 1024)
        public class Ws {

            private final WsServlet wsServlet;
            private String path;
            private String query;

            public Ws(WsServlet wsServlet) {
                this.wsServlet = wsServlet;
            }

            @OnWebSocketConnect
            public void onWebSocketConnect(Session session) {
                path = session.getUpgradeRequest().getRequestURI().getPath();
                query = session.getUpgradeRequest().getRequestURI().getQuery();
                wsServlet.addSession(session);

                protocolOut.observe(new InterProtocolRequest(
                        path,
                        query,
                        MimeTypes.Type.TEXT_PLAIN,
                        null));
            }

            @OnWebSocketClose
            public void onWebSocketClose(Session session, int statusCode, String reason) {
                // System.out.println("On onWebSocketClose: " + session.getRemoteAddress());
                wsServlet.removeSession(session);
            }

            @OnWebSocketError
            public void onWebSocketError(Throwable cause) {
                // System.out.println("On onWebSocketError: " + cause);
            }

            @OnWebSocketMessage
            public void onBinary(Session session, byte buf[], int offset, int length) throws IOException {
                // System.out.println("On Binary: " + new String(buf));
                wsServlet.notifyAllSessions(protocolOut.post(
                        new InterProtocolRequest(
                                path,
                                query,
                                MimeTypes.Type.MULTIPART_BYTERANGES,
                                buf)));
            }

            @OnWebSocketMessage
            public void onText(Session session, String message) {
                // System.out.println("On onText: " + message);
                wsServlet.notifyAllSessions(protocolOut.post(
                        new InterProtocolRequest(
                                path,
                                query,
                                MimeTypes.Type.TEXT_PLAIN,
                                message.getBytes())));
            }
        }

    }
}

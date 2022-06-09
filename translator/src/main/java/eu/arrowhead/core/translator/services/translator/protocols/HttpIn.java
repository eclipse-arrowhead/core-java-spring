package eu.arrowhead.core.translator.services.translator.protocols;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.AsyncContextState;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import eu.arrowhead.core.translator.services.translator.common.ContentTranslator;

public class HttpIn extends ProtocolIn {

    private final Server httpServer = new Server();
    private final HttpService httpService = new HttpService();

    public HttpIn(URI uri) throws Exception {
        super(uri);
        HttpConfiguration config = new HttpConfiguration();
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(config);
        ServerConnector httpConnector = new ServerConnector(httpServer, httpConnectionFactory);
        httpConnector.setPort(uri.getPort());
        httpServer.addConnector(httpConnector);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(httpService), "/*");
        httpServer.setHandler(handler);
        httpServer.start();
    }

    private class HttpService extends HttpServlet {

        private ArrayList<AsyncContextState> contexts = new ArrayList<AsyncContextState>();

        @Override
        public void service(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            if (request.getMethod().equalsIgnoreCase("PATCH")) {
                doPatch(request, response);
            } else {
                super.service(request, response);
            }
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

            if (req.getHeader("Connection") == null || !req.getHeader("Connection").equals("keep-alive")) {

                InterProtocolResponse ipr = protocolOut.get(new InterProtocolRequest(
                        req.getPathInfo(),
                        req.getQueryString(),
                        req.getContentType(),
                        null));

                sendResponse(resp, ipr);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/event-stream");
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.flushBuffer();
            AsyncContextState async = (AsyncContextState) req.startAsync();
            contexts.add(async);
            protocolOut.observe(
                    new InterProtocolRequest(
                            req.getPathInfo(),
                            req.getQueryString(),
                            req.getContentType(),
                            null));
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            byte[] content = new byte[] {};
            try {
                content = getContentAsByteArray(req);
            } catch (IOException ex) {
                sendResponse(resp, new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ex.getLocalizedMessage().getBytes()));
            }
            sendResponse(resp, protocolOut.post(
                    new InterProtocolRequest(
                            req.getPathInfo(),
                            req.getQueryString(),
                            req.getContentType(),
                            content)));
        }

        // @Override
        protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            byte[] content = new byte[] {};
            try {
                content = getContentAsByteArray(req);
            } catch (IOException ex) {
                sendResponse(resp, new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ex.getLocalizedMessage().getBytes()));
            }
            sendResponse(resp, protocolOut.patch(
                    new InterProtocolRequest(
                            req.getPathInfo(),
                            req.getQueryString(),
                            req.getContentType(),
                            content)));
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            byte[] content = new byte[] {};
            try {
                content = getContentAsByteArray(req);
            } catch (IOException ex) {
                sendResponse(resp, new InterProtocolResponse(
                        MimeTypes.Type.TEXT_PLAIN,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ex.getLocalizedMessage().getBytes()));
            }
            sendResponse(resp, protocolOut.put(
                    new InterProtocolRequest(
                            req.getPathInfo(),
                            req.getQueryString(),
                            req.getContentType(),
                            content)));
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            sendResponse(resp, protocolOut.delete(
                    new InterProtocolRequest(
                            req.getPathInfo(),
                            req.getQueryString(),
                            req.getContentType(),
                            null)));
        }

        // PRIVATE
        private void sendResponse(HttpServletResponse resp, InterProtocolResponse ipr) throws IOException {
            resp.setContentType(ipr.getContentType());
            if (ipr.getContent() != null) {
                resp.getOutputStream().write(ipr.getContent());
            }
            resp.setStatus(ipr.getStatusCode());
        }

        private byte[] getContentAsByteArray(HttpServletRequest req) throws IOException {
            if (req.getContentLength() <= 0) {
                return null;
            }
            byte[] b = IOUtils.toByteArray(req.getInputStream());
            return b;
        }

        public void notifyObservers(InterProtocolResponse response) {

            // Translation
            response.setContent(
                    ContentTranslator.translate(getContentType(), protocolOut.getContentType(), response.getContent()));

            contexts.forEach(cont -> {
                try {
                    ServletOutputStream outputStream = cont.getResponse().getOutputStream();
                    if (outputStream.isReady()) {
                        if (response.getContent() == null) {
                            outputStream.write("".getBytes(), 0, "".getBytes().length);
                        } else {
                            outputStream.write(response.getContent(), 0, response.getContent().length);
                        }
                        outputStream.flush();
                        cont.getResponse().flushBuffer();
                    } else {
                        // Not ready, ignore
                    }
                } catch (IOException ex) { // Ignore
                }

            });
        }
    }

    @Override
    synchronized void notifyObservers(InterProtocolResponse response) {
        httpService.notifyObservers(response);
    }
}

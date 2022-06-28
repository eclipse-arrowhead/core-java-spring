package eu.arrowhead.core.translator.services.translator.protocols;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;

public class HttpOut extends ProtocolOut {

    private final static String GET = "GET";
    private final static String POST = "POST";
    private final static String PUT = "PUT";
    private final static String PATCH = "PATCH";
    private final static String DELETE = "DELETE";

    private final HttpClient client;

    public HttpOut(URI uri) throws Exception {
        super(uri);
        client = new HttpClient();
        client.start();
    }

    @Override
    public InterProtocolResponse get(InterProtocolRequest request) {
        String remoteRequest;
        try {
            remoteRequest = new URI(uri.getScheme(), uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                    request.getPath(), request.getQueries()).toString();
        } catch (URISyntaxException ex) {
            remoteRequest = uri.toString();
        }
        try {
            Request httpReq = client.POST(remoteRequest);
            httpReq.method(GET);
            httpReq.header(HttpHeader.CONNECTION, "");
            httpReq.header(HttpHeader.CONTENT_TYPE, request.getContentType());
            ContentResponse res = httpReq.send();

            return new InterProtocolResponse(
                    res.getMediaType(),
                    res.getStatus(),
                    res.getContent());

        } catch (InterruptedException | ExecutionException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (TimeoutException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_REQUEST_TIMEOUT,
                    ex.getLocalizedMessage().getBytes());
        }

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
            Request httpReq = client.POST(remoteRequest);
            httpReq.method(POST);
            httpReq.header(HttpHeader.CONNECTION, "");
            httpReq.header(HttpHeader.CONTENT_TYPE, request.getContentType());
            httpReq.content(new BytesContentProvider(request.getContent()));
            ContentResponse res = httpReq.send();
            return new InterProtocolResponse(
                    res.getMediaType(),
                    res.getStatus(),
                    res.getContent());
        } catch (InterruptedException | ExecutionException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (TimeoutException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_REQUEST_TIMEOUT,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse put(InterProtocolRequest request) {
        String remoteRequest;
        try {
            remoteRequest = new URI(uri.getScheme(), uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                    request.getPath(), request.getQueries()).toString();
        } catch (URISyntaxException ex) {
            remoteRequest = uri.toString();
        }
        try {
            Request httpReq = client.POST(remoteRequest);
            httpReq.method(PUT);
            httpReq.header(HttpHeader.CONNECTION, "");
            httpReq.header(HttpHeader.CONTENT_TYPE, request.getContentType());
            httpReq.content(new BytesContentProvider(request.getContent()));
            ContentResponse res = httpReq.send();
            return new InterProtocolResponse(
                    res.getMediaType(),
                    res.getStatus(),
                    res.getContent());
        } catch (InterruptedException | ExecutionException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (TimeoutException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_REQUEST_TIMEOUT,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse patch(InterProtocolRequest request) {
        String remoteRequest;
        try {
            remoteRequest = new URI(uri.getScheme(), uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                    request.getPath(), request.getQueries()).toString();
        } catch (URISyntaxException ex) {
            remoteRequest = uri.toString();
        }
        try {
            Request httpReq = client.POST(remoteRequest);
            httpReq.method(PATCH);
            httpReq.header(HttpHeader.CONNECTION, "");
            httpReq.header(HttpHeader.CONTENT_TYPE, request.getContentType());
            httpReq.content(new BytesContentProvider(request.getContent()));
            ContentResponse res = httpReq.send();
            return new InterProtocolResponse(
                    res.getMediaType(),
                    res.getStatus(),
                    res.getContent());
        } catch (InterruptedException | ExecutionException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (TimeoutException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_REQUEST_TIMEOUT,
                    ex.getLocalizedMessage().getBytes());
        }
    }

    @Override
    public InterProtocolResponse delete(InterProtocolRequest request) {
        String remoteRequest;
        try {
            remoteRequest = new URI(uri.getScheme(), uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                    request.getPath(), request.getQueries()).toString();
        } catch (URISyntaxException ex) {
            remoteRequest = uri.toString();
        }
        try {
            Request httpReq = client.POST(remoteRequest);
            httpReq.method(DELETE);
            httpReq.header(HttpHeader.CONNECTION, "");
            httpReq.header(HttpHeader.CONTENT_TYPE, request.getContentType());
            ContentResponse res = httpReq.send();
            return new InterProtocolResponse(
                    res.getMediaType(),
                    res.getStatus(),
                    res.getContent());
        } catch (InterruptedException | ExecutionException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (TimeoutException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_REQUEST_TIMEOUT,
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
            Request httpReq = client.POST(remoteRequest);
            httpReq.method(GET);
            httpReq.header(HttpHeader.CONNECTION, "keep-alive");
            httpReq.header(HttpHeader.CONTENT_TYPE, request.getContentType());
            InputStreamResponseListener listener = new InputStreamResponseListener();
            httpReq.send(listener);
            Response response = listener.get(10, TimeUnit.SECONDS);
            if (response.getStatus() == 200) {
                new Thread(() -> {
                    try (InputStream responseContent = listener.getInputStream()) {

                        ByteArrayOutputStream result = new ByteArrayOutputStream();
                        byte[] buffer = new byte[2048];
                        int length;
                        while ((length = responseContent.read(buffer)) != -1) {
                            result.write(buffer, 0, length);

                            protocolIn.notifyObservers(new InterProtocolResponse(
                                    MimeTypes.Type.TEXT_PLAIN,
                                    response.getStatus(),
                                    buffer));
                            buffer = new byte[2048];
                        }
                    } catch (IOException ex) {
                        // Ignore
                    }
                }).start();
            }
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    response.getStatus(),
                    "Stop Observing".getBytes());

        } catch (InterruptedException | ExecutionException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage().getBytes());
        } catch (TimeoutException ex) {
            return new InterProtocolResponse(
                    MimeTypes.Type.TEXT_PLAIN,
                    HttpServletResponse.SC_REQUEST_TIMEOUT,
                    ex.getLocalizedMessage() != null ? ex.getLocalizedMessage().getBytes() : "Timeout".getBytes());
        }

    }
}

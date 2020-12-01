package eu.arrowhead.core.translator.services.translator.spokes;


import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.translator.services.translator.common.TranslatorDef.Method;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerRegistry;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.network.config.NetworkConfig;

public class HttpProducerSpoke implements BaseSpokeProducer {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(HttpProducerSpoke.class);
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final int SOCKET_TIMEOUT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.HTTP_SERVER_SOCKET_TIMEOUT);
    private static final int SOCKET_BUFFER_SIZE = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.HTTP_SERVER_SOCKET_BUFFER_SIZE);
    private static final String SERVER_NAME = "Californium Http Proxy";

    Map<Integer, HttpAsyncExchange> cachedHttpExchangeMap = new HashMap<>();
    private String address = "";
    ListeningIOReactor ioReactor;
    private int activity = 0;

    public HttpProducerSpoke(String ipaddress, String path) throws IOException, InterruptedException {
        address = ipaddress;
        // HTTP parameters for the server
        HttpParams params = new SyncBasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT).setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, SOCKET_BUFFER_SIZE).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true).setParameter(CoreProtocolPNames.ORIGIN_SERVER, SERVER_NAME);

        // Create HTTP protocol processing chain
        // Use standard server-side protocol interceptors
        HttpRequestInterceptor[] requestInterceptors = new HttpRequestInterceptor[]{new RequestAcceptEncoding()};
        HttpResponseInterceptor[] responseInterceptors = new HttpResponseInterceptor[]{new ResponseAllowCORS(), new ResponseContentEncoding(), new ResponseDate(), new ResponseServer(), new ResponseContent(), new ResponseConnControl()};
        HttpProcessor httpProcessor = new ImmutableHttpProcessor(requestInterceptors, responseInterceptors);

        // Create request handler registry
        HttpAsyncRequestHandlerRegistry registry = new HttpAsyncRequestHandlerRegistry();

        // register the handler that will reply to the proxy requests
        registry.register(path, new RequestHandler("", true));

        // Create server-side HTTP protocol handler
        HttpAsyncService protocolHandler = new HttpAsyncService(httpProcessor, new DefaultConnectionReuseStrategy(), registry, params);

        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory = new DefaultNHttpServerConnectionFactory(params);

        // Create server-side I/O event dispatch
        final IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);

        try {
            // Create server-side I/O reactor
            ioReactor = new DefaultListeningIOReactor();
            // Listen of the given port

            InetSocketAddress socketAddress = new InetSocketAddress(ipaddress, 0);
            ListenerEndpoint endpoint1 = ioReactor.listen(socketAddress);

            // create the listener thread
            Thread listener = new Thread("Http listener") {

                @Override
                public void run() {
                    try {
                        ioReactor.execute(ioEventDispatch);
                    } catch (IOException ex) {
                        throw new ArrowheadException(ex.getLocalizedMessage());
                    }

                }
            };

            listener.setDaemon(false);
            listener.start();
            endpoint1.waitFor();

            if (address.contains(":")) {
                if (!address.startsWith("[")) {
                    address = "[" + address + "]";
                }
            }

            address = address + ":" + Integer.toString(((InetSocketAddress) endpoint1.getAddress()).getPort()) + "/";

        } catch (IOException ex) {
            logger.debug(ex.getMessage());
        }
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        try {
            ioReactor.shutdown();
        } catch (IOException ex) {
            logger.debug(ex.getMessage());
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String getAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void in(BaseContext context) {
        // get the sample http response
        HttpResponse httpResponse = cachedHttpExchangeMap.get(context.getKey()).getResponse();

        try {
            constructHttpResponse(context.getContent(), httpResponse);
        } catch (UnsupportedEncodingException ex) {
            logger.debug(ex.getMessage());
        }

        long lStartTime = System.nanoTime();
        // send the response
        cachedHttpExchangeMap.get(context.getKey()).submitResponse();

        cachedHttpExchangeMap.remove(context.getKey());
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void setNextSpoke(Object nextSpoke) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public int getLastActivity() {
        return activity;
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void clearActivity() {
        activity = 0;
    }

    //=================================================================================================
    // assistant methods
    //-------------------------------------------------------------------------------------------------
    private static void constructHttpResponse(String msg, HttpResponse httpResponse) throws UnsupportedEncodingException {
        if (msg != null) {
            HttpEntity httpEntity;
            // create the entity
            httpEntity = new StringEntity(msg);
            httpResponse.setEntity(httpEntity);
        }
    }

    private class RequestHandler implements HttpAsyncRequestHandler<HttpRequest> {

        public RequestHandler(String localResource, boolean proxyingEnabled) {
            super();
        }

        //=================================================================================================
        // methods
        //-------------------------------------------------------------------------------------------------
        @Override
        public void handle(HttpRequest httpRequest, HttpAsyncExchange httpExchange, HttpContext httpContext) throws HttpException, IOException {

            long lStartTime = System.nanoTime();
            activity++;
            BaseContext context = new BaseContext();

            //store the http context for generating the response
            cachedHttpExchangeMap.put(context.getKey(), httpExchange);

            context.setMethod(Method.valueOf(httpRequest.getRequestLine().getMethod().toUpperCase()));
            context.setPath(httpRequest.getRequestLine().getUri());
            // set the payload if the http entity is present
            if (httpRequest instanceof HttpEntityEnclosingRequest) {
                HttpEntity httpEntity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();

                // get the bytes from the entity
                String payload = EntityUtils.toString(httpEntity);
                
                context.setContent(payload);
            }
        }

        //-------------------------------------------------------------------------------------------------
        @Override
        public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
            // Buffer request content in memory for simplicity
            return new BasicAsyncRequestConsumer();
        }
    }
}

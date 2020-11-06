package eu.arrowhead.core.translator.services.translator.spokes;

import eu.arrowhead.core.translator.services.translator.common.TranslatorDef.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.CoapEndpoint.CoapEndpointBuilder;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.Resource;

public class CoapProducerSpoke extends CoapServer implements BaseSpokeProducer {

    //=================================================================================================
    // members
    BaseSpoke nextSpoke;
    Map<Integer, Exchange> cachedCoapExchangeMap = new HashMap<>();
    String interfaceAddress = "";
    private int activity = 0;

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public CoapProducerSpoke(String ipaddress, int port) {
        super(port);
        this.interfaceAddress = ipaddress;
        this.start();
    }

    //-------------------------------------------------------------------------------------------------
    public CoapProducerSpoke(String ipaddress) {
        super();
        InetSocketAddress socketAddress = new InetSocketAddress(ipaddress, 0);
        this.interfaceAddress = ipaddress;
        CoapEndpointBuilder builderCoap = new CoapEndpoint.CoapEndpointBuilder();
        builderCoap.setInetSocketAddress(socketAddress);
        this.addEndpoint(builderCoap.build());
        this.start();
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        this.destroy();
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String getAddress() {
        return "coap://" + this.interfaceAddress + ":" + Integer.toString(this.getEndpoints().get(0).getAddress().getPort()) + "/";

    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void in(BaseContext context) {
        Exchange exchange = cachedCoapExchangeMap.get(context.getKey());
        Response response = new Response(ResponseCode.CONTENT);
        response.setPayload(context.getContent());
        exchange.sendResponse(response);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void setNextSpoke(Object nextSpoke) {
        this.nextSpoke = (BaseSpoke) nextSpoke;
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

    //-------------------------------------------------------------------------------------------------
    @Override
    protected Resource createRoot() {
        return new RootResource();
    }

    private class RootResource extends CoapResource {

        public RootResource() {
            super("");
        }

        //=================================================================================================
        // methods
        //-------------------------------------------------------------------------------------------------
        @Override
        public List<Endpoint> getEndpoints() {
            return CoapProducerSpoke.this.getEndpoints();
        }

        //-------------------------------------------------------------------------------------------------
        @Override
        public void handleRequest(Exchange exchange) {
            exchange.sendAccept();
            activity++;
            BaseContext context = new BaseContext();
            cachedCoapExchangeMap.put(context.getKey(), exchange);
            context.setContent(exchange.getRequest().getPayloadString());
            context.setPath(exchange.getRequest().getOptions().getUriPathString());
            Code code = exchange.getRequest().getCode();
            Method method = null;
            switch (code) {
                case GET:
                    method = Method.GET;
                    break;
                case POST:
                    method = Method.POST;
                    break;
                case PUT:
                    method = Method.PUT;
                    break;
                case DELETE:
                    method = Method.DELETE;
                    break;
                default:
                    method = Method.GET;
                    break;
            }
            context.setMethod(method);
            nextSpoke.in(context);
        }

        //-------------------------------------------------------------------------------------------------
        @Override
        public Resource getChild(String name) {
            //all sub-resources come back to this root resource
            return this;
        }
    }
}

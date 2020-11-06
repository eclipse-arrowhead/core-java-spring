package eu.arrowhead.core.translator.services.translator.spokes;

import java.net.UnknownHostException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class CoapConsumerSpoke implements BaseSpokeConsumer {

    //=================================================================================================
    // members
    BaseSpoke nextSpoke;
    String serviceAddress = "";
    public int activity = 0;

    public CoapConsumerSpoke(String serviceAddress) throws UnknownHostException {
        if (serviceAddress.startsWith("coap")) {
            this.serviceAddress = serviceAddress;
        } else {
            this.serviceAddress = "coap://" + serviceAddress;
        }

        CoapClient pingClient = new CoapClient(serviceAddress);
        pingClient.ping();

    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @Override
    public void close() {
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void in(BaseContext context) {
        //if the context has no error then
        //start a coap client worker
        new Thread(new Worker(context), serviceAddress).start();
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

    class Worker implements Runnable {

        //=================================================================================================
        // members
        BaseContext context = null;

        public Worker(BaseContext paramContext) {
            this.context = paramContext;
        }

        //=================================================================================================
        // methods
        //-------------------------------------------------------------------------------------------------
        @Override
        public void run() {
            if (serviceAddress.endsWith("/") && context.getPath().startsWith("/")) {
                context.setPath(context.getPath().substring(1));
            }

            CoapClient client = new CoapClient(serviceAddress + context.getPath());

            CoapResponse response = null;
            long lStartTime = System.nanoTime();

            switch (context.getMethod()) {
                case GET:
                    response = client.get();
                    break;
                case POST:
                    response = client.post(context.getContent(), MediaTypeRegistry.parse(context.getContentType()));
                    break;
                case PUT:
                    response = client.put(context.getContent(), MediaTypeRegistry.parse(context.getContentType()));
                    break;
                case DELETE:
                    response = client.delete();
                    break;
                default:
                    break;
            }

            lStartTime = System.nanoTime();

            if (response != null) {
                context.setContent(response.getResponseText());
            }
            activity++;
            nextSpoke.in(context);
        }
    }
}

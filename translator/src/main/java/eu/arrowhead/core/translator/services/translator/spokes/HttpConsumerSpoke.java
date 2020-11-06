package eu.arrowhead.core.translator.services.translator.spokes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;

public class HttpConsumerSpoke implements BaseSpokeConsumer {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(HttpConsumerSpoke.class);
    BaseSpoke nextSpoke;
    String serviceAddress = "";
    public int activity = 0;

    public HttpConsumerSpoke(String serviceAddress) {
        this.serviceAddress = serviceAddress;

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
        new Thread(new Worker(context)).start();
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

            // get the requested host, if the port is not specified, the constructor
            // sets it to -1

            //Added path to access resources, not just root "/"
            String myurl = HttpConsumerSpoke.this.serviceAddress + "/" + this.context.getPath();
            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection conn;
            boolean payloadExpected = false;
            try {
                url = new URL(myurl);

                // create the requestLine
                conn = (HttpURLConnection) url.openConnection();

                switch (this.context.getMethod()) {
                    case GET:
                        conn.setRequestMethod("GET");
                        payloadExpected = false;
                        break;
                    case POST:
                        conn.setRequestMethod("POST");
                        payloadExpected = true;
                        break;
                    case PUT:
                        conn.setRequestMethod("PUT");
                        payloadExpected = true;
                        break;
                    case DELETE:
                        conn.setRequestMethod("DELETE");
                        payloadExpected = true;
                        break;
                    default:
                        break;
                }
                //if there is a payload then add that to the request
                if (payloadExpected) {
                    // create the content
                    conn.setDoOutput(true);
                    conn.setRequestProperty("content-type", MediaType.APPLICATION_JSON_VALUE);
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(this.context.getContent());
                    writer.flush();

                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                context.setContent(result.toString()); 
            } catch (IOException ex) {
                logger.debug(ex.getMessage());
            }

            // get the mapping to http for the incoming request
            activity++;
            nextSpoke.in(context);
        }
    }

}

package eu.arrowhead.core.translator.services.translator.spokes;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

public class ResponseAllowCORS implements HttpResponseInterceptor {

    //=================================================================================================
    // members

    public ResponseAllowCORS() {
        super();
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @Override
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null.");
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
}

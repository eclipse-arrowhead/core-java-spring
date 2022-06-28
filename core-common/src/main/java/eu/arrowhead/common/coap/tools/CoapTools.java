package eu.arrowhead.common.coap.tools;

import java.net.URLDecoder;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.DtlsEndpointContext;
import org.eclipse.californium.elements.EndpointContext;

public class CoapTools {

    public static String formatExchange(CoapExchange exchange) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request:\n\n");
        EndpointContext context = exchange.advanced().getRequest().getSourceContext();
        Principal identity = context.getPeerIdentity();
        if (identity != null) {
            sb.append(context.getPeerIdentity()).append("\n");
        } else {
            sb.append("anonymous\n");
        }
        sb.append(context.get(DtlsEndpointContext.KEY_CIPHER)).append("\n");
        sb.append(Utils.prettyPrint(exchange.advanced().getRequest())).append("\n");
        return sb.toString();
    }

    public static Map<String, String> getQueryParams(CoapExchange exchange) {
        Map<String, String> qp = new LinkedHashMap<>();
        try {
            String query = exchange.advanced().getRequest().getOptions().getUriQueryString();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                qp.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (Exception ex) {

        }
        return qp;
    }

    public static String getParam(Map<String, String> queryParams, String key, String defaultValue) {
        if (!queryParams.containsKey(key)) {
            return defaultValue;
        }
        return queryParams.get(key);
    }

    public static int getParam(Map<String, String> queryParams, String key, int defaultValue) {
        if (!queryParams.containsKey(key)) {
            return defaultValue;
        }
        return Integer.parseInt(queryParams.get(key));
    }

    public static long getParam(Map<String, String> queryParams, String key, long defaultValue) {
        if (!queryParams.containsKey(key)) {
            return defaultValue;
        }
        return Long.parseLong(queryParams.get(key));
    }

    public static String getUrlPathValue(CoapExchange exchange, String resourcePath) {
        String url = exchange.advanced().getRequest().getOptions().getUriPathString();
        int len = url.indexOf(resourcePath);
        String tmp = url.substring(len + resourcePath.length());
        String[] paths = tmp.split("/");
        return (paths.length < 2) ? "" : paths[1];
    }

}

package eu.arrowhead.core.translator.services.translator.common;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class TranslatorDef {

    public enum Protocol {
        coap     (0),
        http    (1);

        public final int value;

        Protocol(int value) {
            this.value = value;
        }

        public static Protocol valueOf(final int value) {
            switch (value) {
                case 0: return coap;
                case 1: return http;
                default: throw new IllegalArgumentException("Unknown Protocol " + value);
            }
        }
    }
    
    public static class EndPoint {
        private final URI uri;
        private final InetAddress ip;
        private final String name;

        public EndPoint(String name, String address) throws URISyntaxException, UnknownHostException  {
            this.name = name;
            uri = new URI(address);
            ip = InetAddress.getByName(uri.getHost());
        }

        public String getName() {
            return name;
        }

        public Protocol getProtocol() {
            return Protocol.valueOf(uri.getScheme().toLowerCase());
        }

        public String getHostName() {
            return uri.getHost();
        }

        public int getPort() {
            return uri.getPort();
        }

        public String getHostIpAddress() {
            return ip.getHostAddress();
        }

        public boolean isLocal() {
            return ip.isAnyLocalAddress() || ip.isLoopbackAddress();
        }
    }
    
    public enum Method {
        GET     (0),
        POST    (1),
        PUT     (2),
        DELETE  (3);

        public final int value;

        Method(int value) {
            this.value = value;
        }

        public static Method valueOf(final int value) {
            switch (value) {
                case 0: return GET;
                case 1: return POST;
                case 2: return PUT;
                case 3: return DELETE;
                default: throw new IllegalArgumentException("Unknown Method " + value);
            }
        }
    }
}

package eu.arrowhead.common.coap.configuration;

public class CoapServerConfiguration {
    
    private final String address;
    private final int port;
    private final boolean secured;
    private final CoapCredentials credentials;
    private final CoapCertificates certificates;

    public CoapServerConfiguration(String address, int port, boolean secured, CoapCredentials credentials,
            CoapCertificates certificates) {
        this.address = address;
        this.port = port;
        this.secured = secured;
        this.credentials = credentials;
        this.certificates = certificates;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecured() {
        return secured;
    }

    public CoapCredentials getCredentials() {
        return credentials;
    }

    public CoapCertificates getCertificates() {
        return certificates;
    }
}

package eu.arrowhead.common.coap.configuration;

public class CoapCertificates {
    
    private final String trustName;
    private final String trustStorePassword;
    private final String trustStorePath;

    public CoapCertificates(String trustName, String trustStorePassword, String trustStorePath) {
        this.trustName = trustName;
        this.trustStorePassword = trustStorePassword;
        this.trustStorePath = trustStorePath;
    }

    public String getTrustName() {
        return trustName;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }
}

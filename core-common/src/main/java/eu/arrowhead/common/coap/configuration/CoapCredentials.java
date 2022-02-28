package eu.arrowhead.common.coap.configuration;

public class CoapCredentials {
    
    private final String keyStorePath;
    private final String keyStorePassword;
    private final String keyPassword;
    private final String serverName;

    public CoapCredentials(String keyStorePath, String keyStorePassword, String keyPassword, String serverName) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.serverName = serverName;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getServerName() {
        return serverName;
    }
}

package eu.arrowhead.core.translator.services.translator.common;

public class TranslatorHubAccess {
    private final int id;
    private final String ip;
    private final int port;
    
    public TranslatorHubAccess(int tranlatorId, String ip, int port) {
        id = tranlatorId;
        this.ip = ip;
        this.port = port;
    }
    
    public int getTranslatorId() {
        return id;
    }
    
    public String getIp() {
        return ip;
    }
    
    public int getPort() {
        return port;
    }
}

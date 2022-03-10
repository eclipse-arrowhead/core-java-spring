package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;

public class ProtocolIn {

    final URI uri;
    ProtocolOut protocolOut;

    public ProtocolIn(URI uri) {
        this.uri = uri;
    }
    
    public void setProtocolOut(ProtocolOut protocolOut) {
        this.protocolOut = protocolOut;
    }

    synchronized void notifyObservers(InterProtocolResponse response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


package eu.arrowhead.core.translator.services.translator.protocols;

import java.net.URI;

import eu.arrowhead.core.translator.services.translator.common.Translation.ContentType;

public class ProtocolIn {

    final URI uri;
    ProtocolOut protocolOut;
    ContentType contentType = ContentType.ANY;

    public ProtocolIn(URI uri) {
        this.uri = uri;
    }

    public void setProtocolOut(ProtocolOut protocolOut) {
        this.protocolOut = protocolOut;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    synchronized void notifyObservers(InterProtocolResponse response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

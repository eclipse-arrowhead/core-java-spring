package eu.arrowhead.common.dto.shared;

import eu.arrowhead.common.core.CoreSystemService;

import java.io.Serializable;
import java.net.URI;

public class ServiceEndpoint implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    private CoreSystemService system;
    private URI uri;

    public ServiceEndpoint() {
        super();
    }

    public ServiceEndpoint(final CoreSystemService service, final URI uri) {
        this.system = service;
        this.uri = uri;
    }

    public CoreSystemService getService() {
        return system;
    }
    public void setSystem(final CoreSystemService system) { this.system = system; }

    public URI getUri() {
        return uri;
    }
    public void setUri(final URI uri) { this.uri = uri; }
}

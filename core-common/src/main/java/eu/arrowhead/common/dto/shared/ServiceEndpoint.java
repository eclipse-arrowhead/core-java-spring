package eu.arrowhead.common.dto.shared;

import eu.arrowhead.common.core.CoreSystemService;

import java.net.URI;

public class ServiceEndpoint
{

    private final CoreSystemService system;
    private final URI uri;

    public ServiceEndpoint(final CoreSystemService service, final URI uri)
    {
        this.system = service;
        this.uri = uri;
    }

    public CoreSystemService getService()
    {
        return system;
    }

    public URI getUri()
    {
        return uri;
    }
}

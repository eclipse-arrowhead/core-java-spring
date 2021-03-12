/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import eu.arrowhead.common.core.CoreSystemService;

import java.io.Serializable;
import java.net.URI;
import java.util.StringJoiner;

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

    @Override
    public String toString() {
        return new StringJoiner(", ", ServiceEndpoint.class.getSimpleName() + "[", "]")
                .add("system=" + system)
                .add("uri=" + uri)
                .toString();
    }
}

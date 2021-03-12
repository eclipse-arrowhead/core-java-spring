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

package eu.arrowhead.common.drivers;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;

public abstract class AbstractDriver {

    protected final DriverUtilities driverUtilities;
    protected final HttpService httpService;

    public AbstractDriver(final DriverUtilities driverUtilities, final HttpService httpService) {
        this.driverUtilities = driverUtilities;
        this.httpService = httpService;
    }

    public SystemRequestDTO getRequesterSystem() {
        return driverUtilities.getCoreSystemRequestDTO();
    }

    public DriverUtilities getDriverUtilities() {
        return driverUtilities;
    }

    public HttpService getHttpService() {
        return httpService;
    }
}

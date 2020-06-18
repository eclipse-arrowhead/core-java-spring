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

package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

/**
 * Handles HTTP requests to retrieve a specific Plant Description Entries.
 */
public class GetPlantDescription implements HttpRouteHandler {

    private final PlantDescriptionTracker pdTracker;
    private final MonitorInfo monitorInfo;

    /**
     * Class constructor
     *
     * @param monitorInfo Object that stores information on monitorable systems.
     * @param pdTracker   Object that stores information on Plant Description
     *                    entries.
     */
    public GetPlantDescription(MonitorInfo monitorInfo, PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(monitorInfo, "Expected MonitorInfo");
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");

        this.monitorInfo = monitorInfo;
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP call to acquire the PlantDescriptionEntry specified by the id
     * path parameter.
     *
     * @param request  HTTP request object.
     * @param response HTTP response containing the current
     *                 PlantDescriptionEntryList.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        String idString = request.pathParameter(0);
        int id;

        try {
            id = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            response.status(HttpStatus.BAD_REQUEST);
            response.body(ErrorMessage.of(idString + " is not a valid Plant Description Entry ID."));
            return Future.success(response);
        }

        final PlantDescriptionEntryDto entry = pdTracker.get(id);

        if (entry == null) {
            response.body(ErrorMessage.of("Plant Description with ID " + id + " not found."));
            response.status(HttpStatus.NOT_FOUND);
            return Future.success(response);
        }

        final var extendedEntry = DtoUtils.extend(entry, monitorInfo);
        response.status(HttpStatus.OK).body(extendedEntry);
        return Future.success(response);
    }
}
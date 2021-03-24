package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

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

    /**
     * Class constructor
     *
     * @param pdTracker Object that keeps track of Plant Description Entries.
     */
    public GetPlantDescription(PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
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
            return Future.success(response.status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(idString + " is not a valid Plant Description Entry ID.")));
        }

        final PlantDescriptionEntryDto entry = pdTracker.get(id);

        if (entry == null) {
            return Future.success(response.body(ErrorMessage.of("Plant Description with ID " + id + " not found."))
                .status(HttpStatus.NOT_FOUND));
        }

        return Future.success(response.status(HttpStatus.OK).body(entry));
    }
}
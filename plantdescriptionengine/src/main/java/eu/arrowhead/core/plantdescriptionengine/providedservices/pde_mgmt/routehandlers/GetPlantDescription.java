package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessageDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import se.arkalix.codec.CodecType;
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
    public GetPlantDescription(final PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP call to acquire the PlantDescriptionEntry specified by
     * the id path parameter.
     *
     * @param request  HTTP request object.
     * @param response HTTP response containing the current {@code
     *                 PlantDescriptionEntryList}.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        final String idString = request.pathParameter(0);
        final int id;

        try {
            id = Integer.parseInt(idString);
        } catch (final NumberFormatException e) {
            ErrorMessageDto errorMessage = ErrorMessage.of(idString + " is not a valid Plant Description Entry ID.");
            response
                .body(errorMessage, CodecType.JSON)
                .status(HttpStatus.BAD_REQUEST);
            return Future.success(response);
        }

        final PlantDescriptionEntryDto entry = pdTracker.get(id);

        if (entry == null) {
            ErrorMessageDto errorMessage = ErrorMessage.of("Plant Description with ID " + id + " not found.");
            response
                .status(HttpStatus.NOT_FOUND)
                .body(errorMessage, CodecType.JSON);
        } else {
            response
                .status(HttpStatus.OK)
                .body(entry, CodecType.JSON);
        }

        return Future.success(response);
    }
}
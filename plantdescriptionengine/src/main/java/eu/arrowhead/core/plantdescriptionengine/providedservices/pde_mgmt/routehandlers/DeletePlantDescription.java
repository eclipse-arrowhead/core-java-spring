package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.PlantDescriptionValidator;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Map;
import java.util.Objects;

/**
 * Handles HTTP requests to delete Plant Description Entries.
 */
public class DeletePlantDescription implements HttpRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeletePlantDescription.class);

    private final PlantDescriptionTracker pdTracker;

    /**
     * Class constructor
     *
     * @param pdTracker Object that keeps track of Plant Description Entries.
     */
    public DeletePlantDescription(final PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Entry Tracker");
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP request to delete an existing Plant Description from the
     * PDE.
     *
     * @param request  HTTP request containing a PlantDescription.
     * @param response HTTP response object.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        final int id;

        try {
            id = Integer.parseInt(request.pathParameter(0));
        } catch (final NumberFormatException e) {
            final String errMsg = "'" + request.pathParameter(0) + "' is not a valid Plant Description Entry ID.";
            response
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(errMsg), CodecType.JSON);
            return Future.success(response);
        }

        if (pdTracker.get(id) == null) {
            response
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorMessage.of("Plant Description with ID " + id + " not found."), CodecType.JSON);
            return Future.success(response);
        }

        // Check whether the remaining Plant Description entries are still valid
        // if this one is removed:
        final Map<Integer, PlantDescriptionEntry> entries = pdTracker.getEntryMap();
        entries.remove(id);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        if (validator.hasError()) {
            response
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(validator.getErrorMessage()), CodecType.JSON);
            return Future.success(response);
        }

        return pdTracker.remove(id)
            .map(result -> response.status(HttpStatus.OK))
            .mapCatch(PdStoreException.class, e -> {
                logger.error("Failed to remove Plant Description Entry from backing store", e);
                response
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorMessage.of("Encountered an error while deleting entry file."), CodecType.JSON);
                return response;
            });
    }
}
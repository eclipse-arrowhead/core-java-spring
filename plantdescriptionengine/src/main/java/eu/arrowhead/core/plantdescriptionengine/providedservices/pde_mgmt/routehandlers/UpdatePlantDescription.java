package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessageDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.PlantDescriptionValidator;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionUpdateDto;
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
 * Handles HTTP requests to update Plant Description Entries.
 */
public class UpdatePlantDescription implements HttpRouteHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdatePlantDescription.class);

    private final PlantDescriptionTracker pdTracker;

    /**
     * Class constructor
     *
     * @param pdTracker Object that keeps track of Plant Description Entries.
     */
    public UpdatePlantDescription(final PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Entry map");
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP request to update the Plant Description Entry specified
     * by the id parameter with the information in the request body.
     *
     * @param request  HTTP request containing a PlantDescriptionUpdate.
     * @param response HTTP response containing the updated entry.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        return request.bodyTo(PlantDescriptionUpdateDto::decodeJson)
            .flatMap(newFields -> {
                final String idString = request.pathParameter(0);
                final int id;

                try {
                    id = Integer.parseInt(idString);
                } catch (final NumberFormatException e) {

                    ErrorMessageDto errorMessage = ErrorMessage.of("'" + idString + "' is not a valid Plant Description Entry ID.");
                    response
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorMessage, CodecType.JSON);
                    return Future.success(response);
                }

                final PlantDescriptionEntryDto entry = pdTracker.get(id);

                if (entry == null) {
                    ErrorMessageDto errorMessage = ErrorMessage.of("Plant Description with ID '" + idString + "' not found.");
                    response
                        .status(HttpStatus.NOT_FOUND)
                        .body(errorMessage, CodecType.JSON);
                    return Future.success(response);
                }

                final PlantDescriptionEntryDto updatedEntry = PlantDescriptionEntry.update(entry, newFields);

                // Check if the changes to this entry lead to inconsistencies
                // (e.g. include cycles):
                final Map<Integer, PlantDescriptionEntry> entries = pdTracker.getEntryMap();
                entries.put(id, updatedEntry);
                final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
                if (validator.hasError()) {
                    response
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ErrorMessage.of(validator.getErrorMessage()), CodecType.JSON);
                    return Future.success(response);
                }

                return pdTracker.put(updatedEntry)
                    .map(result -> response
                        .status(HttpStatus.OK)
                        .body(updatedEntry, CodecType.JSON))
                    .mapCatch(PdStoreException.class, e -> {
                        logger.error("Failed to write Plant Description Entry update to backing store.", e);
                        return response.status(HttpStatus.INTERNAL_SERVER_ERROR);
                    });
            });
    }
}
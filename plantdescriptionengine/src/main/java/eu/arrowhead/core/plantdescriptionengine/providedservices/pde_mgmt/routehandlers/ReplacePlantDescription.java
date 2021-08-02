package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.PlantDescriptionValidator;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
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
 * Handles HTTP requests to update or create Plant Description Entries.
 */
public class ReplacePlantDescription implements HttpRouteHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReplacePlantDescription.class);

    private final PlantDescriptionTracker pdTracker;

    /**
     * Class constructor
     *
     * @param pdTracker Object that keeps track of Plant Description Entries.
     */
    public ReplacePlantDescription(final PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Entry map");
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP request to update or create the Plant Description Entry.
     *
     * @param request  HTTP request containing the ID of the entry to
     *                 create/update, and a {@code PlantDescriptionUpdate}
     *                 describing its new state.
     * @param response HTTP response containing the created/updated entry.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        return request.bodyTo(PlantDescriptionDto::decodeJson)
            .flatMap(description -> {
                final int id;

                try {
                    id = Integer.parseInt(request.pathParameter(0));
                } catch (final NumberFormatException e) {
                    return Future.success(response
                        .status(HttpStatus.BAD_REQUEST)
                        .body(request.pathParameter(0) + " is not a valid Plant Description Entry ID."));
                }

                final PlantDescriptionEntryDto entry = PlantDescriptionEntry.from(description, id);

                // Check if introducing this entry leads to inconsistencies
                // (e.g. include cycles):
                final Map<Integer, PlantDescriptionEntry> entries = pdTracker.getEntryMap();
                entries.put(id, entry);
                final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
                if (validator.hasError()) {
                    return Future.success(response
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ErrorMessage.of(validator.getErrorMessage()), CodecType.JSON));
                }

                return pdTracker.put(entry)
                    .map(result -> response.status(HttpStatus.OK).body(entry, CodecType.JSON))
                    .mapCatch(PdStoreException.class, e -> {
                        logger.error("Failed to write Plant Description Entry update to backing store.", e);
                        return response.status(HttpStatus.INTERNAL_SERVER_ERROR);
                    });
            });
    }
}
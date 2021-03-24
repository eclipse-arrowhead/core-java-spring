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
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

/**
 * Handles HTTP requests to create Plant Description Entries.
 */
public class AddPlantDescription implements HttpRouteHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddPlantDescription.class);

    private final PlantDescriptionTracker pdTracker;

    /**
     * Class constructor
     *
     * @param pdTracker Object that keeps track of Plant Description Entries.
     */
    public AddPlantDescription(PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP request to add a new Plant Description to the PDE.
     *
     * @param request  HTTP request object containing a Plant Description.
     * @param response HTTP response containing the newly created Plant Description
     *                 entry.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        return request.bodyAs(PlantDescriptionDto.class)
            .map(description -> {

                final PlantDescriptionEntryDto entry = PlantDescriptionEntry.from(description, pdTracker.getUniqueId());

                // Check if adding this entry leads to inconsistencies
                // (e.g. include cycles):
                final var entries = pdTracker.getEntryMap();
                entries.put(entry.id(), entry);
                final var validator = new PlantDescriptionValidator(entries);
                if (validator.hasError()) {
                    return response
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ErrorMessage.of(validator.getErrorMessage()));
                }

                try {
                    pdTracker.put(entry);
                } catch (final PdStoreException e) {
                    logger.error("Failure when communicating with backing store.", e);
                    return response.status(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return response.status(HttpStatus.CREATED).body(entry);
            });
    }
}
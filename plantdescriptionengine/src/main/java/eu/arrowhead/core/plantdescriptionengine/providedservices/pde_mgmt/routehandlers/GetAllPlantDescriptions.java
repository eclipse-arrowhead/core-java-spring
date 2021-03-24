package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryListBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles HTTP requests to retrieve all current Plant Description Entries.
 */
public class GetAllPlantDescriptions implements HttpRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetAllPlantDescriptions.class);

    private final PlantDescriptionTracker pdTracker;

    /**
     * Class constructor
     *
     * @param pdTracker Object that keeps track of Plant Description Entries.
     */
    public GetAllPlantDescriptions(PlantDescriptionTracker pdTracker) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Entry map");
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP request to acquire a list of Plant Description Entries
     * present in the PDE.
     *
     * @param request  HTTP request object.
     * @param response HTTP response whose body contains a list of Plant Description
     *                 entries.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        final var itemPerPageParam = new IntParameter.Builder()
            .name("item_per_page")
            .min(0)
            .build();
        final var pageParam = new IntParameter.Builder()
            .name("page")
            .min(0)
            .requires(itemPerPageParam)
            .build();

        final var sortFieldParam = new StringParameter.Builder()
            .name("sort_field")
            .legalValues("id", "createdAt", "updatedAt")
            .build();

        final var directionParam = new StringParameter.Builder()
            .name("direction")
            .legalValues("ASC", "DESC")
            .defaultValue("ASC")
            .build();
        final var activeParam = new BooleanParameter.Builder()
            .name("active")
            .build();

        final List<QueryParameter> acceptedParameters = List.of(pageParam, sortFieldParam, directionParam, activeParam);

        QueryParamParser parser;

        try {
            parser = new QueryParamParser(null, acceptedParameters, request);
        } catch (ParseError error) {
            response
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(error.getMessage()));
            logger.error("Encountered the following error(s) while parsing an HTTP request: " + error.getMessage());
            return Future.success(response);
        }

        List<PlantDescriptionEntryDto> entries = pdTracker.getEntries();

        final Optional<String> sortField = parser.getValue(sortFieldParam);
        if (sortField.isPresent()) {
            final String sortDirection = parser.getRequiredValue(directionParam);
            final boolean sortAscending = (sortDirection.equals("ASC"));
            PlantDescriptionEntry.sort(entries, sortField.get(), sortAscending);
        }

        final Optional<Integer> page = parser.getValue(pageParam);
        if (page.isPresent()) {
            int itemsPerPage = parser.getRequiredValue(itemPerPageParam);
            int from = Math.min(page.get() * itemsPerPage, entries.size());
            int to = Math.min(from + itemsPerPage, entries.size());

            entries = entries.subList(from, to);
        }

        final Optional<Boolean> active = parser.getValue(activeParam);
        if (active.isPresent()) {
            PlantDescriptionEntry.filterByActive(entries, active.get());
        }

        response.status(HttpStatus.OK)
            .body(new PlantDescriptionEntryListBuilder()
                .data(entries)
                .count(entries.size())
                .build());

        return Future.success(response);
    }
}
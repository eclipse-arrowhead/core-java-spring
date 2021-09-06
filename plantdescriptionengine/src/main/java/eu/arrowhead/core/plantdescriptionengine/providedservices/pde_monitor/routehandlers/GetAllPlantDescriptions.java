package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.MonitorPlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PlantDescriptionEntryListDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.BooleanParameter;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.IntParameter;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.ParseError;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.QueryParamParser;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.QueryParameter;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.StringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles HTTP requests to retrieve all current Plant Description Entries.
 */
public class GetAllPlantDescriptions implements HttpRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetAllPlantDescriptions.class);

    private final PlantDescriptionTracker pdTracker;
    private final MonitorInfoTracker monitorInfoTracker;

    /**
     * Class constructor
     *
     * @param monitorInfoTracker Object that stores information on monitorable
     *                           systems.
     * @param pdTracker          Object that stores information on Plant
     *                           Description entries.
     */
    public GetAllPlantDescriptions(
        final MonitorInfoTracker monitorInfoTracker,
        final PlantDescriptionTracker pdTracker
    ) {
        Objects.requireNonNull(monitorInfoTracker, "Expected MonitorInfoTracker");
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");

        this.monitorInfoTracker = monitorInfoTracker;
        this.pdTracker = pdTracker;
    }

    /**
     * Handles an HTTP request to acquire a list of Plant Description Entries
     * present in the PDE.
     *
     * @param request  HTTP request object.
     * @param response HTTP response whose body contains a list of Plant
     *                 Description entries.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        final IntParameter itemPerPageParam = new IntParameter.Builder()
            .name(QueryParameter.ITEM_PER_PAGE)
            .min(0)
            .build();
        final IntParameter pageParam = new IntParameter.Builder()
            .name(QueryParameter.PAGE)
            .min(0)
            .requires(itemPerPageParam)
            .build();
        final StringParameter sortFieldParam = new StringParameter.Builder()
            .name(QueryParameter.SORT_FIELD)
            .legalValues(QueryParameter.ID, QueryParameter.CREATED_AT, QueryParameter.UPDATED_AT)
            .build();
        final StringParameter directionParam = new StringParameter.Builder()
            .name(QueryParameter.DIRECTION)
            .legalValues(QueryParameter.ASC, QueryParameter.DESC)
            .defaultValue(QueryParameter.ASC)
            .build();

        final BooleanParameter activeParam = new BooleanParameter.Builder()
            .name(QueryParameter.ACTIVE)
            .build();

        final List<QueryParameter> acceptedParameters = List.of(pageParam, sortFieldParam, directionParam, activeParam);

        final QueryParamParser parser;

        try {
            parser = new QueryParamParser(null, acceptedParameters, request);
        } catch (final ParseError error) {
            logger.error("Encountered the following error(s) while parsing an HTTP request: " + error.getMessage());

            response
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(error.getMessage()), CodecType.JSON);
            return Future.success(response);

        }

        List<PlantDescriptionEntryDto> entries = pdTracker.getEntries();

        final Optional<String> sortField = parser.getValue(sortFieldParam);
        if (sortField.isPresent()) {
            final String sortDirection = parser.getRequiredValue(directionParam);
            final boolean sortAscending = QueryParameter.ASC.equals(sortDirection);
            switch (sortField.get()) {
                case QueryParameter.ID:
                    PlantDescriptionEntry.sortById(entries, sortAscending);
                    break;
                case QueryParameter.CREATED_AT:
                    PlantDescriptionEntry.sortByCreatedAt(entries, sortAscending);
                    break;
                case QueryParameter.UPDATED_AT:
                    PlantDescriptionEntry.sortByUpdatedAt(entries, sortAscending);
                    break;
            }
        }

        final Optional<Boolean> active = parser.getValue(activeParam);
        if (active.isPresent()) {
            PlantDescriptionEntry.filterByActive(entries, active.get());
        }

        final int count = entries.size();

        final Optional<Integer> page = parser.getValue(pageParam);
        if (page.isPresent()) {
            final int itemsPerPage = parser.getRequiredValue(itemPerPageParam);
            final int from = Math.min(page.get() * itemsPerPage, entries.size());
            final int to = Math.min(from + itemsPerPage, entries.size());
            entries = entries.subList(from, to);
        }

        // Extend each Plant Description Entry with monitor data retrieved from
        // monitorable services:
        final List<MonitorPlantDescriptionEntryDto> extendedEntries = new ArrayList<>(entries.size());
        for (final PlantDescriptionEntry entry : entries) {
            extendedEntries.add(DtoUtils.extend(entry, monitorInfoTracker, pdTracker));
        }

        PlantDescriptionEntryListDto result = new PlantDescriptionEntryListDto.Builder()
            .data(extendedEntries)
            .count(count)
            .build();

        response
            .status(HttpStatus.OK)
            .body(result, CodecType.JSON);

        return Future.success(response);
    }

}
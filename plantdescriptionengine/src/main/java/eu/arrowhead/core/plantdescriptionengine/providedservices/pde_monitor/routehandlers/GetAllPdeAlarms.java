package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmSeverity;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmListBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Handles HTTP requests to retrieve PDE alarms.
 */
public class GetAllPdeAlarms implements HttpRouteHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetAllPdeAlarms.class);

    private final AlarmManager alarmManager;

    /**
     * Constructor.
     *
     * @param alarmManager Object used for managing PDE alarms.
     */
    public GetAllPdeAlarms(AlarmManager alarmManager) {
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager.");
        this.alarmManager = alarmManager;
    }

    /**
     * Handles an HTTP call to acquire a list of PDE alarms raised by the PDE.
     *
     * @param request  HTTP request object.
     * @param response HTTP response containing an alarm list.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        List<String> severityValues = new ArrayList<>();
        for (var severity : AlarmSeverity.values()) {
            severityValues.add(severity.toString());
        }
        severityValues.add("not_cleared");

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
            .legalValues("id", "raisedAt", "updatedAt")
            .build();
        final var directionParam = new StringParameter.Builder()
            .name("direction")
            .legalValues("ASC", "DESC")
            .defaultValue("ASC")
            .build();
        final var systemNameParam = new StringParameter.Builder()
            .name("systemName")
            .build();
        final var severityParam = new StringParameter.Builder()
            .name("severity")
            .legalValues(severityValues)
            .build();
        final var acknowledgedParam = new BooleanParameter.Builder()
            .name("acknowledged")
            .build();

        final List<QueryParameter> acceptedParameters = List.of(pageParam, sortFieldParam, directionParam,
            systemNameParam, systemNameParam, severityParam, acknowledgedParam);

        QueryParamParser parser;

        try {
            parser = new QueryParamParser(null, acceptedParameters, request);
        } catch (ParseError error) {
            logger.error("Encountered the following error(s) while parsing an HTTP request: " + error.getMessage());
            return Future.success(response.status(HttpStatus.BAD_REQUEST).body(ErrorMessage.of(error.getMessage())));
        }

        List<PdeAlarmDto> alarms = alarmManager.getAlarms();

        final Optional<String> sortField = parser.getValue(sortFieldParam);
        if (sortField.isPresent()) {
            final String sortDirection = parser.getRequiredValue(directionParam);
            final boolean sortAscending = sortDirection.equals("ASC");
            PdeAlarm.sort(alarms, sortField.get(), sortAscending);
        }

        final Optional<Integer> page = parser.getValue(pageParam);
        if (page.isPresent()) {
            int itemsPerPage = parser.getRequiredValue(itemPerPageParam);

            int from = Math.min(page.get() * itemsPerPage, alarms.size());
            int to = Math.min(from + itemsPerPage, alarms.size());

            alarms = alarms.subList(from, to);
        }

        final Optional<String> systemName = parser.getValue(systemNameParam);
        if (systemName.isPresent()) {
            PdeAlarm.filterBySystemName(alarms, systemName.get());
        }

        final Optional<String> severityValue = parser.getValue(severityParam);
        if (severityValue.isPresent()) {
            PdeAlarm.filterBySeverity(alarms, severityValue.get());
        }

        final Optional<Boolean> acknowledged = parser.getValue(acknowledgedParam);
        if (acknowledged.isPresent()) {
            PdeAlarm.filterAcknowledged(alarms, acknowledged.get());
        }

        response.body(new PdeAlarmListBuilder()
            .data(alarms)
            .count(alarms.size())
            .build());
        response.status(HttpStatus.OK);
        return Future.success(response);
    }
}
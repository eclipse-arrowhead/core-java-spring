package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmSeverity;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmListDto;
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
    public GetAllPdeAlarms(final AlarmManager alarmManager) {
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

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        final List<String> severityValues = new ArrayList<>();
        for (final AlarmSeverity severity : AlarmSeverity.values()) {
            severityValues.add(severity.toString().toLowerCase());
        }

        // Add value corresponding to all severities that are not CLEARED:
        severityValues.add(PdeAlarm.NOT_CLEARED);

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
            .legalValues(
                QueryParameter.ID,
                QueryParameter.RAISED_AT,
                QueryParameter.UPDATED_AT,
                QueryParameter.CLEARED_AT
            )
            .build();
        final StringParameter directionParam = new StringParameter.Builder()
            .name(QueryParameter.DIRECTION)
            .legalValues(QueryParameter.ASC, QueryParameter.DESC)
            .defaultValue(QueryParameter.ASC)
            .build();
        final StringParameter systemNameParam = new StringParameter.Builder()
            .name(QueryParameter.SYSTEM_NAME)
            .build();
        final StringParameter severityParam = new StringParameter.Builder()
            .name(QueryParameter.SEVERITY)
            .legalValues(severityValues)
            .build();
        final BooleanParameter acknowledgedParam = new BooleanParameter.Builder()
            .name(QueryParameter.ACKNOWLEDGED)
            .build();

        final List<QueryParameter> acceptedParameters = List.of(pageParam, sortFieldParam, directionParam,
            systemNameParam, systemNameParam, severityParam, acknowledgedParam);

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

        List<PdeAlarmDto> alarms = alarmManager.getAlarms();

        final Optional<String> sortField = parser.getValue(sortFieldParam);
        if (sortField.isPresent()) {
            final String sortDirection = parser.getRequiredValue(directionParam);
            final boolean ascending = QueryParameter.ASC.equals(sortDirection);

            switch (sortField.get()) {
                case QueryParameter.ID:
                    PdeAlarm.sortById(alarms, ascending);
                    break;
                case QueryParameter.RAISED_AT:
                    PdeAlarm.sortByRaisedAt(alarms, ascending);
                    break;
                case QueryParameter.UPDATED_AT:
                    PdeAlarm.sortByUpdatedAt(alarms, ascending);
                    break;
                case QueryParameter.CLEARED_AT:
                    PdeAlarm.sortByClearedAt(alarms, ascending);
                    break;
                default:
                    // We should never reach this case, since the sortField
                    // param has been validated by the parser.
                    throw new AssertionError("Encountered the invalid sort field '" + sortField + "'.");

            }
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

        final int count = alarms.size();

        final Optional<Integer> page = parser.getValue(pageParam);
        if (page.isPresent()) {
            final int itemsPerPage = parser.getRequiredValue(itemPerPageParam);

            final int from = Math.min(page.get() * itemsPerPage, alarms.size());
            final int to = Math.min(from + itemsPerPage, alarms.size());

            alarms = alarms.subList(from, to);
        }

        PdeAlarmListDto result = new PdeAlarmListDto.Builder()
            .data(alarms)
            .count(count)
            .build();

        response
            .status(HttpStatus.OK)
            .body(result, CodecType.JSON);

        return Future.success(response);
    }
}
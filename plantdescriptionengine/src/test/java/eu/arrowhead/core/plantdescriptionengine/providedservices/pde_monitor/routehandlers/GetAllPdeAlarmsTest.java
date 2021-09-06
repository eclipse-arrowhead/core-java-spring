package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.Alarm;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmList;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.QueryParameter;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.Before;
import org.junit.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetAllPdeAlarmsTest {

    private AlarmManager alarmManager;
    private GetAllPdeAlarms handler;
    private MockServiceResponse response;


    private void raiseMultipleAlarms(AlarmManager alarmManager, int n) {
        for (int i = 0; i < n; i++) {
            alarmManager.raise(Alarm.createSystemNotRegisteredAlarm("system" + i, null, null));
        }
    }

    @Before
    public void initEach() {
        alarmManager = new AlarmManager();
        handler = new GetAllPdeAlarms(alarmManager);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldRejectNonBooleans() {

        final String nonBoolean = "Not a boolean";
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.ACKNOWLEDGED, nonBoolean) // Should be "true" or "false"
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> {
                final String expectedErrorMessage = "<Query parameter 'acknowledged' must be true or false, got '"
                    + nonBoolean + "'.>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldPaginate() {

        final int numAlarms = 10;

        raiseMultipleAlarms(alarmManager, numAlarms);

        final ArrayList<Integer> ids = new ArrayList<>();
        for (final PdeAlarm alarm : alarmManager.getAlarms()) {
            ids.add(alarm.id());
        }

        final int page = 2;
        final int itemsPerPage = 3;

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.SORT_FIELD, QueryParameter.ID)
            .queryParam(QueryParameter.PAGE, page)
            .queryParam(QueryParameter.ITEM_PER_PAGE, itemsPerPage)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            assertEquals(HttpStatus.OK, response.status().orElse(null));

            final PdeAlarmList alarms = (PdeAlarmList) response.getRawBody();
            assertEquals(numAlarms, alarms.count());
            for (int i = 0; i < itemsPerPage; i++) {
                final int index = page * itemsPerPage + i;
                final int alarmId = alarms.data().get(i).id();
                final int expectedId = ids.get(index);
                assertEquals(expectedId, alarmId);
            }

        }).onFailure(e -> fail());
    }

    @Test
    public void shouldRejectNegativePage() {
        final int page = -3;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.PAGE, page)
            .queryParam(QueryParameter.ITEM_PER_PAGE, 4)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            final String expectedErrorMessage = "<Query parameter '" + QueryParameter.PAGE +
                "' must be greater than or equal to 0, got " + page + ".>";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(expectedErrorMessage, actualErrorMessage);
        }).onFailure(e -> fail());

    }

    @Test
    public void shouldFilterBySystemName() {
        final String systemNameA = "sysa";

        alarmManager.raise(List.of(
            Alarm.createSystemNotRegisteredAlarm("Sys-A", systemNameA, null),
            Alarm.createSystemNotRegisteredAlarm("Sys-B", "sysb", null),
            Alarm.createSystemNotRegisteredAlarm("Sys-C", "sysc", null)
        ));

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.SYSTEM_NAME, systemNameA)
            .build();

        handler.handle(request, response)
            .ifSuccess(ascendingResult -> {
                final PdeAlarmList alarms = (PdeAlarmList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(1, alarms.count());
                assertEquals(systemNameA, alarms.data().get(0).systemName().orElse(null));
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldFilterBySeverity() {
        final String systemNameA = "sysa";
        final String systemNameB = "sysb";
        final String systemNameC = "sysc";

        alarmManager.raise(List.of(
            Alarm.createSystemNotRegisteredAlarm("Sys-A", systemNameA, null),
            Alarm.createSystemNotRegisteredAlarm("Sys-B", systemNameB, null)
        ));

        alarmManager.raiseNoPingResponse(systemNameC);
        alarmManager.clearNoPingResponse(systemNameC);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.SEVERITY, "warning")
            .build();

        handler.handle(request, response)
            .ifSuccess(ascendingResult -> {
                final PdeAlarmList alarms = (PdeAlarmList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(2, alarms.count());
                assertEquals(systemNameA, alarms.data().get(0).systemName().orElse(null));
                assertEquals(systemNameB, alarms.data().get(1).systemName().orElse(null));
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldFilterAcknowledged() {

        final String systemNameC = "sysc";

        alarmManager.raise(List.of(
            Alarm.createSystemNotInDescriptionAlarm("sysa", null),
            Alarm.createSystemNotInDescriptionAlarm("sysb", null),
            Alarm.createSystemNotInDescriptionAlarm(systemNameC, null)
        ));

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();

        alarmManager.acknowledge(alarms.get(0).id());
        alarmManager.acknowledge(alarms.get(1).id());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.ACKNOWLEDGED, false)
            .build();

        handler.handle(request, response)
            .ifSuccess(ascendingResult -> {
                final PdeAlarmList result = (PdeAlarmList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(1, result.count());
                assertEquals(systemNameC, result.data().get(0).systemName().orElse(null));
            })
            .onFailure(e -> fail());
    }

}
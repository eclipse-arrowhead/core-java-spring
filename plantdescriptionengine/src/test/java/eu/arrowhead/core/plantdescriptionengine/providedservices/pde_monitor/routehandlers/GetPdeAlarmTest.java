package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.Alarm;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.Before;
import org.junit.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetPdeAlarmTest {

    private AlarmManager alarmManager;
    private GetPdeAlarm handler;
    private MockServiceResponse response;

    @Before
    public void initEach() {
        alarmManager = new AlarmManager();
        handler = new GetPdeAlarm(alarmManager);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldRetrieveAlarm() {

        alarmManager.raise(Alarm.createSystemNotInDescriptionAlarm("abc", null));
        final PdeAlarmDto alarm = alarmManager.getAlarms().get(0);
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(alarm.id())))
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final PdeAlarm retrievedAlarm = (PdeAlarm) response.getRawBody();
            assertEquals(HttpStatus.OK, response.status().orElse(null));
            assertEquals(alarm.systemName().orElse(null), retrievedAlarm.systemName().orElse(null));
            assertEquals(alarm.severity(), retrievedAlarm.severity());
            assertEquals(alarm.acknowledged(), retrievedAlarm.acknowledged());
        }).onFailure(e -> fail());

    }

    @Test
    public void shouldRejectInvalidId() {

        final String invalidEntryId = "Invalid ID";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidEntryId))
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "'" + invalidEntryId + "' is not a valid PDE Alarm ID.";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);
        }).onFailure(e -> fail());

    }

    @Test
    public void shouldRejectNonexistentId() {

        final String nonexistentId = "31";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(nonexistentId))
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "PDE Alarm with ID '" + nonexistentId + "' not found.";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);
        }).onFailure(e -> fail());

    }
}
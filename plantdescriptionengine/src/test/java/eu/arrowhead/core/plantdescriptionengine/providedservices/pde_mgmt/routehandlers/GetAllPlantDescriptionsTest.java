package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryList;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GetAllPlantDescriptionsTest {

    @Test
    public void shouldRespondWithStoredEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(0, 1, 2, 3);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(entryIds.size(), entries.count());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldSortEntries() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final Instant createdAt1 = Instant.parse("2020-05-27T14:48:00.00Z");
        final Instant createdAt2 = Instant.parse("2020-06-27T14:48:00.00Z");
        final Instant createdAt3 = Instant.parse("2020-07-27T14:48:00.00Z");

        final Instant updatedAt1 = Instant.parse("2020-08-01T14:48:00.00Z");
        final Instant updatedAt2 = Instant.parse("2020-08-03T14:48:00.00Z");
        final Instant updatedAt3 = Instant.parse("2020-08-02T14:48:00.00Z");

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt1)
            .updatedAt(updatedAt1)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt2)
            .updatedAt(updatedAt2)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt3)
            .updatedAt(updatedAt3)
            .build();

        pdTracker.put(entry1);
        pdTracker.put(entry2);
        pdTracker.put(entry3);

        final int numEntries = pdTracker.getListDto().count();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final HttpServiceRequest idDescendingRequest = new MockRequest.Builder()
            .queryParameters(Map.of("sort_field", List.of("id"), "direction", List.of("DESC")))
            .build();
        final HttpServiceRequest creationAscendingRequest = new MockRequest.Builder()
            .queryParameters(Map.of("sort_field", List.of("createdAt"), "direction", List.of("ASC")))
            .build();
        final HttpServiceRequest updatesDescendingRequest = new MockRequest.Builder()
            .queryParameters(Map.of("sort_field", List.of("updatedAt"), "direction", List.of("DESC")))
            .build();
        final MockServiceResponse response1 = new MockServiceResponse();
        final MockServiceResponse response2 = new MockServiceResponse();
        final MockServiceResponse response3 = new MockServiceResponse();

        try {
            handler.handle(idDescendingRequest, response1).flatMap(result -> {
                assertTrue(response1.status().isPresent());
                assertEquals(HttpStatus.OK, response1.status().get());

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response1.getRawBody();
                assertEquals(numEntries, entries.count());

                int previousId = entries.data().get(0).id();
                for (int i = 1; i < entries.count(); i++) {
                    final PlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.id() <= previousId);
                    previousId = entry.id();
                }

                return handler.handle(creationAscendingRequest, response2);
            }).flatMap(result -> {
                assertTrue(response2.status().isPresent());
                assertEquals(HttpStatus.OK, response2.status().get());

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response2.getRawBody();
                assertEquals(numEntries, entries.count());

                Instant previousTimestamp = entries.data().get(0).createdAt();
                for (int i = 1; i < entries.count(); i++) {
                    final PlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.createdAt().compareTo(previousTimestamp) >= 0);
                    previousTimestamp = entry.createdAt();
                }

                return handler.handle(updatesDescendingRequest, response3);
            }).ifSuccess(result -> {
                assertTrue(response3.status().isPresent());
                assertEquals(HttpStatus.OK, response3.status().get());

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response3.getRawBody();
                assertEquals(numEntries, entries.count());

                Instant previousTimestamp = entries.data().get(0).updatedAt();
                for (int i = 1; i < entries.count(); i++) {
                    final PlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.updatedAt().compareTo(previousTimestamp) < 0);
                    previousTimestamp = entry.updatedAt();
                }
            })
            .onFailure(e -> fail());

        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectNonBooleans() throws PdStoreException {
        final List<Integer> entryIds = List.of(0, 1, 2);
        final int activeEntryId = 3;
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final Instant now = Instant.now();
        pdTracker.put(new PlantDescriptionEntryDto.Builder()
            .id(activeEntryId)
            .plantDescription("Plant Description XY")
            .active(true)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build());

        final String nonBoolean = "Not a boolean";

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("active", List.of(nonBoolean)))
            .build();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                final String expectedErrorMessage = "<Query parameter 'active' must be true or false, got '" + nonBoolean
                    + "'.>";

                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldFilterEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(0, 1, 2);
        final int activeEntryId = 3;
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final Instant now = Instant.now();
        pdTracker.put(new PlantDescriptionEntryDto.Builder()
            .id(activeEntryId)
            .plantDescription("Plant Description 1B")
            .active(true)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build());

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("active", List.of("true")))
            .build();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(1, entries.count());
                assertEquals(entries.data().get(0).id(), activeEntryId, 0);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldPaginate() throws PdStoreException {

        final List<Integer> entryIds = Arrays.asList(32, 11, 25, 3, 24, 35);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final MockServiceResponse response = new MockServiceResponse();
        final int page = 1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of(
                "sort_field", List.of("id"),
                "page", List.of(String.valueOf(page)),
                "item_per_page", List.of(String.valueOf(itemsPerPage))
            ))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(itemsPerPage, entries.count());

                // Sort the entry ID:s, so that their order will match that of
                // the response data.
                Collections.sort(entryIds);

                for (int i = 0; i < itemsPerPage; i++) {
                    final int index = page * itemsPerPage + i;
                    assertEquals((int) entryIds.get(index), entries.data().get(i).id());
                }

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectNegativePage() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final MockServiceResponse response = new MockServiceResponse();
        final int page = -1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(
                Map.of("page", List.of(String.valueOf(page)), "item_per_page", List.of(String.valueOf(itemsPerPage))))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

                final String expectedErrorMessage = "<Query parameter 'page' must be greater than 0, got " + page + ".>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRequireItemPerPage() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(pdTracker);
        final MockServiceResponse response = new MockServiceResponse();
        final int page = 4;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("page", List.of(String.valueOf(page))))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

                final String expectedErrorMessage = "<Missing parameter 'item_per_page'.>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

}
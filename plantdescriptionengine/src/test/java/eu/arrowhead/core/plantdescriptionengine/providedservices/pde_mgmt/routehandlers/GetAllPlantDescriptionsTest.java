package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryList;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.QueryParameter;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.util.concurrent.Futures;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GetAllPlantDescriptionsTest {

    final Instant t1 = Instant.parse("2020-05-27T14:48:00.00Z");
    final Instant t2 = Instant.parse("2020-06-27T14:48:00.00Z");
    final Instant t3 = Instant.parse("2020-07-27T14:48:00.00Z");

    final Instant t4 = Instant.parse("2020-08-01T14:48:00.00Z");
    final Instant t5 = Instant.parse("2020-08-02T14:48:00.00Z");
    final Instant t6 = Instant.parse("2020-08-03T14:48:00.00Z");

    PlantDescriptionTracker pdTracker;
    GetAllPlantDescriptions handler;
    MockServiceResponse response;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new GetAllPlantDescriptions(pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldRespondWithStoredEntries() {

        final List<Integer> entryIds = List.of(0, 1, 2, 3);
        final HttpServiceRequest request = new MockRequest();

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(entryIds.size(), entries.count());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldSortByIdDescending() {

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .createdAt(t1)
            .updatedAt(t4)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .createdAt(t2)
            .updatedAt(t6)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .createdAt(t3)
            .updatedAt(t5)
            .build();

        final HttpServiceRequest request = MockRequest.getSortRequest(
            QueryParameter.ID, QueryParameter.DESC
        );

        pdTracker.put(entry1)
            .flatMap(result -> pdTracker.put(entry2))
            .flatMap(result -> pdTracker.put(entry3))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();

                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(3, entries.count());

                int previousId = entries.data().get(0).id();
                for (int i = 1; i < entries.count(); i++) {
                    final PlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.id() <= previousId);
                    previousId = entry.id();
                }
            })
            .onFailure(e -> {
                e.printStackTrace();
                fail();
            });
    }

    @Test
    public void shouldSortByCreatedAtAscending() {
        final HttpServiceRequest request = MockRequest.getSortRequest(
            QueryParameter.CREATED_AT, QueryParameter.ASC
        );
        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .createdAt(t1)
            .updatedAt(t4)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .createdAt(t2)
            .updatedAt(t6)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .createdAt(t3)
            .updatedAt(t5)
            .build();

        pdTracker.put(entry1)
            .flatMap(result -> pdTracker.put(entry2))
            .flatMap(result -> pdTracker.put(entry3))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(3, entries.count());

                Instant previousTimestamp = entries.data().get(0).createdAt();
                for (int i = 1; i < entries.count(); i++) {
                    final PlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.createdAt().compareTo(previousTimestamp) >= 0);
                    previousTimestamp = entry.createdAt();
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldSortByUpdatedAtDescending() {
        final HttpServiceRequest request = MockRequest.getSortRequest(
            QueryParameter.UPDATED_AT, QueryParameter.DESC
        );

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .createdAt(t1)
            .updatedAt(t4)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .createdAt(t2)
            .updatedAt(t6)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .createdAt(t3)
            .updatedAt(t5)
            .build();

        pdTracker.put(entry1)
            .flatMap(result -> pdTracker.put(entry2))
            .flatMap(result -> pdTracker.put(entry3))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();

                assertTrue(response.status().isPresent());
                assertEquals(HttpStatus.OK, response.status().get());
                assertEquals(3, entries.count());

                Instant previousTimestamp = entries.data().get(0).updatedAt();
                for (int i = 1; i < entries.count(); i++) {
                    final PlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.updatedAt().compareTo(previousTimestamp) < 0);
                    previousTimestamp = entry.updatedAt();
                }
            })
            .onFailure(e -> fail());
    }


    @Test
    public void shouldRejectNonBooleans() {
        final String nonBoolean = "Not a boolean";
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.ACTIVE, nonBoolean)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "<Query parameter 'active' must be true or false, got '" +
                nonBoolean + "'.>";

            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);
        })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldFilterEntries() {

        final List<Integer> entryIds = List.of(0, 1, 2);
        final int activeEntryId = 3;
        final Instant now = Instant.now();
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.ACTIVE, true)
            .build();

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .flatMap(result -> pdTracker.put(new PlantDescriptionEntryDto.Builder()
                .id(activeEntryId)
                .plantDescription("Plant Description 1B")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build()))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(1, entries.count());
                assertEquals(entries.data().get(0).id(), activeEntryId);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldPaginate() {

        final List<Integer> entryIds = Arrays.asList(32, 11, 25, 3, 24, 35);

        final int page = 1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.SORT_FIELD, QueryParameter.ID)
            .queryParam(QueryParameter.PAGE, page)
            .queryParam(QueryParameter.ITEM_PER_PAGE, itemsPerPage)
            .build();

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(entryIds.size(), entries.count());

                // Sort the entry ID:s, so that their order will match that of
                // the response data.
                Collections.sort(entryIds);

                for (int i = 0; i < itemsPerPage; i++) {
                    final int index = page * itemsPerPage + i;
                    assertEquals((int) entryIds.get(index), entries.data().get(i).id());
                }
            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldRejectNegativePage() {
        final int page = -1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.PAGE, page)
            .queryParam(QueryParameter.ITEM_PER_PAGE, itemsPerPage)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "<Query parameter 'page' must be greater than or equal to 0, got " +
                page + ".>";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);

        })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRequireItemPerPage() {
        final int page = 4;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.PAGE, page)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "<Missing parameter 'item_per_page'.>";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);

        })
            .onFailure(e -> fail());
    }

}
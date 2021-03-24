package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BooleanParameterTest {

    @Test
    public void shouldParseBooleans() throws ParseError {

        final var smartParam = new BooleanParameter.Builder()
            .name("smart")
            .build();
        final var tiredParam = new BooleanParameter.Builder()
            .name("tired")
            .build();

        final List<QueryParameter> requiredParameters = List.of(smartParam, tiredParam);

        final var strongParam = new BooleanParameter.Builder()
            .name("strong")
            .build();
        final var fatParam = new BooleanParameter.Builder()
            .name("fat")
            .build();

        final List<QueryParameter> acceptedParameters = List.of(strongParam, fatParam);

        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("smart", List.of("true"),
            "tired", List.of("false"), "strong", List.of("true"), "fat", List.of("false"))).build();

        final var parser = new QueryParamParser(requiredParameters, acceptedParameters, request);

        assertTrue(parser.getValue(smartParam).orElse(false));
        assertFalse(parser.getValue(tiredParam).orElse(true));
        assertTrue(parser.getValue(strongParam).orElse(false));
        assertFalse(parser.getValue(fatParam).orElse(true));
    }

    @Test
    public void shouldUseDefaultArgument() throws ParseError {

        final var goodParam = new BooleanParameter.Builder()
            .name("good")
            .defaultValue(true)
            .build();
        final var happyParam = new BooleanParameter.Builder()
            .name("happy")
            .defaultValue(false)
            .build();

        final List<QueryParameter> acceptedParameters = List.of(goodParam, happyParam);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of())
            .build();

        final var parser = new QueryParamParser(null, acceptedParameters, request);

        assertTrue(parser.getValue(goodParam).orElse(false));
        assertFalse(parser.getValue(happyParam).orElse(true));
    }

    @Test
    public void shouldNonBooleans() {
        final List<QueryParameter> requiredParameters = List.of(new BooleanParameter.Builder()
            .name("cool")
            .build());

        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("cool", List.of("128")))
            .build();

        Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter 'cool' must be true or false, got '128'.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingParameter() {

        final List<QueryParameter> requiredParameters = List
            .of(new BooleanParameter.Builder()
                .name("weekends")
                .build());

        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParameters(Map.of())
                .build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals("<Missing parameter 'weekends'.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingDependency() {

        final List<QueryParameter> acceptedParameters = List.of(new BooleanParameter.Builder().name("sort")
            .requires(new IntParameter.Builder()
                .name("item_per_page")
                .build())
            .build());

        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParameters(Map.of("sort", List.of("true")))
                .build();
            new QueryParamParser(null, acceptedParameters, request);
        });
        assertEquals("<Missing parameter 'item_per_page'.>", exception.getMessage());
    }

}

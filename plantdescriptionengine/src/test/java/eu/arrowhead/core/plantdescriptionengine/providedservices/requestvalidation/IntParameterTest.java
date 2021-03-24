package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntParameterTest {

    @Test
    public void shouldParseIntegers() throws ParseError {

        final var a = new IntParameter.Builder().name("a").build();
        final var b = new IntParameter.Builder().name("b").build();
        final var c = new IntParameter.Builder().name("c").build();
        final var d = new IntParameter.Builder().name("d").build();

        final List<QueryParameter> requiredParameters = List.of(a, b);

        final List<QueryParameter> acceptedParameters = List.of(c, d);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("a", List.of("0"), "b", List.of("1"), "c", List.of("126"), "d", List.of("99999")))
            .build();

        final var parser = new QueryParamParser(requiredParameters, acceptedParameters, request);

        assertEquals(0, parser.getRequiredValue(a));
        assertEquals(1, parser.getRequiredValue(b));
        assertEquals(126, parser.getValue(c).orElse(null));
        assertEquals(99999, parser.getValue(d).orElse(null));
    }

    @Test
    public void shouldRejectNonInteger() {
        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name("weight")
            .build());

        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("weight", List.of("heavy")))
            .build();

        Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter 'weight' must be a valid integer, got 'heavy'.>", exception.getMessage());
    }

    @Test
    public void shouldRejectInvalidInteger() {
        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name("weight")
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("weight", List.of("123 test")))
            .build();

        Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter 'weight' must be a valid integer, got '123 test'.>", exception.getMessage());
    }

    @Test
    public void shouldRejectTooSmallValues() {
        final List<QueryParameter> requiredParameters = List.of(
            new IntParameter.Builder()
                .name("a")
                .min(38)
                .build(),
            new IntParameter.Builder()
                .name("b")
                .min(38)
                .build(), new IntParameter.Builder().name("c")
                .min(38)
                .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("a", List.of("39"), "b", List.of("38"), "c", List.of("37")))
            .build();

        Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter 'c' must be greater than 38, got 37.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingParameter() {

        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name("height")
            .build());

        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParameters(Map.of())
                .build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals("<Missing parameter 'height'.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingDependency() {

        final List<QueryParameter> acceptedParameters = List.of(new IntParameter.Builder()
            .name("a")
            .requires(new IntParameter.Builder()
                .name("b")
                .build())
            .build());
        Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("a", List.of("95")))
                .build();
            new QueryParamParser(null, acceptedParameters, request);
        });
        assertEquals("<Missing parameter 'b'.>", exception.getMessage());
    }
}

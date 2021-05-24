package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntParameterTest {

    @Test
    public void shouldParseIntegers() throws ParseError {
        final String keyA = "a";
        final String keyB = "b";
        final String keyC = "c";
        final String keyD = "d";

        final IntParameter a = new IntParameter.Builder().name(keyA).build();
        final IntParameter b = new IntParameter.Builder().name(keyB).build();
        final IntParameter c = new IntParameter.Builder().name(keyC).build();
        final IntParameter d = new IntParameter.Builder().name(keyD).build();

        final List<QueryParameter> requiredParameters = List.of(a, b);
        final List<QueryParameter> acceptedParameters = List.of(c, d);

        final int valueA = 0;
        final int valueB = 1;
        final int valueC = 126;
        final int valueD = 99999;

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(keyA, valueA)
            .queryParam(keyB, valueB)
            .queryParam(keyC, valueC)
            .queryParam(keyD, valueD)
            .build();

        final QueryParamParser parser = new QueryParamParser(requiredParameters, acceptedParameters, request);

        assertEquals(valueA, parser.getRequiredValue(a));
        assertEquals(valueB, parser.getRequiredValue(b));
        assertEquals(valueC, parser.getValue(c).orElse(null));
        assertEquals(valueD, parser.getValue(d).orElse(null));
    }

    @Test
    public void shouldRejectNonInteger() {
        final String key = "weight";
        final String value = "heavy";
        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name(key)
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key, value)
            .build();

        final Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter '" + key + "' must be a valid integer, got '" + value + "'.>", exception.getMessage());
    }

    @Test
    public void shouldRejectInvalidInteger() {
        final String key = "weight";
        final String value = "123 test";

        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name("weight")
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key, value)
            .build();

        final Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter '" + key + "' must be a valid integer, got '" +
            value + "'.>", exception.getMessage());
    }

    @Test
    public void shouldRejectTooSmallValues() {
        final String keyA = "a";
        final String keyB = "b";
        final String keyC = "c";

        final int min = 38;
        final int aValue = 39;
        final int bValue = 38;
        final int cValue = 37;

        final List<QueryParameter> requiredParameters = List.of(
            new IntParameter.Builder()
                .name(keyA)
                .min(min)
                .build(),
            new IntParameter.Builder()
                .name(keyB)
                .min(min)
                .build(),
            new IntParameter.Builder().name(keyC)
                .min(min)
                .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(keyA, aValue)
            .queryParam(keyB, bValue)
            .queryParam(keyC, cValue)
            .build();

        final Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter '" + keyC +
            "' must be greater than or equal to " + min + ", got " + cValue +
            ".>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingParameter() {
        final String key = "height";
        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name(key)
            .build());
        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals("<Missing parameter '" + key + "'.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingDependency() {
        final String keyA = "a";
        final String keyB = "b";

        final List<QueryParameter> acceptedParameters = List.of(new IntParameter.Builder()
            .name(keyA)
            .requires(new IntParameter.Builder()
                .name(keyB)
                .build())
            .build());
        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParam(keyA, 95)
                .build();
            new QueryParamParser(null, acceptedParameters, request);
        });
        assertEquals("<Missing parameter '" + keyB + "'.>", exception.getMessage());
    }
}

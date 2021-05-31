package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class IntParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        assertEquals(valueC, (int) parser.getValue(c).orElse(-1));
        assertEquals(valueD, (int) parser.getValue(d).orElse(-1));
    }

    @Test
    public void shouldRejectNonInteger() throws ParseError {
        final String key = "weight";
        final String value = "heavy";
        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name(key)
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key, value)
            .build();

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Query parameter '" + key + "' must be a valid integer, got '" + value + "'.>");
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldRejectInvalidInteger() throws ParseError {
        final String key = "weight";
        final String value = "123 test";

        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name("weight")
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key, value)
            .build();

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Query parameter '" + key + "' must be a valid integer, got '" + value + "'.>");
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldRejectTooSmallValues() throws ParseError {
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


        thrown.expect(ParseError.class);
        thrown.expectMessage(
            "<Query parameter '" + keyC +
                "' must be greater than or equal to " + min + ", got " + cValue +
                ".>"
        );
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldReportMissingParameter() throws ParseError {
        final String key = "height";
        final List<QueryParameter> requiredParameters = List.of(new IntParameter.Builder()
            .name(key)
            .build());

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Missing parameter '" + key + "'.>");
        final HttpServiceRequest request = new MockRequest.Builder().build();
        new QueryParamParser(requiredParameters, null, request);
    }

    @Test
    public void shouldReportMissingDependency() throws ParseError {
        final String keyA = "a";
        final String keyB = "b";

        final List<QueryParameter> acceptedParameters = List.of(new IntParameter.Builder()
            .name(keyA)
            .requires(new IntParameter.Builder()
                .name(keyB)
                .build())
            .build());

        thrown.expect(ParseError.class);
        thrown.expectMessage("<Missing parameter '" + keyB + "'.>");
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(keyA, 95)
            .build();
        new QueryParamParser(null, acceptedParameters, request);
    }
}

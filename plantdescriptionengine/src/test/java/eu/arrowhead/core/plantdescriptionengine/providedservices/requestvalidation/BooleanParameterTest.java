package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanParameterTest {

    @Test
    public void shouldParseBooleans() throws ParseError {

        String key1 = "smart";
        String key2 = "tired";
        String key3 = "strong";
        String key4 = "fat";

        final BooleanParameter smartParam = new BooleanParameter.Builder()
            .name(key1)
            .build();
        final BooleanParameter tiredParam = new BooleanParameter.Builder()
            .name(key2)
            .build();

        final List<QueryParameter> requiredParameters = List.of(smartParam, tiredParam);

        final BooleanParameter strongParam = new BooleanParameter.Builder().name(key3).build();
        final BooleanParameter fatParam = new BooleanParameter.Builder().name(key4).build();
        final List<QueryParameter> acceptedParameters = List.of(strongParam, fatParam);

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key1, true)
            .queryParam(key2, false)
            .queryParam(key3, true)
            .queryParam(key4, false)
            .build();

        final QueryParamParser parser = new QueryParamParser(requiredParameters, acceptedParameters, request);

        assertTrue(parser.getValue(smartParam).orElse(false));
        assertFalse(parser.getValue(tiredParam).orElse(true));
        assertTrue(parser.getValue(strongParam).orElse(false));
        assertFalse(parser.getValue(fatParam).orElse(true));
    }

    @Test
    public void shouldUseDefaultArgument() throws ParseError {

        final BooleanParameter goodParam = new BooleanParameter.Builder()
            .name("good")
            .defaultValue(true)
            .build();
        final BooleanParameter happyParam = new BooleanParameter.Builder()
            .name("happy")
            .defaultValue(false)
            .build();

        final List<QueryParameter> acceptedParameters = List.of(goodParam, happyParam);
        final HttpServiceRequest request = new MockRequest.Builder().build();
        final QueryParamParser parser = new QueryParamParser(null, acceptedParameters, request);

        assertTrue(parser.getValue(goodParam).orElse(false));
        assertFalse(parser.getValue(happyParam).orElse(true));
    }

    @Test
    public void shouldNonBooleans() {
        final String key = "cool";
        final String value = "123";
        final List<QueryParameter> requiredParameters = List.of(new BooleanParameter.Builder()
            .name(key)
            .build());

        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(key, value)
            .build();

        final Exception exception = assertThrows(ParseError.class,
            () -> new QueryParamParser(requiredParameters, null, request));

        assertEquals("<Query parameter '" + key + "' must be true or false, got '" + value + "'.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingParameter() {

        final List<QueryParameter> requiredParameters = List
            .of(new BooleanParameter.Builder()
                .name("weekends")
                .build());

        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder().build();
            new QueryParamParser(requiredParameters, null, request);
        });
        assertEquals("<Missing parameter 'weekends'.>", exception.getMessage());
    }

    @Test
    public void shouldReportMissingDependency() {
        final String sortKey = "sort";
        final QueryParameter itemPerPageParameter = new IntParameter.Builder()
            .name(QueryParameter.ITEM_PER_PAGE)
            .build();
        final QueryParameter sortParameter = new BooleanParameter.Builder().name(sortKey)
            .requires(itemPerPageParameter)
            .build();

        final Exception exception = assertThrows(ParseError.class, () -> {
            final HttpServiceRequest request = new MockRequest.Builder()
                .queryParam(sortKey, true)
                .build();
            new QueryParamParser(null, List.of(sortParameter), request);
        });
        assertEquals("<Missing parameter '" + QueryParameter.ITEM_PER_PAGE + "'.>", exception.getMessage());
    }

}
